"""
Modelos: Pedido y PedidoItem
El precio unitario aplicado se almacena como snapshot, nunca se recalcula.
"""
from __future__ import annotations
from datetime import datetime
from decimal import Decimal
from typing import TYPE_CHECKING
from sqlalchemy.orm import Mapped, mapped_column, relationship
from sqlalchemy import Integer, Numeric, String, DateTime, ForeignKey, func
from app.models.base import Base

if TYPE_CHECKING:
    from app.models.cliente import Cliente
    from app.models.tienda import Tienda
    from app.models.lote import Lote


class Pedido(Base):
    __tablename__ = "pedidos"

    id: Mapped[int] = mapped_column(primary_key=True, index=True)
    cliente_id: Mapped[int] = mapped_column(ForeignKey("clientes.id"), nullable=False, index=True)
    tienda_id: Mapped[int] = mapped_column(ForeignKey("tiendas.id"), nullable=False, index=True)
    fecha_pedido: Mapped[datetime] = mapped_column(
        DateTime(timezone=True), server_default=func.now()
    )
    estado: Mapped[str] = mapped_column(
        String(20), default="pendiente", nullable=False, index=True
    )  # pendiente | confirmado | cancelado | entregado
    total: Mapped[Decimal] = mapped_column(Numeric(12, 2), nullable=False)
    notas: Mapped[str | None] = mapped_column(String(500))

    # ─── Relaciones ────────────────────────────────────────────────────────────
    cliente: Mapped["Cliente"] = relationship(back_populates="pedidos")
    tienda: Mapped["Tienda"] = relationship(back_populates="pedidos")
    items: Mapped[list["PedidoItem"]] = relationship(
        back_populates="pedido", cascade="all, delete-orphan"
    )


class PedidoItem(Base):
    __tablename__ = "pedido_items"

    id: Mapped[int] = mapped_column(primary_key=True, index=True)
    pedido_id: Mapped[int] = mapped_column(ForeignKey("pedidos.id"), nullable=False, index=True)
    lote_id: Mapped[int] = mapped_column(ForeignKey("lotes.id"), nullable=False)
    cantidad: Mapped[int] = mapped_column(Integer, nullable=False)
    precio_unitario_aplicado: Mapped[Decimal] = mapped_column(
        Numeric(10, 2), nullable=False
    )  # snapshot inmutable del precio al momento de la compra

    # ─── Relaciones ────────────────────────────────────────────────────────────
    pedido: Mapped["Pedido"] = relationship(back_populates="items")
    lote: Mapped["Lote"] = relationship(back_populates="pedido_items")
