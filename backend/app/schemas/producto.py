"""
Schemas de Producto.
"""
from decimal import Decimal
from pydantic import BaseModel


class ProductoBase(BaseModel):
    nombre: str
    descripcion: str | None = None
    categoria: str | None = None
    precio_base: Decimal
    imagen_url: str | None = None


class ProductoCreate(ProductoBase):
    tienda_id: int


class ProductoUpdate(BaseModel):
    nombre: str | None = None
    descripcion: str | None = None
    categoria: str | None = None
    precio_base: Decimal | None = None
    imagen_url: str | None = None
    activo: bool | None = None


class ProductoResponse(ProductoBase):
    id: int
    tienda_id: int
    activo: bool

    model_config = {"from_attributes": True}
