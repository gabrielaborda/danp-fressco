"""
Modelo: Tienda
"""
from __future__ import annotations
from typing import TYPE_CHECKING
from sqlalchemy.orm import Mapped, mapped_column, relationship
from sqlalchemy import String, Text
from app.models.base import Base

if TYPE_CHECKING:
    from app.models.administrador import Administrador
    from app.models.producto import Producto
    from app.models.pedido import Pedido


class Tienda(Base):
    __tablename__ = "tiendas"

    id: Mapped[int] = mapped_column(primary_key=True, index=True)
    nombre: Mapped[str] = mapped_column(String(120), nullable=False)
    direccion: Mapped[str] = mapped_column(String(255), nullable=False)
    telefono: Mapped[str | None] = mapped_column(String(30))
    email_contacto: Mapped[str | None] = mapped_column(String(120))
    descripcion: Mapped[str | None] = mapped_column(Text)
    logo_url: Mapped[str | None] = mapped_column(String(500))
    activa: Mapped[bool] = mapped_column(default=True)

    # ─── Relaciones ────────────────────────────────────────────────────────────
    administradores: Mapped[list["Administrador"]] = relationship(
        back_populates="tienda", cascade="all, delete-orphan"
    )
    productos: Mapped[list["Producto"]] = relationship(
        back_populates="tienda", cascade="all, delete-orphan"
    )
    pedidos: Mapped[list["Pedido"]] = relationship(back_populates="tienda")
