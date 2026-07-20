"""
Modelo: Administrador (usuario con rol admin vinculado a una tienda)
"""
from __future__ import annotations
from typing import TYPE_CHECKING
from sqlalchemy.orm import Mapped, mapped_column, relationship
from sqlalchemy import String, ForeignKey
from app.models.base import Base

if TYPE_CHECKING:
    from app.models.tienda import Tienda


class Administrador(Base):
    __tablename__ = "administradores"

    id: Mapped[int] = mapped_column(primary_key=True, index=True)
    tienda_id: Mapped[int] = mapped_column(ForeignKey("tiendas.id"), nullable=False)
    nombre: Mapped[str] = mapped_column(String(120), nullable=False)
    email: Mapped[str] = mapped_column(String(120), unique=True, nullable=False, index=True)
    password_hash: Mapped[str] = mapped_column(String(255), nullable=False)
    rol: Mapped[str] = mapped_column(String(20), default="admin", nullable=False)
    activo: Mapped[bool] = mapped_column(default=True)

    # ─── Relaciones ────────────────────────────────────────────────────────────
    tienda: Mapped["Tienda"] = relationship(back_populates="administradores")
