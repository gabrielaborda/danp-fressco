"""
Modelo: Cliente (usuario final de la app Android)
"""
from __future__ import annotations
from datetime import datetime, timezone
from typing import TYPE_CHECKING
from sqlalchemy.orm import Mapped, mapped_column, relationship
from sqlalchemy import String, DateTime, func
from app.models.base import Base

if TYPE_CHECKING:
    from app.models.carrito import CarritoItem
    from app.models.pedido import Pedido


class Cliente(Base):
    __tablename__ = "clientes"

    id: Mapped[int] = mapped_column(primary_key=True, index=True)
    nombre: Mapped[str] = mapped_column(String(120), nullable=False)
    email: Mapped[str] = mapped_column(String(120), unique=True, nullable=False, index=True)
    password_hash: Mapped[str] = mapped_column(String(255), nullable=False)
    rol: Mapped[str] = mapped_column(String(20), default="cliente", nullable=False)
    telefono: Mapped[str | None] = mapped_column(String(30))
    fecha_registro: Mapped[datetime] = mapped_column(
        DateTime(timezone=True),
        server_default=func.now(),
    )
    estado: Mapped[str] = mapped_column(
        String(20), default="activo", nullable=False
    )  # activo | suspendido

    # ─── Relaciones ────────────────────────────────────────────────────────────
    carrito_items: Mapped[list["CarritoItem"]] = relationship(
        back_populates="cliente", cascade="all, delete-orphan"
    )
    pedidos: Mapped[list["Pedido"]] = relationship(back_populates="cliente")
