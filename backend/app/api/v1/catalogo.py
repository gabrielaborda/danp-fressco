"""
Router: Catálogo público/autenticado
GET /productos       — con filtros
GET /productos/{id}
GET /lotes-disponibles — con descuento calculado
"""
from datetime import date
from decimal import Decimal
from typing import Annotated
from fastapi import APIRouter, Depends, Query
from sqlalchemy.orm import Session

from app.db.session import get_db
from app.models.lote import Lote
from app.models.producto import Producto
from app.models.tienda import Tienda
from app.schemas.lote import LoteConDescuento
from app.schemas.producto import ProductoResponse
from app.services.descuento_service import resolver_descuento_efectivo
from app.core.exceptions import NotFoundError

router = APIRouter(tags=["Catálogo"])


@router.get("/productos", response_model=list[ProductoResponse])
def listar_productos(
    categoria: str | None = None,
    tienda_id: int | None = None,
    busqueda: str | None = None,
    solo_con_descuento: bool = False,
    db: Session = Depends(get_db),
):
    """
    Catálogo de productos con filtros opcionales:
    - categoria, tienda_id, texto de búsqueda, solo_con_descuento
    """
    query = db.query(Producto).filter(Producto.activo == True)

    if categoria:
        query = query.filter(Producto.categoria.ilike(f"%{categoria}%"))
    if tienda_id:
        query = query.filter(Producto.tienda_id == tienda_id)
    if busqueda:
        query = query.filter(Producto.nombre.ilike(f"%{busqueda}%"))

    if solo_con_descuento:
        # Solo productos que tienen al menos un lote disponible con descuento
        query = query.join(Lote, Lote.producto_id == Producto.id).filter(
            Lote.estado == "disponible",
            Lote.fecha_vencimiento > date.today(),
        )

    return query.distinct().all()


@router.get("/productos/{producto_id}", response_model=ProductoResponse)
def obtener_producto(
    producto_id: int,
    db: Annotated[Session, Depends(get_db)],
):
    producto = db.get(Producto, producto_id)
    if producto is None or not producto.activo:
        raise NotFoundError(f"Producto #{producto_id} no encontrado")
    return producto


@router.get("/lotes-disponibles", response_model=list[LoteConDescuento])
def lotes_disponibles(
    tienda_id: int | None = None,
    categoria: str | None = None,
    solo_con_descuento: bool = False,
    db: Session = Depends(get_db),
):
    """
    Retorna lotes disponibles (no vencidos, con stock) enriquecidos con
    el descuento efectivo ya calculado (manual o automático).
    """
    hoy = date.today()

    query = (
        db.query(Lote)
        .join(Producto, Producto.id == Lote.producto_id)
        .join(Tienda, Tienda.id == Producto.tienda_id)
        .filter(
            Lote.estado == "disponible",
            Lote.fecha_vencimiento > hoy,
            Producto.activo == True,
            Tienda.activa == True,
        )
    )

    if tienda_id:
        query = query.filter(Producto.tienda_id == tienda_id)
    if categoria:
        query = query.filter(Producto.categoria.ilike(f"%{categoria}%"))

    lotes = query.all()

    resultado: list[LoteConDescuento] = []
    for lote in lotes:
        descuento_info = resolver_descuento_efectivo(db, lote)

        if solo_con_descuento and descuento_info["porcentaje_descuento"] == 0:
            continue

        resultado.append(
            LoteConDescuento(
                id=lote.id,
                producto_id=lote.producto_id,
                cantidad=lote.cantidad,
                cantidad_inicial=lote.cantidad_inicial,
                fecha_ingreso=lote.fecha_ingreso,
                fecha_vencimiento=lote.fecha_vencimiento,
                precio_lote=lote.precio_lote,
                estado=lote.estado,
                creado_en=lote.creado_en,
                precio_con_descuento=descuento_info["precio_con_descuento"],
                porcentaje_descuento=descuento_info["porcentaje_descuento"],
                dias_para_vencer=descuento_info["dias_para_vencer"],
                tiene_descuento_manual=descuento_info["tiene_descuento_manual"],
                nombre_producto=lote.producto.nombre,
                imagen_url=lote.producto.imagen_url,
                categoria=lote.producto.categoria,
                nombre_tienda=lote.producto.tienda.nombre,
            )
        )

    return resultado
