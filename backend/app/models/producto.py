"""
Modelo: Producto
"""
from __future__ import annotations
from typing import TYPE_CHECKING
from sqlalchemy.orm import Mapped, mapped_column, relationship
from sqlalchemy import String, Text, Numeric, ForeignKey
from decimal import Decimal
from app.models.base import Base

if TYPE_CHECKING:
    from app.models.tienda import Tienda
    from app.models.lote import Lote


class Producto(Base):
    __tablename__ = "productos"

    id: Mapped[int] = mapped_column(primary_key=True, index=True)
    tienda_id: Mapped[int] = mapped_column(ForeignKey("tiendas.id"), nullable=False)
    nombre: Mapped[str] = mapped_column(String(200), nullable=False)
    descripcion: Mapped[str | None] = mapped_column(Text)
    categoria: Mapped[str | None] = mapped_column(String(80), index=True)
    precio_base: Mapped[Decimal] = mapped_column(Numeric(10, 2), nullable=False)
    imagen_url: Mapped[str | None] = mapped_column(String(500))
    activo: Mapped[bool] = mapped_column(default=True)

    # ─── Relaciones ────────────────────────────────────────────────────────────
    tienda: Mapped["Tienda"] = relationship(back_populates="productos")
    lotes: Mapped[list["Lote"]] = relationship(
        back_populates="producto", cascade="all, delete-orphan"
    )
