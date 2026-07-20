"""
Router: Admin — Reportes
GET /admin/reportes/productos-mas-vendidos
GET /admin/reportes/lotes-proximos-a-vencer
GET /admin/reportes/ventas-por-periodo
"""
from datetime import date, timedelta
from typing import Annotated
from fastapi import APIRouter, Depends
from sqlalchemy import func
from sqlalchemy.orm import Session

from app.core.dependencies import require_admin
from app.db.session import get_db
from app.models.administrador import Administrador
from app.models.lote import Lote
from app.models.pedido import Pedido, PedidoItem
from app.models.producto import Producto

router = APIRouter(prefix="/admin/reportes", tags=["Admin — Reportes"])


@router.get("/productos-mas-vendidos")
def productos_mas_vendidos(
    limite: int = 10,
    tienda_id: int | None = None,
    db: Session = Depends(get_db),
    _: Administrador = Depends(require_admin),
):
    """Retorna los productos con mayor cantidad de unidades vendidas."""
    query = (
        db.query(
            Producto.id,
            Producto.nombre,
            Producto.categoria,
            func.sum(PedidoItem.cantidad).label("unidades_vendidas"),
            func.sum(
                PedidoItem.cantidad * PedidoItem.precio_unitario_aplicado
            ).label("ingresos_totales"),
        )
        .join(Lote, Lote.producto_id == Producto.id)
        .join(PedidoItem, PedidoItem.lote_id == Lote.id)
        .join(Pedido, Pedido.id == PedidoItem.pedido_id)
        .filter(Pedido.estado.in_(["confirmado", "entregado"]))
    )

    if tienda_id:
        query = query.filter(Producto.tienda_id == tienda_id)

    resultados = (
        query.group_by(Producto.id, Producto.nombre, Producto.categoria)
        .order_by(func.sum(PedidoItem.cantidad).desc())
        .limit(limite)
        .all()
    )

    return [
        {
            "producto_id": r.id,
            "nombre": r.nombre,
            "categoria": r.categoria,
            "unidades_vendidas": r.unidades_vendidas,
            "ingresos_totales": float(r.ingresos_totales or 0),
        }
        for r in resultados
    ]


@router.get("/lotes-proximos-a-vencer")
def lotes_proximos_a_vencer(
    dias: int = 10,
    tienda_id: int | None = None,
    db: Session = Depends(get_db),
    _: Administrador = Depends(require_admin),
):
    """Retorna lotes disponibles que vencen en los próximos N días."""
    hoy = date.today()
    limite = hoy + timedelta(days=dias)

    query = (
        db.query(Lote, Producto)
        .join(Producto, Producto.id == Lote.producto_id)
        .filter(
            Lote.estado == "disponible",
            Lote.fecha_vencimiento >= hoy,
            Lote.fecha_vencimiento <= limite,
        )
    )

    if tienda_id:
        query = query.filter(Producto.tienda_id == tienda_id)

    resultados = query.order_by(Lote.fecha_vencimiento.asc()).all()

    return [
        {
            "lote_id": lote.id,
            "producto_id": prod.id,
            "nombre_producto": prod.nombre,
            "cantidad_disponible": lote.cantidad,
            "fecha_vencimiento": lote.fecha_vencimiento.isoformat(),
            "dias_restantes": (lote.fecha_vencimiento - hoy).days,
            "precio_lote": float(lote.precio_lote),
        }
        for lote, prod in resultados
    ]


@router.get("/ventas-por-periodo")
def ventas_por_periodo(
    fecha_desde: date = None,
    fecha_hasta: date = None,
    tienda_id: int | None = None,
    db: Session = Depends(get_db),
    _: Administrador = Depends(require_admin),
):
    """Retorna el resumen de ventas en un período de tiempo."""
    if fecha_desde is None:
        fecha_desde = date.today() - timedelta(days=30)
    if fecha_hasta is None:
        fecha_hasta = date.today()

    query = (
        db.query(
            func.count(Pedido.id).label("total_pedidos"),
            func.sum(Pedido.total).label("ingresos_totales"),
            func.avg(Pedido.total).label("ticket_promedio"),
        )
        .filter(
            Pedido.estado.in_(["confirmado", "entregado"]),
            Pedido.fecha_pedido >= fecha_desde,
            Pedido.fecha_pedido <= fecha_hasta,
        )
    )

    if tienda_id:
        query = query.filter(Pedido.tienda_id == tienda_id)

    resultado = query.first()

    return {
        "periodo": {
            "desde": fecha_desde.isoformat(),
            "hasta": fecha_hasta.isoformat(),
        },
        "total_pedidos": resultado.total_pedidos or 0,
        "ingresos_totales": float(resultado.ingresos_totales or 0),
        "ticket_promedio": float(resultado.ticket_promedio or 0),
    }
