"""
Router: Admin — Gestión de Clientes
GET /admin/clientes
PUT /admin/clientes/{id}    (suspender/activar/modificar)
DELETE /admin/clientes/{id}
"""
from typing import Annotated
from fastapi import APIRouter, Depends
from sqlalchemy.orm import Session

from app.core.dependencies import require_admin
from app.core.exceptions import NotFoundError
from app.db.session import get_db
from app.models.administrador import Administrador
from app.models.cliente import Cliente
from app.schemas.cliente import ClienteAdminUpdate, ClienteResponse

router = APIRouter(prefix="/admin/clientes", tags=["Admin — Clientes"])


@router.get("", response_model=list[ClienteResponse])
def listar_clientes(
    estado: str | None = None,
    db: Session = Depends(get_db),
    _: Administrador = Depends(require_admin),
):
    query = db.query(Cliente)
    if estado:
        query = query.filter(Cliente.estado == estado)
    return query.order_by(Cliente.fecha_registro.desc()).all()


@router.get("/{cliente_id}", response_model=ClienteResponse)
def obtener_cliente(
    cliente_id: int,
    db: Annotated[Session, Depends(get_db)],
    _: Annotated[Administrador, Depends(require_admin)],
):
    cliente = db.get(Cliente, cliente_id)
    if cliente is None:
        raise NotFoundError(f"Cliente #{cliente_id} no encontrado")
    return cliente


@router.put("/{cliente_id}", response_model=ClienteResponse)
def actualizar_cliente(
    cliente_id: int,
    body: ClienteAdminUpdate,
    db: Annotated[Session, Depends(get_db)],
    _: Annotated[Administrador, Depends(require_admin)],
):
    """Permite al admin suspender, activar o modificar datos de un cliente."""
    cliente = db.get(Cliente, cliente_id)
    if cliente is None:
        raise NotFoundError(f"Cliente #{cliente_id} no encontrado")

    for campo, valor in body.model_dump(exclude_unset=True).items():
        setattr(cliente, campo, valor)

    db.commit()
    db.refresh(cliente)
    return cliente


@router.delete("/{cliente_id}", status_code=204)
def eliminar_cliente(
    cliente_id: int,
    db: Annotated[Session, Depends(get_db)],
    _: Annotated[Administrador, Depends(require_admin)],
):
    cliente = db.get(Cliente, cliente_id)
    if cliente is None:
        raise NotFoundError(f"Cliente #{cliente_id} no encontrado")
    db.delete(cliente)
    db.commit()
