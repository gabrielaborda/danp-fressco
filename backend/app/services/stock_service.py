"""
Servicio de Stock.

Manejo seguro de stock con control de concurrencia usando SELECT FOR UPDATE.
Garantiza que dos requests simultáneas no puedan sobrepasar el stock disponible.
"""
from datetime import date
from sqlalchemy.orm import Session

from app.core.exceptions import StockInsuficienteError, LoteVencidoError, NotFoundError
from app.models.lote import Lote


def validar_lote_disponible(lote: Lote) -> None:
    """
    Valida que un lote esté disponible para compra/carrito.
    Lanza LoteVencidoError o StockInsuficienteError según corresponda.
    """
    if lote.fecha_vencimiento <= date.today():
        raise LoteVencidoError(f"El lote #{lote.id} está vencido")

    if lote.estado != "disponible":
        raise LoteVencidoError(
            f"El lote #{lote.id} no está disponible (estado: {lote.estado})"
        )

    if lote.cantidad <= 0:
        raise StockInsuficienteError(f"El lote #{lote.id} no tiene stock")


def reservar_stock(db: Session, lote_id: int, cantidad: int) -> Lote:
    """
    Reserva (descuenta) stock de un lote de forma atómica y segura.

    Usa SELECT FOR UPDATE para bloquear la fila durante la transacción,
    evitando condiciones de carrera cuando múltiples clientes compran
    el mismo lote simultáneamente.

    IMPORTANTE: Debe llamarse dentro de una transacción activa.
    La sesión debe hacer commit() después de llamar a esta función.
    """
    # Bloqueo a nivel de fila: ninguna otra transacción puede leer
    # ni modificar este lote hasta que la transacción actual haga commit/rollback.
    lote = (
        db.query(Lote)
        .filter(Lote.id == lote_id)
        .with_for_update()
        .first()
    )

    if lote is None:
        raise NotFoundError(f"Lote #{lote_id} no encontrado")

    validar_lote_disponible(lote)

    if lote.cantidad < cantidad:
        raise StockInsuficienteError(
            f"Stock insuficiente para lote #{lote_id}: "
            f"disponible={lote.cantidad}, solicitado={cantidad}"
        )

    lote.cantidad -= cantidad

    if lote.cantidad == 0:
        lote.estado = "agotado"

    return lote


def liberar_stock(db: Session, lote_id: int, cantidad: int) -> Lote:
    """
    Devuelve stock a un lote (ej: al cancelar un pedido).
    """
    lote = (
        db.query(Lote)
        .filter(Lote.id == lote_id)
        .with_for_update()
        .first()
    )

    if lote is None:
        raise NotFoundError(f"Lote #{lote_id} no encontrado")

    lote.cantidad += cantidad

    # Si el lote estaba agotado y ahora tiene stock, volvemos a disponible
    if lote.estado == "agotado" and lote.cantidad > 0:
        if lote.fecha_vencimiento > date.today():
            lote.estado = "disponible"

    return lote
