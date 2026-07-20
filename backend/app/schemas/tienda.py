"""
Schemas de Tienda.
"""
from pydantic import BaseModel, EmailStr


class TiendaBase(BaseModel):
    nombre: str
    direccion: str
    telefono: str | None = None
    email_contacto: EmailStr | None = None
    descripcion: str | None = None
    logo_url: str | None = None


class TiendaCreate(TiendaBase):
    pass


class TiendaUpdate(BaseModel):
    nombre: str | None = None
    direccion: str | None = None
    telefono: str | None = None
    email_contacto: EmailStr | None = None
    descripcion: str | None = None
    logo_url: str | None = None
    activa: bool | None = None


class TiendaResponse(TiendaBase):
    id: int
    activa: bool

    model_config = {"from_attributes": True}
