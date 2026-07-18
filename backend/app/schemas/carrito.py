"""
Schemas de CarritoItem.
"""
from datetime import datetime
from decimal import Decimal
from pydantic import BaseModel


class CarritoItemCreate(BaseModel):
    lote_id: int
    cantidad: int


class CarritoItemUpdate(BaseModel):
    cantidad: int


class CarritoItemResponse(BaseModel):
    id: int
    lote_id: int
    cantidad: int
    precio_aplicado: Decimal
    agregado_en: datetime
    # Info del lote/producto
    nombre_producto: str | None = None
    imagen_url: str | None = None
    subtotal: Decimal | None = None

    model_config = {"from_attributes": True}


class CarritoResponse(BaseModel):
    items: list[CarritoItemResponse]
    total: Decimal
