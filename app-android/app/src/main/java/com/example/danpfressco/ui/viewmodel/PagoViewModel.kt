package com.example.danpfressco.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.danpfressco.data.model.ItemCarrito
import com.example.danpfressco.data.model.MetodoPago
import com.example.danpfressco.data.repository.CarritoRepository
import com.example.danpfressco.data.repository.PagoRepository
import com.example.danpfressco.data.repository.PedidoRepository
import com.example.danpfressco.ui.state.EstadoPago
import com.example.danpfressco.ui.state.PagoUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel de la pantalla PasarelaPago.
 *
 * Depende únicamente de interfaces ([PagoRepository], [PedidoRepository],
 * [CarritoRepository]) — nunca de implementaciones concretas (DIP).
 *
 * Recibe los datos de contacto del pedido vía [SavedStateHandle] (argumentos
 * de navegación) y calcula el monto sumando los precios actuales del carrito.
 *
 * Flujo de pago exitoso:
 *   procesarPago() → crearPedido() → vaciarCarrito() → EstadoPago.Exito
 */
@HiltViewModel
class PagoViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val pagoRepository: PagoRepository,
    private val pedidoRepository: PedidoRepository,
    private val carritoRepository: CarritoRepository
) : ViewModel() {

    // Argumentos de navegación — pasados desde FormularioPedidoScreen
    val nombreContacto: String = checkNotNull(savedStateHandle["nombre"])
    val telefonoContacto: String = checkNotNull(savedStateHandle["telefono"])
    val horarioRecogida: String = checkNotNull(savedStateHandle["horario"])

    private val _uiState = MutableStateFlow(PagoUiState())
    val uiState: StateFlow<PagoUiState> = _uiState.asStateFlow()

    /** Items vigentes del carrito — la fuente de verdad del monto a cobrar. */
    val itemsCarrito: StateFlow<List<ItemCarrito>> = carritoRepository.items
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    // ─── Handlers de cambio de método y campos ───────────────────────────────

    fun onMetodoPagoChanged(metodo: MetodoPago) {
        _uiState.update {
            it.copy(
                metodoPago = metodo,
                // Limpiar errores al cambiar método
                numeroTarjetaError = null,
                vencimientoError = null,
                cvvError = null,
                estadoPago = EstadoPago.Idle
            )
        }
    }

    fun onNumeroTarjetaChanged(numero: String) {
        // Solo dígitos, máximo 16
        val limpio = numero.filter { it.isDigit() }.take(16)
        _uiState.update {
            it.copy(
                numeroTarjeta = limpio,
                numeroTarjetaError = null
            )
        }
    }

    fun onVencimientoChanged(vencimiento: String) {
        // Formato MM/YY automático
        val digitos = vencimiento.filter { it.isDigit() }.take(4)
        val formateado = if (digitos.length > 2) {
            "${digitos.take(2)}/${digitos.drop(2)}"
        } else {
            digitos
        }
        _uiState.update {
            it.copy(
                vencimiento = formateado,
                vencimientoError = null
            )
        }
    }

    fun onCvvChanged(cvv: String) {
        val limpio = cvv.filter { it.isDigit() }.take(3)
        _uiState.update {
            it.copy(
                cvv = limpio,
                cvvError = null
            )
        }
    }

    // ─── Confirmación del pago ───────────────────────────────────────────────

    /**
     * Ejecuta el flujo completo de pago:
     * 1. Valida campos de tarjeta si aplica.
     * 2. Calcula el monto desde los ítems actuales del carrito.
     * 3. Delega el procesamiento a [PagoRepository].
     * 4. Si éxito: crea el pedido y vacía el carrito.
     * 5. Actualiza [estadoPago] según el resultado.
     */
    fun confirmarPago() {
        val current = _uiState.value

        // Validación de campos de tarjeta (solo formato básico, no Luhn)
        if (current.metodoPago == MetodoPago.TARJETA) {
            val numError = if (current.numeroTarjeta.length < 13) "Ingresa un número de tarjeta válido" else null
            val vencError = if (!current.vencimiento.matches(Regex("\\d{2}/\\d{2}"))) "Formato MM/AA" else null
            val cvvError = if (current.cvv.length < 3) "CVV inválido" else null

            if (numError != null || vencError != null || cvvError != null) {
                _uiState.update {
                    it.copy(
                        numeroTarjetaError = numError,
                        vencimientoError = vencError,
                        cvvError = cvvError
                    )
                }
                return
            }
        }

        _uiState.update { it.copy(estadoPago = EstadoPago.Procesando) }

        viewModelScope.launch {
            // Ajuste #2: monto calculado desde el carrito vigente
            val items = itemsCarrito.value
            val monto = items.sumOf { it.oferta.lote.precioDescuento * it.cantidad }

            val resultado = pagoRepository.procesarPago(
                metodo = current.metodoPago,
                monto = monto
            )

            resultado.fold(
                onSuccess = {
                    // Pago exitoso → crear pedido → vaciar carrito
                    val pedidoResult = pedidoRepository.crearPedido(
                        items = items,
                        nombreContacto = nombreContacto,
                        telefonoContacto = telefonoContacto,
                        horarioRecogida = horarioRecogida
                    )

                    pedidoResult.fold(
                        onSuccess = {
                            carritoRepository.vaciarCarrito()
                            _uiState.update { it.copy(estadoPago = EstadoPago.Exito) }
                        },
                        onFailure = { error ->
                            _uiState.update {
                                it.copy(
                                    estadoPago = EstadoPago.Error(
                                        error.message ?: "Error al registrar el pedido"
                                    )
                                )
                            }
                        }
                    )
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            estadoPago = EstadoPago.Error(
                                error.message ?: "El pago no pudo procesarse"
                            )
                        )
                    }
                }
            )
        }
    }

    /** Permite al usuario reintentar después de un error, volviendo a Idle. */
    fun reintentar() {
        _uiState.update { it.copy(estadoPago = EstadoPago.Idle) }
    }
}
