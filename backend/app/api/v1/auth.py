"""
Router: Autenticación
- POST /auth/registro  — Registro de nuevos clientes
- POST /auth/login     — Login de clientes y admins (devuelve JWT)
"""
from typing import Annotated
from fastapi import APIRouter, Depends
from sqlalchemy.orm import Session

from app.core.security import hash_password, verify_password, create_access_token
from app.core.exceptions import ConflictError, UnauthorizedError
from app.db.session import get_db
from app.models.administrador import Administrador
from app.models.cliente import Cliente
from app.schemas.auth import LoginRequest, RegisterRequest, TokenResponse

router = APIRouter(prefix="/auth", tags=["Autenticación"])


@router.post("/registro", response_model=TokenResponse, status_code=201)
def registro_cliente(
    body: RegisterRequest,
    db: Annotated[Session, Depends(get_db)],
):
    """Registra un nuevo cliente y devuelve su JWT."""
    # Verificar email único
    if db.query(Cliente).filter(Cliente.email == body.email).first():
        raise ConflictError(f"El email '{body.email}' ya está registrado")

    cliente = Cliente(
        nombre=body.nombre,
        email=body.email,
        password_hash=hash_password(body.password),
        telefono=body.telefono,
    )
    db.add(cliente)
    db.commit()
    db.refresh(cliente)

    token = create_access_token({"sub": str(cliente.id), "rol": "cliente"})
    return TokenResponse(
        access_token=token,
        rol="cliente",
        nombre=cliente.nombre,
        id=cliente.id,
    )


@router.post("/login", response_model=TokenResponse)
def login(
    body: LoginRequest,
    db: Annotated[Session, Depends(get_db)],
):
    """
    Login unificado para clientes y administradores.
    Primero busca en clientes, luego en administradores.
    """
    # Buscar como cliente
    cliente = db.query(Cliente).filter(Cliente.email == body.email).first()
    if cliente and verify_password(body.password, cliente.password_hash):
        if cliente.estado == "suspendido":
            raise UnauthorizedError("Tu cuenta está suspendida")
        token = create_access_token({"sub": str(cliente.id), "rol": "cliente"})
        return TokenResponse(
            access_token=token,
            rol="cliente",
            nombre=cliente.nombre,
            id=cliente.id,
        )

    # Buscar como administrador
    admin = db.query(Administrador).filter(Administrador.email == body.email).first()
    if admin and verify_password(body.password, admin.password_hash):
        if not admin.activo:
            raise UnauthorizedError("La cuenta de administrador está inactiva")
        token = create_access_token({"sub": str(admin.id), "rol": "admin"})
        return TokenResponse(
            access_token=token,
            rol="admin",
            nombre=admin.nombre,
            id=admin.id,
        )

    raise UnauthorizedError("Email o contraseña incorrectos")
