package com.example.danpfressco.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.danpfressco.data.model.ItemCarrito
import com.example.danpfressco.data.repository.CarritoRepository
import com.example.danpfressco.data.repository.PedidoRepository
import com.example.danpfressco.ui.state.FormularioPedidoUiState
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
 * ViewModel de la pantalla FormularioPedido.
 *
 * Inyecta tanto [CarritoRepository] (para leer y vaciar el carrito) como
 * [PedidoRepository] (para crear y persistir el pedido). Nunca depende de
 * implementaciones concretas.
 */
@HiltViewModel
class FormularioPedidoViewModel @Inject constructor(
    private val carritoRepository: CarritoRepository,
    private val pedidoRepository: PedidoRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FormularioPedidoUiState())
    val uiState: StateFlow<FormularioPedidoUiState> = _uiState.asStateFlow()

    /** Exposición reactiva de los ítems del carrito — no duplica el estado. */
    val itemsCarrito: StateFlow<List<ItemCarrito>> = carritoRepository.items
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    // Regex: solo dígitos, longitud entre 7 y 15 (locales y con código de país)
    private val telefonoRegex = "^\\d{7,15}$".toRegex()

    // ─── Handlers de cambio de campo ─────────────────────────────────────────

    fun onNombreChanged(nombre: String) {
        _uiState.update { state ->
            val error = if (nombre.isBlank()) "El nombre no puede estar vacío" else null
            state.copy(nombreContacto = nombre, nombreError = error, errorMessage = null)
        }
    }

    fun onTelefonoChanged(telefono: String) {
        _uiState.update { state ->
            val error = when {
                telefono.isBlank() -> "El teléfono no puede estar vacío"
                !telefono.matches(telefonoRegex) -> "Ingresa entre 7 y 15 dígitos sin espacios ni guiones"
                else -> null
            }
            state.copy(telefonoContacto = telefono, telefonoError = error, errorMessage = null)
        }
    }

    fun onHorarioChanged(horario: String) {
        _uiState.update { state ->
            val error = if (horario.isBlank()) "Selecciona un horario de recogida" else null
            state.copy(horarioRecogida = horario, horarioError = error, errorMessage = null)
        }
    }

    // ─── Confirmación del pedido ──────────────────────────────────────────────

    fun confirmarPedido() {
        val current = _uiState.value

        // Validación completa antes de proceder
        val nombreError = if (current.nombreContacto.isBlank()) "El nombre no puede estar vacío" else null
        val telefonoError = when {
            current.telefonoContacto.isBlank() -> "El teléfono no puede estar vacío"
            !current.telefonoContacto.matches(telefonoRegex) -> "Ingresa entre 7 y 15 dígitos sin espacios ni guiones"
            else -> null
        }
        val horarioError = if (current.horarioRecogida.isNullOrBlank()) "Selecciona un horario de recogida" else null

        if (nombreError != null || telefonoError != null || horarioError != null) {
            _uiState.update { state ->
                state.copy(
                    nombreError = nombreError,
                    telefonoError = telefonoError,
                    horarioError = horarioError
                )
            }
            return
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            val items = itemsCarrito.value

            val result = pedidoRepository.crearPedido(
                items = items,
                nombreContacto = current.nombreContacto.trim(),
                telefonoContacto = current.telefonoContacto.trim(),
                horarioRecogida = current.horarioRecogida!!
            )

            result.fold(
                onSuccess = {
                    // Vaciar carrito sólo una vez que el pedido fue creado exitosamente
                    carritoRepository.vaciarCarrito()
                    _uiState.update { it.copy(isLoading = false, pedidoConfirmado = true) }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Ocurrió un error inesperado"
                        )
                    }
                }
            )
        }
    }
}
