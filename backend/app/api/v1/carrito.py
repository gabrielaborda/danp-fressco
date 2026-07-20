"""
Router: Carrito (cliente autenticado)
GET /carrito
POST /carrito/items
PUT /carrito/items/{id}
DELETE /carrito/items/{id}
DELETE /carrito
"""
from decimal import Decimal
from typing import Annotated
from fastapi import APIRouter, Depends
from sqlalchemy.orm import Session

from app.core.dependencies import require_cliente
from app.db.session import get_db
from app.models.cliente import Cliente
from app.schemas.carrito import (
    CarritoItemCreate,
    CarritoItemUpdate,
    CarritoItemResponse,
    CarritoResponse,
)
from app.services import carrito_service

router = APIRouter(prefix="/carrito", tags=["Carrito"])


def _enriquecer_item(item) -> CarritoItemResponse:
    """Construye el schema de respuesta enriquecido con datos del producto."""
    return CarritoItemResponse(
        id=item.id,
        lote_id=item.lote_id,
        cantidad=item.cantidad,
        precio_aplicado=item.precio_aplicado,
        agregado_en=item.agregado_en,
        nombre_producto=item.lote.producto.nombre if item.lote and item.lote.producto else None,
        imagen_url=item.lote.producto.imagen_url if item.lote and item.lote.producto else None,
        subtotal=item.precio_aplicado * item.cantidad,
    )


@router.get("", response_model=CarritoResponse)
def ver_carrito(
    db: Annotated[Session, Depends(get_db)],
    cliente: Annotated[Cliente, Depends(require_cliente)],
):
    items = carrito_service.obtener_carrito(db, cliente)
    items_response = [_enriquecer_item(i) for i in items]
    total = sum(i.subtotal for i in items_response if i.subtotal)
    return CarritoResponse(items=items_response, total=total)


@router.post("/items", response_model=CarritoItemResponse, status_code=201)
def agregar_al_carrito(
    body: CarritoItemCreate,
    db: Annotated[Session, Depends(get_db)],
    cliente: Annotated[Cliente, Depends(require_cliente)],
):
    item = carrito_service.agregar_item(db, cliente, body.lote_id, body.cantidad)
    db.commit()
    db.refresh(item)
    return _enriquecer_item(item)


@router.put("/items/{item_id}", response_model=CarritoItemResponse)
def actualizar_item_carrito(
    item_id: int,
    body: CarritoItemUpdate,
    db: Annotated[Session, Depends(get_db)],
    cliente: Annotated[Cliente, Depends(require_cliente)],
):
    item = carrito_service.actualizar_item(db, cliente, item_id, body.cantidad)
    db.commit()
    db.refresh(item)
    return _enriquecer_item(item)


@router.delete("/items/{item_id}", status_code=204)
def eliminar_item_carrito(
    item_id: int,
    db: Annotated[Session, Depends(get_db)],
    cliente: Annotated[Cliente, Depends(require_cliente)],
):
    carrito_service.eliminar_item(db, cliente, item_id)
    db.commit()


@router.delete("", status_code=204)
def vaciar_carrito(
    db: Annotated[Session, Depends(get_db)],
    cliente: Annotated[Cliente, Depends(require_cliente)],
):
    carrito_service.vaciar_carrito(db, cliente)
    db.commit()
