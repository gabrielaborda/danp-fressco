"""
Servicio de Descuentos.

Responsabilidades:
1. Calcular el descuento automático de un lote según su proximidad a vencer.
2. Resolver el descuento efectivo de un lote (manual > automático).
3. Calcular el precio final con descuento.

La tabla de escalones es configurable desde la variable DISCOUNT_TIERS en .env.
"""
from datetime import date
from decimal import Decimal, ROUND_HALF_UP
from sqlalchemy.orm import Session

from app.core.config import settings
from app.models.lote import Lote
from app.models.descuento import Descuento


# ─── Helpers ─────────────────────────────────────────────────────────────────

def _dias_hasta_vencimiento(fecha_vencimiento: date) -> int:
    """Calcula los días restantes hasta la fecha de vencimiento."""
    delta = fecha_vencimiento - date.today()
    return delta.days


def calcular_porcentaje_automatico(fecha_vencimiento: date) -> int:
    """
    Determina el porcentaje de descuento automático basado en los escalones
    configurados en DISCOUNT_TIERS (leídos desde .env).

    Ejemplo con tiers por defecto (2:60, 5:40, 10:20):
      - dias <= 2  → 60%
      - dias <= 5  → 40%
      - dias <= 10 → 20%
      - dias > 10  → 0%
    """
    dias = _dias_hasta_vencimiento(fecha_vencimiento)

    if dias <= 0:
        # Ya venció o vence hoy
        return 0

    for dias_limite, porcentaje in settings.get_discount_tiers():
        if dias <= dias_limite:
            return porcentaje

    return 0  # Sin descuento automático


def _get_descuento_manual_activo(db: Session, lote_id: int) -> Descuento | None:
    """
    Busca el descuento manual activo vigente para un lote.
    Un descuento manual tiene precedencia absoluta sobre el automático.
    """
    hoy = date.today()
    return (
        db.query(Descuento)
        .filter(
            Descuento.lote_id == lote_id,
            Descuento.tipo == "manual_admin",
            Descuento.activo == True,
            Descuento.fecha_inicio <= hoy,
            Descuento.fecha_fin >= hoy,
        )
        .first()
    )


# ─── API pública del servicio ─────────────────────────────────────────────────

def calcular_precio_con_descuento(
    precio_base: Decimal,
    porcentaje: Decimal,
) -> Decimal:
    """Aplica un porcentaje de descuento al precio base."""
    factor = Decimal(1) - (porcentaje / Decimal(100))
    return (precio_base * factor).quantize(Decimal("0.01"), rounding=ROUND_HALF_UP)


def resolver_descuento_efectivo(
    db: Session,
    lote: Lote,
) -> dict:
    """
    Determina el descuento efectivo para un lote.

    Retorna un diccionario con:
    - porcentaje_descuento: Decimal
    - precio_con_descuento: Decimal
    - tiene_descuento_manual: bool
    - dias_para_vencer: int
    """
    dias = _dias_hasta_vencimiento(lote.fecha_vencimiento)

    # 1. Prioridad: descuento manual activo del admin
    descuento_manual = _get_descuento_manual_activo(db, lote.id)
    if descuento_manual:
        if descuento_manual.porcentaje is not None:
            porcentaje = descuento_manual.porcentaje
        else:
            # Convertir monto fijo a porcentaje equivalente
            porcentaje = (
                (descuento_manual.monto_fijo / lote.precio_lote) * Decimal(100)
            ).quantize(Decimal("0.01"), rounding=ROUND_HALF_UP)

        return {
            "porcentaje_descuento": porcentaje,
            "precio_con_descuento": calcular_precio_con_descuento(lote.precio_lote, porcentaje),
            "tiene_descuento_manual": True,
            "dias_para_vencer": dias,
        }

    # 2. Fallback: descuento automático por proximidad a vencimiento
    porcentaje_auto = Decimal(calcular_porcentaje_automatico(lote.fecha_vencimiento))
    return {
        "porcentaje_descuento": porcentaje_auto,
        "precio_con_descuento": calcular_precio_con_descuento(lote.precio_lote, porcentaje_auto),
        "tiene_descuento_manual": False,
        "dias_para_vencer": dias,
    }
