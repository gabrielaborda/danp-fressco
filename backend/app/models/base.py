"""
Base declarativa compartida por todos los modelos SQLAlchemy.
"""
from sqlalchemy.orm import DeclarativeBase


class Base(DeclarativeBase):
    """Clase base para todos los modelos ORM del proyecto."""
    pass
