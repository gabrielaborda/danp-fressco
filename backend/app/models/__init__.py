"""
Modelos SQLAlchemy — exporta todos para Alembic y conveniencia.
"""
from app.models.base import Base
from app.models.tienda import Tienda
from app.models.administrador import Administrador
from app.models.cliente import Cliente
from app.models.producto import Producto
from app.models.lote import Lote
from app.models.descuento import Descuento
from app.models.carrito import CarritoItem
from app.models.pedido import Pedido, PedidoItem

__all__ = [
    "Base",
    "Tienda",
    "Administrador",
    "Cliente",
    "Producto",
    "Lote",
    "Descuento",
    "CarritoItem",
    "Pedido",
    "PedidoItem",
]
