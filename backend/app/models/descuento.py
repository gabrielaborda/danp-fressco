"""
Modelo: Descuento
Soporta descuentos automáticos (por proximidad a vencimiento) y manuales (admin).
El descuento manual activo tiene precedencia sobre el automático.
"""
from __future__ import annotations
from datetime import date, datetime
from decimal import Decimal
from typing import TYPE_CHECKING
from sqlalchemy.orm import Mapped, mapped_column, relationship
from sqlalchemy import String, Date, DateTime, Numeric, Boolean, ForeignKey, func
from app.models.base import Base

if TYPE_CHECKING:
    from app.models.lote import Lote


class Descuento(Base):
    __tablename__ = "descuentos"

    id: Mapped[int] = mapped_column(primary_key=True, index=True)
    lote_id: Mapped[int] = mapped_column(ForeignKey("lotes.id"), nullable=False, index=True)
    tipo: Mapped[str] = mapped_column(
        String(30), nullable=False
    )  # automatico_por_vencimiento | manual_admin
    porcentaje: Mapped[Decimal | None] = mapped_column(Numeric(5, 2))   # ej: 20.00 = 20%
    monto_fijo: Mapped[Decimal | None] = mapped_column(Numeric(10, 2))  # descuento en S/
    fecha_inicio: Mapped[date] = mapped_column(Date, nullable=False)
    fecha_fin: Mapped[date] = mapped_column(Date, nullable=False)
    activo: Mapped[bool] = mapped_column(Boolean, default=True, nullable=False)
    creado_en: Mapped[datetime] = mapped_column(
        DateTime(timezone=True), server_default=func.now()
    )
    descripcion: Mapped[str | None] = mapped_column(String(255))

    # ─── Relaciones ────────────────────────────────────────────────────────────
    lote: Mapped["Lote"] = relationship(back_populates="descuentos")
