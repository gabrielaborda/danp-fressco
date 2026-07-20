"""
Servicio de Carrito.
Encapsula las reglas de negocio del carrito de compras.
"""
from decimal import Decimal
from sqlalchemy.orm import Session

from app.core.exceptions import NotFoundError, BadRequestError, StockInsuficienteError
from app.models.carrito import CarritoItem
from app.models.lote import Lote
from app.models.cliente import Cliente
from app.services.descuento_service import resolver_descuento_efectivo
from app.services.stock_service import validar_lote_disponible


def agregar_item(
    db: Session,
    cliente: Cliente,
    lote_id: int,
    cantidad: int,
) -> CarritoItem:
    """
    Agrega un lote al carrito del cliente.
    - Valida que el lote exista, esté disponible y tenga stock.
    - Hace snapshot del precio con descuento en el momento de agregar.
    - Si el item ya existe en el carrito, suma la cantidad.
    """
    if cantidad <= 0:
        raise BadRequestError("La cantidad debe ser mayor a 0")

    lote = db.get(Lote, lote_id)
    if lote is None:
        raise NotFoundError(f"Lote #{lote_id} no encontrado")

    validar_lote_disponible(lote)

    # Verificar stock suficiente
    item_existente = (
        db.query(CarritoItem)
        .filter(CarritoItem.cliente_id == cliente.id, CarritoItem.lote_id == lote_id)
        .first()
    )
    cantidad_en_carrito = item_existente.cantidad if item_existente else 0
    cantidad_total = cantidad_en_carrito + cantidad

    if lote.cantidad < cantidad_total:
        raise StockInsuficienteError(
            f"Stock insuficiente: disponible={lote.cantidad}, en carrito={cantidad_en_carrito}, solicitado={cantidad}"
        )

    # Calcular precio con descuento (snapshot)
    descuento_info = resolver_descuento_efectivo(db, lote)
    precio_aplicado: Decimal = descuento_info["precio_con_descuento"]

    if item_existente:
        item_existente.cantidad = cantidad_total
        item_existente.precio_aplicado = precio_aplicado  # actualizar precio snapshot
        db.flush()
        return item_existente

    nuevo_item = CarritoItem(
        cliente_id=cliente.id,
        lote_id=lote_id,
        cantidad=cantidad,
        precio_aplicado=precio_aplicado,
    )
    db.add(nuevo_item)
    db.flush()
    return nuevo_item


def actualizar_item(
    db: Session,
    cliente: Cliente,
    item_id: int,
    nueva_cantidad: int,
) -> CarritoItem:
    """Actualiza la cantidad de un item del carrito."""
    if nueva_cantidad <= 0:
        raise BadRequestError("La cantidad debe ser mayor a 0")

    item = (
        db.query(CarritoItem)
        .filter(CarritoItem.id == item_id, CarritoItem.cliente_id == cliente.id)
        .first()
    )
    if item is None:
        raise NotFoundError(f"Item #{item_id} no encontrado en el carrito")

    lote = db.get(Lote, item.lote_id)
    validar_lote_disponible(lote)

    if lote.cantidad < nueva_cantidad:
        raise StockInsuficienteError(
            f"Stock insuficiente: disponible={lote.cantidad}, solicitado={nueva_cantidad}"
        )

    # Recalcular precio snapshot
    descuento_info = resolver_descuento_efectivo(db, lote)
    item.cantidad = nueva_cantidad
    item.precio_aplicado = descuento_info["precio_con_descuento"]
    db.flush()
    return item


def eliminar_item(db: Session, cliente: Cliente, item_id: int) -> None:
    """Elimina un item del carrito."""
    item = (
        db.query(CarritoItem)
        .filter(CarritoItem.id == item_id, CarritoItem.cliente_id == cliente.id)
        .first()
    )
    if item is None:
        raise NotFoundError(f"Item #{item_id} no encontrado en el carrito")
    db.delete(item)
    db.flush()


def vaciar_carrito(db: Session, cliente: Cliente) -> None:
    """Elimina todos los items del carrito del cliente."""
    db.query(CarritoItem).filter(CarritoItem.cliente_id == cliente.id).delete()
    db.flush()


def obtener_carrito(db: Session, cliente: Cliente) -> list[CarritoItem]:
    """Retorna todos los items del carrito del cliente con relaciones cargadas."""
    return (
        db.query(CarritoItem)
        .filter(CarritoItem.cliente_id == cliente.id)
        .all()
    )
