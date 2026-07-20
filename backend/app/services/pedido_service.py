"""
Servicio de Pedidos.

Responsabilidades:
- Confirmar compra desde el carrito del cliente con control de concurrencia.
- Cancelar pedidos y liberar stock.
- Verificar reglas de negocio (carrito no vacío, todos los lotes del mismo tienda, etc.).
"""
from decimal import Decimal
from sqlalchemy.orm import Session

from app.core.exceptions import (
    CarritoVacioError,
    BadRequestError,
    NotFoundError,
    ForbiddenError,
)
from app.models.carrito import CarritoItem
from app.models.cliente import Cliente
from app.models.pedido import Pedido, PedidoItem
from app.services.stock_service import reservar_stock, liberar_stock


def confirmar_pedido(db: Session, cliente: Cliente, notas: str | None = None) -> Pedido:
    """
    Convierte el carrito del cliente en un pedido confirmado.

    Pasos:
    1. Obtener items del carrito (valida que no esté vacío).
    2. Validar que todos los items pertenecen a la misma tienda.
    3. Por cada item: reservar stock con SELECT FOR UPDATE (atómico).
    4. Crear Pedido + PedidoItems con precios snapshot del carrito.
    5. Vaciar el carrito.
    6. Commit de la transacción completa.
    """
    items_carrito = (
        db.query(CarritoItem)
        .filter(CarritoItem.cliente_id == cliente.id)
        .all()
    )

    if not items_carrito:
        raise CarritoVacioError()

    # Determinar tienda (todos los items deben ser de la misma tienda)
    tiendas_ids: set[int] = set()
    for item in items_carrito:
        lote = item.lote
        if lote is None or lote.producto is None:
            raise BadRequestError(f"Lote #{item.lote_id} no tiene producto asociado")
        tiendas_ids.add(lote.producto.tienda_id)

    if len(tiendas_ids) > 1:
        raise BadRequestError(
            "El carrito contiene productos de múltiples tiendas. "
            "Por favor, realiza pedidos por tienda separados."
        )

    tienda_id = tiendas_ids.pop()

    # Calcular total usando el precio snapshot del carrito
    total: Decimal = sum(
        item.precio_aplicado * item.cantidad for item in items_carrito
    )

    # Crear el pedido
    pedido = Pedido(
        cliente_id=cliente.id,
        tienda_id=tienda_id,
        estado="confirmado",
        total=total,
        notas=notas,
    )
    db.add(pedido)
    db.flush()  # Obtener pedido.id sin commitear aún

    # Reservar stock y crear PedidoItems (atómico con FOR UPDATE)
    for item in items_carrito:
        reservar_stock(db, item.lote_id, item.cantidad)

        pedido_item = PedidoItem(
            pedido_id=pedido.id,
            lote_id=item.lote_id,
            cantidad=item.cantidad,
            precio_unitario_aplicado=item.precio_aplicado,
        )
        db.add(pedido_item)

    # Vaciar carrito
    for item in items_carrito:
        db.delete(item)

    db.commit()
    db.refresh(pedido)
    return pedido


def cancelar_pedido(db: Session, pedido_id: int, cliente: Cliente) -> Pedido:
    """
    Cancela un pedido del cliente y libera el stock reservado.
    Solo se pueden cancelar pedidos en estado 'pendiente' o 'confirmado'.
    """
    pedido = db.get(Pedido, pedido_id)

    if pedido is None:
        raise NotFoundError(f"Pedido #{pedido_id} no encontrado")

    if pedido.cliente_id != cliente.id:
        raise ForbiddenError("No tienes permiso para cancelar este pedido")

    if pedido.estado not in ("pendiente", "confirmado"):
        raise BadRequestError(
            f"No se puede cancelar un pedido en estado '{pedido.estado}'"
        )

    # Liberar stock de cada item
    for item in pedido.items:
        liberar_stock(db, item.lote_id, item.cantidad)

    pedido.estado = "cancelado"
    db.commit()
    db.refresh(pedido)
    return pedido


def cambiar_estado_admin(
    db: Session, pedido_id: int, nuevo_estado: str
) -> Pedido:
    """
    Permite al admin cambiar el estado de un pedido.
    Si se cancela, libera el stock.
    """
    estados_validos = {"confirmado", "entregado", "cancelado"}
    if nuevo_estado not in estados_validos:
        raise BadRequestError(
            f"Estado inválido '{nuevo_estado}'. Válidos: {', '.join(estados_validos)}"
        )

    pedido = db.get(Pedido, pedido_id)
    if pedido is None:
        raise NotFoundError(f"Pedido #{pedido_id} no encontrado")

    if pedido.estado == nuevo_estado:
        raise BadRequestError(f"El pedido ya está en estado '{nuevo_estado}'")

    if nuevo_estado == "cancelado" and pedido.estado in ("pendiente", "confirmado"):
        for item in pedido.items:
            liberar_stock(db, item.lote_id, item.cantidad)

    pedido.estado = nuevo_estado
    db.commit()
    db.refresh(pedido)
    return pedido
