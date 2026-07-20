"""
Schemas de Lote — incluyendo respuesta enriquecida con descuento calculado.
"""
from datetime import date, datetime
from decimal import Decimal
from pydantic import BaseModel


class LoteBase(BaseModel):
    producto_id: int
    cantidad: int
    fecha_ingreso: date
    fecha_vencimiento: date
    precio_lote: Decimal


class LoteCreate(LoteBase):
    pass


class LoteUpdate(BaseModel):
    cantidad: int | None = None
    fecha_vencimiento: date | None = None
    precio_lote: Decimal | None = None
    estado: str | None = None  # disponible | agotado | vencido | dado_de_baja


class LoteResponse(LoteBase):
    id: int
    cantidad_inicial: int
    estado: str
    creado_en: datetime

    model_config = {"from_attributes": True}


class LoteConDescuento(LoteResponse):
    """Respuesta para el catálogo: incluye precio final con descuento."""
    precio_con_descuento: Decimal
    porcentaje_descuento: Decimal
    dias_para_vencer: int
    tiene_descuento_manual: bool
    nombre_producto: str
    imagen_url: str | None
    categoria: str | None
    nombre_tienda: str
