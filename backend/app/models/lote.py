"""
Modelo: Lote
Un lote es una cantidad específica de un producto con fecha de vencimiento.
"""
from __future__ import annotations
from datetime import date, datetime
from decimal import Decimal
from typing import TYPE_CHECKING
from sqlalchemy.orm import Mapped, mapped_column, relationship
from sqlalchemy import String, Date, DateTime, Numeric, Integer, ForeignKey, func
from app.models.base import Base

if TYPE_CHECKING:
    from app.models.producto import Producto
    from app.models.descuento import Descuento
    from app.models.carrito import CarritoItem
    from app.models.pedido import PedidoItem


class Lote(Base):
    __tablename__ = "lotes"

    id: Mapped[int] = mapped_column(primary_key=True, index=True)
    producto_id: Mapped[int] = mapped_column(ForeignKey("productos.id"), nullable=False)
    cantidad: Mapped[int] = mapped_column(Integer, nullable=False)
    cantidad_inicial: Mapped[int] = mapped_column(Integer, nullable=False)
    fecha_ingreso: Mapped[date] = mapped_column(Date, nullable=False)
    fecha_vencimiento: Mapped[date] = mapped_column(Date, nullable=False, index=True)
    precio_lote: Mapped[Decimal] = mapped_column(Numeric(10, 2), nullable=False)
    estado: Mapped[str] = mapped_column(
        String(20), default="disponible", nullable=False, index=True
    )  # disponible | agotado | vencido | dado_de_baja
    creado_en: Mapped[datetime] = mapped_column(
        DateTime(timezone=True), server_default=func.now()
    )

    # ─── Relaciones ────────────────────────────────────────────────────────────
    producto: Mapped["Producto"] = relationship(back_populates="lotes")
    descuentos: Mapped[list["Descuento"]] = relationship(
        back_populates="lote", cascade="all, delete-orphan"
    )
    carrito_items: Mapped[list["CarritoItem"]] = relationship(back_populates="lote")
    pedido_items: Mapped[list["PedidoItem"]] = relationship(back_populates="lote")
