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
 */
@HiltViewModel
class PagoViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val pagoRepository: PagoRepository,
    private val pedidoRepository: PedidoRepository,
    private val carritoRepository: CarritoRepository
) : ViewModel() {

    val nombreContacto: String = checkNotNull(savedStateHandle["nombre"])
    val telefonoContacto: String = checkNotNull(savedStateHandle["telefono"])
    val horarioRecogida: String = checkNotNull(savedStateHandle["horario"])

    private val _uiState = MutableStateFlow(PagoUiState())
    val uiState: StateFlow<PagoUiState> = _uiState.asStateFlow()

    val itemsCarrito: StateFlow<List<ItemCarrito>> = carritoRepository.items
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    fun onMetodoPagoChanged(metodo: MetodoPago) {
        _uiState.update {
            it.copy(
                metodoPago = metodo,
                numeroTarjetaError = null,
                vencimientoError = null,
                cvvError = null,
                estadoPago = EstadoPago.Idle
            )
        }
    }

    fun onNumeroTarjetaChanged(numero: String) {
        val limpio = numero.filter { it.isDigit() }.take(16)
        _uiState.update {
            it.copy(
                numeroTarjeta = limpio,
                numeroTarjetaError = null
            )
        }
    }

    fun onVencimientoChanged(vencimiento: String) {
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

    fun confirmarPago() {
        val current = _uiState.value

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
            val items = itemsCarrito.value
            val monto = items.sumOf { it.precioAplicado * it.cantidad }

            val resultado = pagoRepository.procesarPago(
                metodo = current.metodoPago,
                monto = monto
            )

            resultado.fold(
                onSuccess = {
                    val pedidoResult = pedidoRepository.crearPedido(
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

    fun reintentar() {
        _uiState.update { it.copy(estadoPago = EstadoPago.Idle) }
    }
}
