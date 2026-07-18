"""
Router: Admin — Gestión de Lotes y Descuentos
GET/POST/PUT/DELETE /admin/lotes
PUT /admin/lotes/{id}/descuento
GET /admin/lotes/{id}/historial-descuentos
"""
from datetime import date
from typing import Annotated
from fastapi import APIRouter, Depends
from sqlalchemy.orm import Session

from app.core.dependencies import require_admin
from app.core.exceptions import NotFoundError, BadRequestError
from app.db.session import get_db
from app.models.administrador import Administrador
from app.models.lote import Lote
from app.models.descuento import Descuento
from app.models.producto import Producto
from app.schemas.lote import LoteCreate, LoteUpdate, LoteResponse
from app.schemas.descuento import DescuentoManualCreate, DescuentoResponse

router = APIRouter(prefix="/admin/lotes", tags=["Admin — Lotes"])


@router.get("", response_model=list[LoteResponse])
def listar_lotes(
    producto_id: int | None = None,
    estado: str | None = None,
    db: Session = Depends(get_db),
    _: Administrador = Depends(require_admin),
):
    query = db.query(Lote)
    if producto_id:
        query = query.filter(Lote.producto_id == producto_id)
    if estado:
        query = query.filter(Lote.estado == estado)
    return query.all()


@router.post("", response_model=LoteResponse, status_code=201)
def crear_lote(
    body: LoteCreate,
    db: Annotated[Session, Depends(get_db)],
    _: Annotated[Administrador, Depends(require_admin)],
):
    producto = db.get(Producto, body.producto_id)
    if producto is None:
        raise NotFoundError(f"Producto #{body.producto_id} no encontrado")

    if body.fecha_vencimiento <= date.today():
        raise BadRequestError("La fecha de vencimiento debe ser futura")

    lote_data = body.model_dump()
    lote = Lote(**lote_data, cantidad_inicial=body.cantidad)
    db.add(lote)
    db.commit()
    db.refresh(lote)
    return lote


@router.get("/{lote_id}", response_model=LoteResponse)
def obtener_lote(
    lote_id: int,
    db: Annotated[Session, Depends(get_db)],
    _: Annotated[Administrador, Depends(require_admin)],
):
    lote = db.get(Lote, lote_id)
    if lote is None:
        raise NotFoundError(f"Lote #{lote_id} no encontrado")
    return lote


@router.put("/{lote_id}", response_model=LoteResponse)
def actualizar_lote(
    lote_id: int,
    body: LoteUpdate,
    db: Annotated[Session, Depends(get_db)],
    _: Annotated[Administrador, Depends(require_admin)],
):
    lote = db.get(Lote, lote_id)
    if lote is None:
        raise NotFoundError(f"Lote #{lote_id} no encontrado")

    for campo, valor in body.model_dump(exclude_unset=True).items():
        setattr(lote, campo, valor)

    db.commit()
    db.refresh(lote)
    return lote


@router.delete("/{lote_id}", status_code=204)
def eliminar_lote(
    lote_id: int,
    db: Annotated[Session, Depends(get_db)],
    _: Annotated[Administrador, Depends(require_admin)],
):
    lote = db.get(Lote, lote_id)
    if lote is None:
        raise NotFoundError(f"Lote #{lote_id} no encontrado")
    # Baja lógica
    lote.estado = "dado_de_baja"
    db.commit()


@router.put("/{lote_id}/descuento", response_model=DescuentoResponse, status_code=201)
def sobreescribir_descuento(
    lote_id: int,
    body: DescuentoManualCreate,
    db: Annotated[Session, Depends(get_db)],
    _: Annotated[Administrador, Depends(require_admin)],
):
    """
    Sobreescribe el descuento de un lote con un descuento manual.
    Desactiva cualquier descuento manual previo antes de crear el nuevo.
    """
    lote = db.get(Lote, lote_id)
    if lote is None:
        raise NotFoundError(f"Lote #{lote_id} no encontrado")

    # Desactivar descuentos manuales anteriores
    db.query(Descuento).filter(
        Descuento.lote_id == lote_id,
        Descuento.tipo == "manual_admin",
        Descuento.activo == True,
    ).update({"activo": False})

    descuento = Descuento(
        lote_id=lote_id,
        tipo="manual_admin",
        porcentaje=body.porcentaje,
        monto_fijo=body.monto_fijo,
        fecha_inicio=body.fecha_inicio,
        fecha_fin=body.fecha_fin,
        descripcion=body.descripcion,
        activo=True,
    )
    db.add(descuento)
    db.commit()
    db.refresh(descuento)
    return descuento


@router.get("/{lote_id}/historial-descuentos", response_model=list[DescuentoResponse])
def historial_descuentos(
    lote_id: int,
    db: Annotated[Session, Depends(get_db)],
    _: Annotated[Administrador, Depends(require_admin)],
):
    """Retorna el historial completo de descuentos (activos e inactivos) de un lote."""
    lote = db.get(Lote, lote_id)
    if lote is None:
        raise NotFoundError(f"Lote #{lote_id} no encontrado")

    return (
        db.query(Descuento)
        .filter(Descuento.lote_id == lote_id)
        .order_by(Descuento.creado_en.desc())
        .all()
    )
