package com.example.danpfressco.data.model

/**
 * Métodos de pago disponibles en la pasarela simulada.
 * Si en el futuro se integra un backend real de pagos, solo se amplía este enum
 * sin tocar el ViewModel ni la UI (Open/Closed Principle).
 */
enum class MetodoPago {
    TARJETA,
    YAPE_PLIN
}
