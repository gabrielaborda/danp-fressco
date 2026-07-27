"""
Router: Autenticación
- POST /auth/registro  — Registro de nuevos clientes
- POST /auth/login     — Login de clientes y admins (devuelve JWT)
- POST /auth/admin/registro — Crear un administrador autenticado por otro admin
"""
from typing import Annotated
from fastapi import APIRouter, Depends
from sqlalchemy.orm import Session

from app.core.config import settings
from app.core.security import hash_password, verify_password, create_access_token
from app.core.exceptions import ConflictError, ForbiddenError, NotFoundError, UnauthorizedError
from app.core.dependencies import require_admin
from app.db.session import get_db
from app.models.administrador import Administrador
from app.models.cliente import Cliente
from app.models.tienda import Tienda
from app.schemas.administrador import AdministradorCreate, AdministradorCreateDirect, AdministradorResponse
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


@router.post("/admin/registro", response_model=AdministradorResponse, status_code=201)
def crear_admin(
    body: AdministradorCreate,
    db: Annotated[Session, Depends(get_db)],
    _: Annotated[Administrador, Depends(require_admin)],
):
    """Crea un nuevo administrador a partir de un token de otro admin."""
    if db.query(Administrador).filter(Administrador.email == body.email).first():
        raise ConflictError(f"El email '{body.email}' ya está registrado")

    tienda = db.get(Tienda, body.tienda_id)
    if tienda is None:
        raise NotFoundError("La tienda indicada no existe")

    admin = Administrador(
        tienda_id=body.tienda_id,
        nombre=body.nombre,
        email=str(body.email),
        password_hash=hash_password(body.password),
        rol="admin",
        activo=True,
    )
    db.add(admin)
    db.commit()
    db.refresh(admin)

    return AdministradorResponse(
        id=admin.id,
        tienda_id=admin.tienda_id,
        nombre=admin.nombre,
        email=admin.email,
        rol=admin.rol,
        activo=admin.activo,
    )


@router.post("/admin/registro-directo", response_model=AdministradorResponse, status_code=201)
def crear_admin_directo(
    body: AdministradorCreateDirect,
    db: Annotated[Session, Depends(get_db)],
):
    """Crea un administrador desde Swagger en modo desarrollo sin token."""
    if not settings.DEBUG:
        raise ForbiddenError("Este endpoint solo está disponible en modo desarrollo")

    if db.query(Administrador).filter(Administrador.email == str(body.email)).first():
        raise ConflictError(f"El email '{body.email}' ya está registrado")

    tienda = None
    if body.tienda_id is not None:
        tienda = db.get(Tienda, body.tienda_id)
        if tienda is None:
            raise NotFoundError("La tienda indicada no existe")
    else:
        tienda = db.query(Tienda).first()
        if tienda is None:
            tienda = Tienda(
                nombre=body.tienda_nombre or "Tienda creada por admin",
                direccion=body.tienda_direccion or "Dirección pendiente",
                telefono=body.tienda_telefono or "000000000",
                email_contacto=body.tienda_email_contacto or str(body.email),
                descripcion=body.tienda_descripcion or "Tienda creada desde Swagger",
                activa=True,
            )
            db.add(tienda)
            db.flush()

    admin = Administrador(
        tienda_id=tienda.id,
        nombre=body.nombre,
        email=str(body.email),
        password_hash=hash_password(body.password),
        rol="admin",
        activo=True,
    )
    db.add(admin)
    db.commit()
    db.refresh(admin)

    return AdministradorResponse(
        id=admin.id,
        tienda_id=admin.tienda_id,
        nombre=admin.nombre,
        email=admin.email,
        rol=admin.rol,
        activo=admin.activo,
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
