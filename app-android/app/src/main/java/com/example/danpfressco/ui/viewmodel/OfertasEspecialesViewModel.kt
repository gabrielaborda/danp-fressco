package com.example.danpfressco.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.danpfressco.data.repository.ProductoRepository
import com.example.danpfressco.ui.state.OfertasEspecialesUiState
import com.example.danpfressco.ui.util.calcularPorcentajeDescuento
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OfertasEspecialesViewModel @Inject constructor(
    private val productoRepository: ProductoRepository
) : ViewModel() {

    companion object {
        const val UMBRAL_DESCUENTO_ESPECIAL = 40.0
    }

    private val _uiState = MutableStateFlow(OfertasEspecialesUiState())
    val uiState: StateFlow<OfertasEspecialesUiState> = _uiState.asStateFlow()

    init {
        cargarOfertasEspeciales()
    }

    fun cargarOfertasEspeciales() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            val result = productoRepository.obtenerOfertas()
            result.fold(
                onSuccess = { ofertas ->
                    val ofertasFiltradas = ofertas.filter { oferta ->
                        calcularPorcentajeDescuento(
                            oferta.producto.precioOriginal,
                            oferta.lote.precioDescuento
                        ) >= UMBRAL_DESCUENTO_ESPECIAL
                    }
                    _uiState.update { it.copy(isLoading = false, ofertas = ofertasFiltradas) }
                },
                onFailure = { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = throwable.message ?: "Error al cargar las ofertas especiales"
                        )
                    }
                }
            )
        }
    }
}
