"""
Router: Admin — Gestión de Tiendas
GET/POST/PUT/DELETE /admin/tiendas
"""
from typing import Annotated
from fastapi import APIRouter, Depends
from sqlalchemy.orm import Session

from app.core.dependencies import require_admin
from app.core.exceptions import NotFoundError, ConflictError
from app.db.session import get_db
from app.models.administrador import Administrador
from app.models.tienda import Tienda
from app.schemas.tienda import TiendaCreate, TiendaUpdate, TiendaResponse

router = APIRouter(prefix="/admin/tiendas", tags=["Admin — Tiendas"])


@router.get("", response_model=list[TiendaResponse])
def listar_tiendas(
    db: Annotated[Session, Depends(get_db)],
    _: Annotated[Administrador, Depends(require_admin)],
):
    return db.query(Tienda).all()


@router.post("", response_model=TiendaResponse, status_code=201)
def crear_tienda(
    body: TiendaCreate,
    db: Annotated[Session, Depends(get_db)],
    _: Annotated[Administrador, Depends(require_admin)],
):
    tienda = Tienda(**body.model_dump())
    db.add(tienda)
    db.commit()
    db.refresh(tienda)
    return tienda


@router.get("/{tienda_id}", response_model=TiendaResponse)
def obtener_tienda(
    tienda_id: int,
    db: Annotated[Session, Depends(get_db)],
    _: Annotated[Administrador, Depends(require_admin)],
):
    tienda = db.get(Tienda, tienda_id)
    if tienda is None:
        raise NotFoundError(f"Tienda #{tienda_id} no encontrada")
    return tienda


@router.put("/{tienda_id}", response_model=TiendaResponse)
def actualizar_tienda(
    tienda_id: int,
    body: TiendaUpdate,
    db: Annotated[Session, Depends(get_db)],
    _: Annotated[Administrador, Depends(require_admin)],
):
    tienda = db.get(Tienda, tienda_id)
    if tienda is None:
        raise NotFoundError(f"Tienda #{tienda_id} no encontrada")

    for campo, valor in body.model_dump(exclude_unset=True).items():
        setattr(tienda, campo, valor)

    db.commit()
    db.refresh(tienda)
    return tienda


@router.delete("/{tienda_id}", status_code=204)
def eliminar_tienda(
    tienda_id: int,
    db: Annotated[Session, Depends(get_db)],
    _: Annotated[Administrador, Depends(require_admin)],
):
    tienda = db.get(Tienda, tienda_id)
    if tienda is None:
        raise NotFoundError(f"Tienda #{tienda_id} no encontrada")
    db.delete(tienda)
    db.commit()
