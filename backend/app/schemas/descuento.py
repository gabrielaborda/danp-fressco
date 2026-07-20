"""
Schemas de Descuento.
"""
from datetime import date, datetime
from decimal import Decimal
from pydantic import BaseModel, model_validator


class DescuentoBase(BaseModel):
    tipo: str  # automatico_por_vencimiento | manual_admin
    porcentaje: Decimal | None = None
    monto_fijo: Decimal | None = None
    fecha_inicio: date
    fecha_fin: date
    activo: bool = True
    descripcion: str | None = None

    @model_validator(mode="after")
    def validar_tipo_descuento(self) -> "DescuentoBase":
        if self.porcentaje is None and self.monto_fijo is None:
            raise ValueError("Debe especificarse porcentaje o monto_fijo")
        return self


class DescuentoCreate(DescuentoBase):
    lote_id: int


class DescuentoManualCreate(BaseModel):
    """Schema simplificado para que el admin sobreescriba el descuento de un lote."""
    porcentaje: Decimal | None = None
    monto_fijo: Decimal | None = None
    fecha_inicio: date
    fecha_fin: date
    descripcion: str | None = None

    @model_validator(mode="after")
    def validar_al_menos_uno(self) -> "DescuentoManualCreate":
        if self.porcentaje is None and self.monto_fijo is None:
            raise ValueError("Debe especificarse porcentaje o monto_fijo")
        return self


class DescuentoResponse(DescuentoBase):
    id: int
    lote_id: int
    creado_en: datetime

    model_config = {"from_attributes": True}
