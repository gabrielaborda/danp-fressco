package com.example.danpfressco.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.danpfressco.data.model.OfertaProducto
import com.example.danpfressco.data.repository.AgregarItemResult
import com.example.danpfressco.data.repository.CarritoRepository
import com.example.danpfressco.data.repository.ProductoRepository
import com.example.danpfressco.ui.state.ProductosUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProductosViewModel @Inject constructor(
    private val productoRepository: ProductoRepository,
    private val carritoRepository: CarritoRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val loteId: String = savedStateHandle.get<String>("loteId") ?: ""

    private val _uiState = MutableStateFlow(ProductosUiState())
    val uiState: StateFlow<ProductosUiState> = _uiState.asStateFlow()

    private val _snackbarMessage = MutableSharedFlow<String>()
    val snackbarMessage: SharedFlow<String> = _snackbarMessage.asSharedFlow()

    init {
        cargarOferta()
    }

    fun cargarOferta() {
        if (loteId.isEmpty()) {
            _uiState.update { it.copy(isLoading = false, errorMessage = "ID de producto no válido") }
            return
        }
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            val result = productoRepository.obtenerOfertaPorId(loteId)
            result.fold(
                onSuccess = { oferta ->
                    if (oferta != null) {
                        _uiState.update { it.copy(isLoading = false, oferta = oferta) }
                    } else {
                        _uiState.update {
                            it.copy(isLoading = false, errorMessage = "Producto no encontrado")
                        }
                    }
                },
                onFailure = { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = throwable.message ?: "Error al cargar el producto"
                        )
                    }
                }
            )
        }
    }

    fun agregarAlCarrito(oferta: OfertaProducto, cantidad: Int) {
        viewModelScope.launch {
            when (val result = carritoRepository.agregarItem(oferta, cantidad)) {
                is AgregarItemResult.Agregado -> {
                    _snackbarMessage.emit("$cantidad unidades agregadas")
                }
                is AgregarItemResult.ConflictoTienda -> {
                    _uiState.update {
                        it.copy(
                            mostrarDialogoCambioTienda = true,
                            tiendaEnCarrito = result.tiendaActual,
                            tiendaNueva = result.tiendaNueva,
                            ofertaPendiente = oferta,
                            cantidadPendiente = cantidad
                        )
                    }
                }
            }
        }
    }

    fun confirmarCambioTienda() {
        val estado = _uiState.value
        val oferta = estado.ofertaPendiente ?: return
        viewModelScope.launch {
            carritoRepository.confirmarCambioTienda(oferta, estado.cantidadPendiente)
            _uiState.update {
                it.copy(
                    mostrarDialogoCambioTienda = false,
                    ofertaPendiente = null,
                    cantidadPendiente = 0
                )
            }
            _snackbarMessage.emit("${estado.cantidadPendiente} unidades agregadas")
        }
    }

    fun cancelarCambioTienda() {
        _uiState.update {
            it.copy(
                mostrarDialogoCambioTienda = false,
                ofertaPendiente = null,
                cantidadPendiente = 0
            )
        }
    }
}
