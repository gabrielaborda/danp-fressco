"""
Modelo: CarritoItem
Snapshot del precio con descuento al momento de agregar al carrito.
"""
from __future__ import annotations
from datetime import datetime
from decimal import Decimal
from typing import TYPE_CHECKING
from sqlalchemy.orm import Mapped, mapped_column, relationship
from sqlalchemy import Integer, Numeric, DateTime, ForeignKey, func
from app.models.base import Base

if TYPE_CHECKING:
    from app.models.cliente import Cliente
    from app.models.lote import Lote


class CarritoItem(Base):
    __tablename__ = "carrito_items"

    id: Mapped[int] = mapped_column(primary_key=True, index=True)
    cliente_id: Mapped[int] = mapped_column(ForeignKey("clientes.id"), nullable=False, index=True)
    lote_id: Mapped[int] = mapped_column(ForeignKey("lotes.id"), nullable=False)
    cantidad: Mapped[int] = mapped_column(Integer, nullable=False)
    precio_aplicado: Mapped[Decimal] = mapped_column(
        Numeric(10, 2), nullable=False
    )  # snapshot del precio con descuento al agregar
    agregado_en: Mapped[datetime] = mapped_column(
        DateTime(timezone=True), server_default=func.now()
    )

    # ─── Relaciones ────────────────────────────────────────────────────────────
    cliente: Mapped["Cliente"] = relationship(back_populates="carrito_items")
    lote: Mapped["Lote"] = relationship(back_populates="carrito_items")
