"""
Fressco API — Punto de entrada principal de FastAPI.

Registra todos los routers, middlewares y handlers de error.
"""
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from fastapi.exceptions import RequestValidationError

from app.core.config import settings
from app.core.exceptions import (
    FresscoException,
    fressco_exception_handler,
    validation_exception_handler,
    generic_exception_handler,
)

# ─── Importar routers ────────────────────────────────────────────────────────
from app.api.v1 import auth
from app.api.v1 import admin_tiendas
from app.api.v1 import admin_productos
from app.api.v1 import admin_lotes
from app.api.v1 import admin_clientes
from app.api.v1 import admin_pedidos
from app.api.v1 import admin_reportes
from app.api.v1 import catalogo
from app.api.v1 import carrito
from app.api.v1 import pedidos
from app.api.v1 import perfil

# ─── Aplicación ─────────────────────────────────────────────────────────────
app = FastAPI(
    title=settings.APP_NAME,
    version=settings.APP_VERSION,
    description=(
        "API REST de **Fressco** — plataforma que conecta clientes con tiendas "
        "que ofrecen lotes de productos próximos a vencer con descuentos especiales."
    ),
    docs_url="/docs",
    redoc_url="/redoc",
    openapi_url="/openapi.json",
)

# ─── CORS ────────────────────────────────────────────────────────────────────
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],  # Ajustar en producción con dominios específicos
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# ─── Exception Handlers ──────────────────────────────────────────────────────
app.add_exception_handler(FresscoException, fressco_exception_handler)
app.add_exception_handler(RequestValidationError, validation_exception_handler)
app.add_exception_handler(Exception, generic_exception_handler)

# ─── Routers ─────────────────────────────────────────────────────────────────
PREFIX = settings.API_PREFIX

app.include_router(auth.router, prefix=PREFIX)
app.include_router(admin_tiendas.router, prefix=PREFIX)
app.include_router(admin_productos.router, prefix=PREFIX)
app.include_router(admin_lotes.router, prefix=PREFIX)
app.include_router(admin_clientes.router, prefix=PREFIX)
app.include_router(admin_pedidos.router, prefix=PREFIX)
app.include_router(admin_reportes.router, prefix=PREFIX)
app.include_router(catalogo.router, prefix=PREFIX)
app.include_router(carrito.router, prefix=PREFIX)
app.include_router(pedidos.router, prefix=PREFIX)
app.include_router(perfil.router, prefix=PREFIX)


# ─── Health check ────────────────────────────────────────────────────────────
@app.get("/health", tags=["Sistema"])
def health_check():
    return {
        "status": "ok",
        "app": settings.APP_NAME,
        "version": settings.APP_VERSION,
    }
