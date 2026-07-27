package com.example.danpfressco.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.danpfressco.data.model.ItemCarrito
import com.example.danpfressco.data.repository.CarritoRepository
import com.example.danpfressco.ui.state.FormularioPedidoUiState
import com.example.danpfressco.data.session.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel de la pantalla FormularioPedido.
 *
 * Responsabilidad única (SRP): valida los datos de contacto y señaliza cuándo
 * el usuario puede avanzar a [PasarelaPagoScreen]. La creación del pedido ocurre
 * en [PagoViewModel] una vez que el pago simulado es exitoso.
 *
 * Ya no inyecta [PedidoRepository] — esa dependencia se movió a [PagoViewModel].
 */
@HiltViewModel
class FormularioPedidoViewModel @Inject constructor(
    private val carritoRepository: CarritoRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    init {
        viewModelScope.launch {
            sessionManager.getUserName().firstOrNull()?.let { userName ->
                _uiState.update { state ->
                    if (state.nombreContacto.isBlank()) {
                        state.copy(nombreContacto = userName)
                    } else {
                        state
                    }
                }
            }
        }
    }

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
    private val telefonoRegex = "^\\d{9}$".toRegex()

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
                !telefono.matches(telefonoRegex) -> "Ingresa 9 dígitos sin espacios ni guiones"
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

    // ─── Validación y señal de navegación ────────────────────────────────────

    /**
     * Valida todos los campos. Si son correctos, activa [listoParaPago] para
     * que la pantalla navegue a PasarelaPago. NO crea el pedido ni vacía el carrito.
     */
    fun confirmarFormulario() {
        val current = _uiState.value

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

        // Datos válidos — señal de navegación a PasarelaPago
        _uiState.update { it.copy(listoParaPago = true) }
    }

    /**
     * Restablece el flag de navegación para evitar bucles al regresar a la pantalla.
     */
    fun onNavigationHandled() {
        _uiState.update { it.copy(listoParaPago = false) }
    }
}
