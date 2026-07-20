"""
Router: Admin — Gestión de Pedidos
GET /admin/pedidos            (con filtros)
PUT /admin/pedidos/{id}/estado
"""
from datetime import date
from typing import Annotated
from fastapi import APIRouter, Depends
from sqlalchemy.orm import Session

from app.core.dependencies import require_admin
from app.db.session import get_db
from app.models.administrador import Administrador
from app.models.pedido import Pedido
from app.schemas.pedido import PedidoResponse, PedidoEstadoUpdate
from app.services.pedido_service import cambiar_estado_admin

router = APIRouter(prefix="/admin/pedidos", tags=["Admin — Pedidos"])


@router.get("", response_model=list[PedidoResponse])
def listar_pedidos(
    tienda_id: int | None = None,
    estado: str | None = None,
    fecha_desde: date | None = None,
    fecha_hasta: date | None = None,
    db: Session = Depends(get_db),
    _: Administrador = Depends(require_admin),
):
    """Lista todos los pedidos con filtros opcionales por tienda, estado y rango de fechas."""
    query = db.query(Pedido)

    if tienda_id:
        query = query.filter(Pedido.tienda_id == tienda_id)
    if estado:
        query = query.filter(Pedido.estado == estado)
    if fecha_desde:
        query = query.filter(Pedido.fecha_pedido >= fecha_desde)
    if fecha_hasta:
        query = query.filter(Pedido.fecha_pedido <= fecha_hasta)

    return query.order_by(Pedido.fecha_pedido.desc()).all()


@router.put("/{pedido_id}/estado", response_model=PedidoResponse)
def actualizar_estado_pedido(
    pedido_id: int,
    body: PedidoEstadoUpdate,
    db: Annotated[Session, Depends(get_db)],
    _: Annotated[Administrador, Depends(require_admin)],
):
    """Cambia el estado de un pedido. Si se cancela, libera el stock."""
    return cambiar_estado_admin(db, pedido_id, body.estado)
