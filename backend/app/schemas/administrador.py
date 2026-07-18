"""
Schemas de Administrador.
"""
from pydantic import BaseModel, EmailStr


class AdministradorCreate(BaseModel):
    tienda_id: int
    nombre: str
    email: EmailStr
    password: str


class AdministradorUpdate(BaseModel):
    nombre: str | None = None
    email: EmailStr | None = None
    password: str | None = None
    activo: bool | None = None


class AdministradorResponse(BaseModel):
    id: int
    tienda_id: int
    nombre: str
    email: str
    rol: str
    activo: bool

    model_config = {"from_attributes": True}
