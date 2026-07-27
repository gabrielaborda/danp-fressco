package com.example.danpfressco.ui.util

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import java.time.LocalDate
import java.time.temporal.ChronoUnit

fun calcularPorcentajeDescuento(precioOriginal: Double, precioDescuento: Double): Double {
    if (precioOriginal <= 0.0) return 0.0
    return ((precioOriginal - precioDescuento) / precioOriginal) * 100.0
}

fun formatearPrecio(precio: Double): String {
    return "S/ ${String.format("%.2f", precio)}"
}

fun calcularDiasRestantes(fechaVencimiento: LocalDate): Long {
    return ChronoUnit.DAYS.between(LocalDate.now(), fechaVencimiento)
}

fun textoVencimiento(diasRestantes: Long): String {
    return when {
        diasRestantes < 0 -> "Vencido"
        diasRestantes == 0L -> "Vence hoy"
        diasRestantes == 1L -> "Vence mañana"
        else -> "Vence en $diasRestantes días"
    }
}

@Composable
fun colorVencimiento(diasRestantes: Long): Color {
    return when {
        diasRestantes <= 1 -> MaterialTheme.colorScheme.error
        diasRestantes <= 3 -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.primary
    }
}
