"""
Configuración centralizada con pydantic-settings.
Lee variables de entorno desde .env automáticamente.
"""
from functools import lru_cache
from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    model_config = SettingsConfigDict(
        env_file=".env",
        env_file_encoding="utf-8",
        case_sensitive=False,
        extra="ignore",
    )

    # ─── App ─────────────────────────────────────────────────────────────────
    APP_NAME: str = "Fressco API"
    APP_VERSION: str = "1.0.0"
    DEBUG: bool = False
    API_PREFIX: str = "/api/v1"

    # ─── Base de datos ────────────────────────────────────────────────────────
    DATABASE_URL: str = "sqlite:///./data/fressco.db"

    # ─── JWT ─────────────────────────────────────────────────────────────────
    SECRET_KEY: str = "dev-secret-key-cambia-esto-en-produccion"
    ALGORITHM: str = "HS256"
    ACCESS_TOKEN_EXPIRE_MINUTES: int = 60

    # ─── Descuentos automáticos ──────────────────────────────────────────────
    # Formato: "dias_limite:porcentaje" separado por coma.
    # Orden: de menor plazo a mayor (se evalúan de izquierda a derecha).
    # Default: ≤2 días → 60%, ≤5 días → 40%, ≤10 días → 20%, resto → 0%
    DISCOUNT_TIERS: str = "2:60,5:40,10:20"

    def get_discount_tiers(self) -> list[tuple[int, int]]:
        """
        Parsea DISCOUNT_TIERS en una lista ordenada de (dias_limite, porcentaje).
        Se ordenan de menor a mayor para evaluar primero el mayor descuento.
        """
        tiers = []
        for tier in self.DISCOUNT_TIERS.split(","):
            dias, porcentaje = tier.strip().split(":")
            tiers.append((int(dias), int(porcentaje)))
        # Menor límite de días primero → mayor descuento primero
        return sorted(tiers, key=lambda x: x[0])


@lru_cache()
def get_settings() -> Settings:
    return Settings()


settings = get_settings()
