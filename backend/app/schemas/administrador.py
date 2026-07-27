"""
Schemas de Administrador.
"""
from pydantic import BaseModel, EmailStr


class AdministradorCreate(BaseModel):
    tienda_id: int
    nombre: str
    email: EmailStr
    password: str


class AdministradorCreateDirect(BaseModel):
    nombre: str
    email: EmailStr
    password: str
    tienda_id: int | None = None
    tienda_nombre: str | None = None
    tienda_direccion: str | None = None
    tienda_telefono: str | None = None
    tienda_email_contacto: str | None = None
    tienda_descripcion: str | None = None


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
