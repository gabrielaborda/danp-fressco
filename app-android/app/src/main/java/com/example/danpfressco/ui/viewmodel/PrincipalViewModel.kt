package com.example.danpfressco.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.danpfressco.data.repository.ProductoRepository
import com.example.danpfressco.ui.state.PrincipalUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PrincipalViewModel @Inject constructor(
    private val productoRepository: ProductoRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PrincipalUiState())
    val uiState: StateFlow<PrincipalUiState> = _uiState.asStateFlow()

    init {
        cargarOfertas()
    }

    fun cargarOfertas() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            val result = productoRepository.obtenerOfertas()
            result.fold(
                onSuccess = { ofertas ->
                    _uiState.update { it.copy(isLoading = false, ofertas = ofertas) }
                },
                onFailure = { throwable ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            errorMessage = throwable.message ?: "Error al cargar las ofertas"
                        )
                    }
                }
            )
        }
    }
}
