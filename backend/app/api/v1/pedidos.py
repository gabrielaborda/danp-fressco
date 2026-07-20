"""
Router: Pedidos (cliente autenticado)
POST /pedidos              — confirmar compra desde el carrito
GET /pedidos               — historial
GET /pedidos/{id}
PUT /pedidos/{id}/cancelar
"""
from decimal import Decimal
from typing import Annotated
from fastapi import APIRouter, Depends
from sqlalchemy.orm import Session

from app.core.dependencies import require_cliente
from app.core.exceptions import NotFoundError, ForbiddenError
from app.db.session import get_db
from app.models.cliente import Cliente
from app.models.pedido import Pedido, PedidoItem
from app.schemas.pedido import PedidoCreate, PedidoResponse, PedidoItemResponse
from app.services.pedido_service import confirmar_pedido, cancelar_pedido

router = APIRouter(prefix="/pedidos", tags=["Pedidos"])


def _enriquecer_pedido(pedido: Pedido) -> PedidoResponse:
    items = [
        PedidoItemResponse(
            id=item.id,
            lote_id=item.lote_id,
            cantidad=item.cantidad,
            precio_unitario_aplicado=item.precio_unitario_aplicado,
            subtotal=item.precio_unitario_aplicado * item.cantidad,
            nombre_producto=(
                item.lote.producto.nombre
                if item.lote and item.lote.producto
                else None
            ),
        )
        for item in pedido.items
    ]
    return PedidoResponse(
        id=pedido.id,
        cliente_id=pedido.cliente_id,
        tienda_id=pedido.tienda_id,
        fecha_pedido=pedido.fecha_pedido,
        estado=pedido.estado,
        total=pedido.total,
        notas=pedido.notas,
        items=items,
    )


@router.post("", response_model=PedidoResponse, status_code=201)
def crear_pedido(
    body: PedidoCreate,
    db: Annotated[Session, Depends(get_db)],
    cliente: Annotated[Cliente, Depends(require_cliente)],
):
    """
    Confirma la compra convirtiendo el carrito en un pedido.
    Usa SELECT FOR UPDATE internamente para control de concurrencia.
    """
    pedido = confirmar_pedido(db, cliente, body.notas)
    return _enriquecer_pedido(pedido)


@router.get("", response_model=list[PedidoResponse])
def historial_pedidos(
    db: Annotated[Session, Depends(get_db)],
    cliente: Annotated[Cliente, Depends(require_cliente)],
):
    pedidos = (
        db.query(Pedido)
        .filter(Pedido.cliente_id == cliente.id)
        .order_by(Pedido.fecha_pedido.desc())
        .all()
    )
    return [_enriquecer_pedido(p) for p in pedidos]


@router.get("/{pedido_id}", response_model=PedidoResponse)
def obtener_pedido(
    pedido_id: int,
    db: Annotated[Session, Depends(get_db)],
    cliente: Annotated[Cliente, Depends(require_cliente)],
):
    pedido = db.get(Pedido, pedido_id)
    if pedido is None:
        raise NotFoundError(f"Pedido #{pedido_id} no encontrado")
    if pedido.cliente_id != cliente.id:
        raise ForbiddenError("No tienes permiso para ver este pedido")
    return _enriquecer_pedido(pedido)


@router.put("/{pedido_id}/cancelar", response_model=PedidoResponse)
def cancelar_pedido_endpoint(
    pedido_id: int,
    db: Annotated[Session, Depends(get_db)],
    cliente: Annotated[Cliente, Depends(require_cliente)],
):
    pedido = cancelar_pedido(db, pedido_id, cliente)
    return _enriquecer_pedido(pedido)
