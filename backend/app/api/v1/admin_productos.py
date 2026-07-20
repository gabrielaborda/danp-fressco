"""
Router: Admin — Gestión de Productos
GET/POST/PUT/DELETE /admin/productos
"""
from typing import Annotated
from fastapi import APIRouter, Depends
from sqlalchemy.orm import Session

from app.core.dependencies import require_admin
from app.core.exceptions import NotFoundError
from app.db.session import get_db
from app.models.administrador import Administrador
from app.models.producto import Producto
from app.models.tienda import Tienda
from app.schemas.producto import ProductoCreate, ProductoUpdate, ProductoResponse

router = APIRouter(prefix="/admin/productos", tags=["Admin — Productos"])


@router.get("", response_model=list[ProductoResponse])
def listar_productos(
    tienda_id: int | None = None,
    db: Session = Depends(get_db),
    _: Administrador = Depends(require_admin),
):
    query = db.query(Producto)
    if tienda_id:
        query = query.filter(Producto.tienda_id == tienda_id)
    return query.all()


@router.post("", response_model=ProductoResponse, status_code=201)
def crear_producto(
    body: ProductoCreate,
    db: Annotated[Session, Depends(get_db)],
    _: Annotated[Administrador, Depends(require_admin)],
):
    tienda = db.get(Tienda, body.tienda_id)
    if tienda is None:
        raise NotFoundError(f"Tienda #{body.tienda_id} no encontrada")

    producto = Producto(**body.model_dump())
    db.add(producto)
    db.commit()
    db.refresh(producto)
    return producto


@router.get("/{producto_id}", response_model=ProductoResponse)
def obtener_producto(
    producto_id: int,
    db: Annotated[Session, Depends(get_db)],
    _: Annotated[Administrador, Depends(require_admin)],
):
    producto = db.get(Producto, producto_id)
    if producto is None:
        raise NotFoundError(f"Producto #{producto_id} no encontrado")
    return producto


@router.put("/{producto_id}", response_model=ProductoResponse)
def actualizar_producto(
    producto_id: int,
    body: ProductoUpdate,
    db: Annotated[Session, Depends(get_db)],
    _: Annotated[Administrador, Depends(require_admin)],
):
    producto = db.get(Producto, producto_id)
    if producto is None:
        raise NotFoundError(f"Producto #{producto_id} no encontrado")

    for campo, valor in body.model_dump(exclude_unset=True).items():
        setattr(producto, campo, valor)

    db.commit()
    db.refresh(producto)
    return producto


@router.delete("/{producto_id}", status_code=204)
def eliminar_producto(
    producto_id: int,
    db: Annotated[Session, Depends(get_db)],
    _: Annotated[Administrador, Depends(require_admin)],
):
    producto = db.get(Producto, producto_id)
    if producto is None:
        raise NotFoundError(f"Producto #{producto_id} no encontrado")
    db.delete(producto)
    db.commit()
