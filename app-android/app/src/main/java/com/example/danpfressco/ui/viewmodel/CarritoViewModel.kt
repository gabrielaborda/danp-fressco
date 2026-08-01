package com.example.danpfressco.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.danpfressco.data.repository.CarritoRepository
import com.example.danpfressco.ui.state.CarritoScreenUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CarritoViewModel @Inject constructor(
    private val carritoRepository: CarritoRepository
) : ViewModel() {

    init {
        cargarCarrito()
    }

    val uiState: StateFlow<CarritoScreenUiState> = carritoRepository.items
        .map { items ->
            CarritoScreenUiState(
                items = items,
                total = items.sumOf { it.precioAplicado * it.cantidad },
                cantidadTotal = items.sumOf { it.cantidad }
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = CarritoScreenUiState()
        )

    fun cargarCarrito() {
        viewModelScope.launch {
            carritoRepository.cargarCarrito()
        }
    }

    fun actualizarCantidad(itemId: String, nuevaCantidad: Int) {
        viewModelScope.launch {
            carritoRepository.actualizarCantidad(itemId, nuevaCantidad)
        }
    }

    fun eliminarItem(itemId: String) {
        viewModelScope.launch {
            carritoRepository.eliminarItem(itemId)
        }
    }
}
