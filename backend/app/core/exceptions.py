"""
Excepciones personalizadas y handlers HTTP para FastAPI.
Devuelven siempre JSON consistente con el schema ErrorDetail.
"""
from fastapi import Request, status
from fastapi.responses import JSONResponse


# ─── Excepciones de dominio ───────────────────────────────────────────────────

class FresscoException(Exception):
    """Base para todas las excepciones de negocio del proyecto."""
    status_code: int = status.HTTP_500_INTERNAL_SERVER_ERROR
    code: str = "INTERNAL_ERROR"
    detail: str = "Error interno del servidor"

    def __init__(self, detail: str | None = None):
        self.detail = detail or self.__class__.detail
        super().__init__(self.detail)


class NotFoundError(FresscoException):
    status_code = status.HTTP_404_NOT_FOUND
    code = "NOT_FOUND"
    detail = "Recurso no encontrado"


class ConflictError(FresscoException):
    status_code = status.HTTP_409_CONFLICT
    code = "CONFLICT"
    detail = "Conflicto con el estado actual del recurso"


class BadRequestError(FresscoException):
    status_code = status.HTTP_400_BAD_REQUEST
    code = "BAD_REQUEST"
    detail = "Solicitud inválida"


class UnauthorizedError(FresscoException):
    status_code = status.HTTP_401_UNAUTHORIZED
    code = "UNAUTHORIZED"
    detail = "No autenticado o credenciales inválidas"


class ForbiddenError(FresscoException):
    status_code = status.HTTP_403_FORBIDDEN
    code = "FORBIDDEN"
    detail = "No tiene permisos para realizar esta acción"


class StockInsuficienteError(ConflictError):
    code = "STOCK_INSUFICIENTE"
    detail = "Stock insuficiente para completar la operación"


class LoteVencidoError(BadRequestError):
    code = "LOTE_VENCIDO"
    detail = "El lote está vencido o no disponible"


class CarritoVacioError(BadRequestError):
    code = "CARRITO_VACIO"
    detail = "El carrito está vacío"


# ─── Handlers globales ────────────────────────────────────────────────────────

def _error_response(status_code: int, code: str, detail: str) -> JSONResponse:
    return JSONResponse(
        status_code=status_code,
        content={"success": False, "code": code, "detail": detail},
    )


async def fressco_exception_handler(
    request: Request, exc: FresscoException
) -> JSONResponse:
    return _error_response(exc.status_code, exc.code, exc.detail)


async def validation_exception_handler(request: Request, exc: Exception) -> JSONResponse:
    from fastapi.exceptions import RequestValidationError
    errors = exc.errors() if hasattr(exc, "errors") else [str(exc)]
    return JSONResponse(
        status_code=status.HTTP_422_UNPROCESSABLE_ENTITY,
        content={
            "success": False,
            "code": "VALIDATION_ERROR",
            "detail": "Error de validación en los datos enviados",
            "extra": {"errors": errors},
        },
    )


async def generic_exception_handler(request: Request, exc: Exception) -> JSONResponse:
    return _error_response(
        status.HTTP_500_INTERNAL_SERVER_ERROR,
        "INTERNAL_ERROR",
        "Error interno del servidor",
    )
