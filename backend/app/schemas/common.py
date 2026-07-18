"""
Schemas comunes: respuesta genérica, paginación y mensajes de error.
"""
from typing import Any, Generic, TypeVar
from pydantic import BaseModel

T = TypeVar("T")


class APIResponse(BaseModel, Generic[T]):
    """Envoltorio estándar para todas las respuestas exitosas."""
    success: bool = True
    data: T
    message: str | None = None


class PaginatedResponse(BaseModel, Generic[T]):
    """Respuesta paginada genérica."""
    success: bool = True
    data: list[T]
    total: int
    page: int
    page_size: int
    total_pages: int


class MessageResponse(BaseModel):
    """Respuesta simple de mensaje."""
    success: bool = True
    message: str


class ErrorDetail(BaseModel):
    """Detalle de error estándar."""
    success: bool = False
    code: str
    detail: str
    extra: dict[str, Any] | None = None
