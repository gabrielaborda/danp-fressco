package com.example.danpfressco.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.danpfressco.data.model.OfertaProducto
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
            val result = carritoRepository.agregarItem(oferta.lote.id, cantidad)
            result.fold(
                onSuccess = {
                    _snackbarMessage.emit("$cantidad unidades agregadas")
                },
                onFailure = { error ->
                    _snackbarMessage.emit(error.message ?: "No se pudo agregar el producto al carrito")
                }
            )
        }
    }

    fun confirmarCambioTienda() {
        // Obsoleto en Fase 5, se mantiene firma por compatibilidad con UI antigua si existiese
    }

    fun cancelarCambioTienda() {
        // Obsoleto en Fase 5, se mantiene firma por compatibilidad con UI antigua si existiese
    }
}
