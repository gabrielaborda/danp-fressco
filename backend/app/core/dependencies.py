"""
Dependencias de autenticación y autorización para usar con Depends().

Funciones disponibles:
- get_current_user  → cualquier usuario autenticado (admin o cliente)
- require_admin     → solo administradores
- require_cliente   → solo clientes activos
"""
from typing import Annotated
import jwt
from fastapi import Depends
from fastapi.security import HTTPBearer, HTTPAuthorizationCredentials
from sqlalchemy.orm import Session

from app.core.config import settings
from app.core.security import decode_token
from app.core.exceptions import UnauthorizedError, ForbiddenError
from app.db.session import get_db
from app.models.administrador import Administrador
from app.models.cliente import Cliente

# Bearer token extractor
bearer_scheme = HTTPBearer(auto_error=False)


def get_current_user(
    credentials: Annotated[HTTPAuthorizationCredentials | None, Depends(bearer_scheme)],
    db: Annotated[Session, Depends(get_db)],
) -> Administrador | Cliente:
    """
    Extrae y valida el JWT. Devuelve el usuario (admin o cliente) de la BD.
    Lanza 401 si el token es inválido, expirado o el usuario no existe.
    """
    if credentials is None:
        raise UnauthorizedError("Se requiere token de autenticación")

    try:
        payload = decode_token(credentials.credentials)
    except jwt.ExpiredSignatureError:
        raise UnauthorizedError("El token ha expirado")
    except jwt.InvalidTokenError:
        raise UnauthorizedError("Token inválido")

    user_id: int | None = payload.get("sub")
    rol: str | None = payload.get("rol")

    if user_id is None or rol is None:
        raise UnauthorizedError("Token mal formado")

    if rol == "admin":
        user = db.get(Administrador, int(user_id))
        if user is None or not user.activo:
            raise UnauthorizedError("Administrador no encontrado o inactivo")
    elif rol == "cliente":
        user = db.get(Cliente, int(user_id))
        if user is None:
            raise UnauthorizedError("Cliente no encontrado")
    else:
        raise UnauthorizedError("Rol desconocido en el token")

    return user


def require_admin(
    current_user: Annotated[Administrador | Cliente, Depends(get_current_user)],
) -> Administrador:
    """Solo permite acceso a administradores."""
    if not isinstance(current_user, Administrador) or current_user.rol != "admin":
        raise ForbiddenError("Se requiere rol de administrador")
    return current_user


def require_cliente(
    current_user: Annotated[Administrador | Cliente, Depends(get_current_user)],
) -> Cliente:
    """Solo permite acceso a clientes activos."""
    if not isinstance(current_user, Cliente) or current_user.rol != "cliente":
        raise ForbiddenError("Se requiere rol de cliente")
    if current_user.estado == "suspendido":
        raise ForbiddenError("Tu cuenta está suspendida. Contacta al soporte.")
    return current_user
