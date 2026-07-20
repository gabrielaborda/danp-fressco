"""
Sesión de base de datos y engine de SQLAlchemy.

Soporta PostgreSQL (producción) y SQLite (desarrollo/tests) transparentemente
a través de la variable de entorno DATABASE_URL.
"""
from sqlalchemy import create_engine, event
from sqlalchemy.orm import sessionmaker
from app.core.config import settings

# ─── Engine ──────────────────────────────────────────────────────────────────
connect_args: dict = {}

if settings.DATABASE_URL.startswith("sqlite"):
    # SQLite requiere check_same_thread=False para uso con FastAPI/threads
    connect_args["check_same_thread"] = False

engine = create_engine(
    settings.DATABASE_URL,
    connect_args=connect_args,
    pool_pre_ping=True,          # Verifica la conexión antes de usarla
    echo=settings.DEBUG,         # Loguea SQL cuando DEBUG=true
)

# Habilitar foreign keys en SQLite (desactivadas por defecto)
if settings.DATABASE_URL.startswith("sqlite"):
    @event.listens_for(engine, "connect")
    def set_sqlite_pragma(dbapi_connection, connection_record):
        cursor = dbapi_connection.cursor()
        cursor.execute("PRAGMA foreign_keys=ON")
        cursor.close()

# ─── Session Factory ─────────────────────────────────────────────────────────
SessionLocal = sessionmaker(
    autocommit=False,
    autoflush=False,
    bind=engine,
)


# ─── Dependencia FastAPI ──────────────────────────────────────────────────────
def get_db():
    """
    Generador de sesión de base de datos para usar como dependencia (Depends).
    Garantiza el cierre de la sesión al finalizar la request.
    """
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()
