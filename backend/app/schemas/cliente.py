"""
Schemas de Cliente.
"""
from datetime import datetime
from pydantic import BaseModel, EmailStr


class ClienteCreate(BaseModel):
    nombre: str
    email: EmailStr
    password: str
    telefono: str | None = None


class ClienteUpdate(BaseModel):
    nombre: str | None = None
    telefono: str | None = None


class ClienteAdminUpdate(BaseModel):
    """Para uso exclusivo del admin: puede cambiar el estado."""
    nombre: str | None = None
    telefono: str | None = None
    estado: str | None = None  # activo | suspendido


class ClienteResponse(BaseModel):
    id: int
    nombre: str
    email: str
    rol: str
    telefono: str | None
    fecha_registro: datetime
    estado: str

    model_config = {"from_attributes": True}
