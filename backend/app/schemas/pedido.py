"""
Schemas de Pedido y PedidoItem.
"""
from datetime import datetime
from decimal import Decimal
from pydantic import BaseModel


class PedidoItemResponse(BaseModel):
    id: int
    lote_id: int
    cantidad: int
    precio_unitario_aplicado: Decimal
    subtotal: Decimal | None = None
    nombre_producto: str | None = None

    model_config = {"from_attributes": True}


class PedidoCreate(BaseModel):
    """Body opcional — la compra se toma directo del carrito del cliente."""
    notas: str | None = None


class PedidoResponse(BaseModel):
    id: int
    cliente_id: int
    tienda_id: int
    fecha_pedido: datetime
    estado: str
    total: Decimal
    notas: str | None
    items: list[PedidoItemResponse] = []

    model_config = {"from_attributes": True}


class PedidoEstadoUpdate(BaseModel):
    estado: str  # confirmado | entregado | cancelado


class PedidoCancelRequest(BaseModel):
    motivo: str | None = None
