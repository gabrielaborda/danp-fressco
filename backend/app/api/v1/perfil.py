"""
Router: Perfil del cliente autenticado
GET /perfil
PUT /perfil
"""
from typing import Annotated
from fastapi import APIRouter, Depends
from sqlalchemy.orm import Session

from app.core.dependencies import require_cliente
from app.core.security import hash_password
from app.db.session import get_db
from app.models.cliente import Cliente
from app.schemas.cliente import ClienteUpdate, ClienteResponse

router = APIRouter(prefix="/perfil", tags=["Perfil"])


@router.get("", response_model=ClienteResponse)
def ver_perfil(
    cliente: Annotated[Cliente, Depends(require_cliente)],
):
    return cliente


@router.put("", response_model=ClienteResponse)
def actualizar_perfil(
    body: ClienteUpdate,
    db: Annotated[Session, Depends(get_db)],
    cliente: Annotated[Cliente, Depends(require_cliente)],
):
    for campo, valor in body.model_dump(exclude_unset=True).items():
        setattr(cliente, campo, valor)

    db.commit()
    db.refresh(cliente)
    return cliente
