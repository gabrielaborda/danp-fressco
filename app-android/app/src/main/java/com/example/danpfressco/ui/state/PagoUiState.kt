package com.example.danpfressco.ui.state

import com.example.danpfressco.data.model.MetodoPago

/**
 * Estado de la pantalla Pasarela de Pago.
 *
 * Sigue el mismo patrón que [FormularioPedidoUiState]:
 * - Campos de entrada con su valor actual
 * - Estado de la operación como sealed class ([EstadoPago])
 * - Separación clara entre estado de UI y estado de negocio
 */
data class PagoUiState(
    // Método seleccionado por el usuario
    val metodoPago: MetodoPago = MetodoPago.TARJETA,

    // Campos simulados de tarjeta (sin validación Luhn real)
    val numeroTarjeta: String = "",
    val vencimiento: String = "",
    val cvv: String = "",

    // Errores de formato básico por campo (null = sin error)
    val numeroTarjetaError: String? = null,
    val vencimientoError: String? = null,
    val cvvError: String? = null,

    // Estado de la operación de pago
    val estadoPago: EstadoPago = EstadoPago.Idle
)

/**
 * Estados posibles del proceso de pago.
 * Diseñado como sealed class para exhaustividad en el when() de la UI.
 */
sealed class EstadoPago {
    object Idle : EstadoPago()
    object Procesando : EstadoPago()
    object Exito : EstadoPago()
    data class Error(val mensaje: String) : EstadoPago()
}
