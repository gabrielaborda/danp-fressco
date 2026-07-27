package com.example.danpfressco.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.danpfressco.data.repository.PedidoRepository
import com.example.danpfressco.ui.state.MisPedidosUiState
import com.example.danpfressco.ui.state.PedidoItemUiState
import com.example.danpfressco.ui.util.formatearPrecio
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

private val FECHA_FORMATTER = DateTimeFormatter.ofPattern("d MMM, HH:mm", Locale("es"))

/**
 * ViewModel de la pantalla Mis Pedidos.
 * Observa el [PedidoRepository], ordena los pedidos por fecha descendente y
 * pre-formatea los textos para la UI.
 */
@HiltViewModel
class MisPedidosViewModel @Inject constructor(
    private val pedidoRepository: PedidoRepository
) : ViewModel() {

    val uiState: StateFlow<MisPedidosUiState> = pedidoRepository.pedidos
        .map { lista ->
            val items = lista
                .sortedByDescending { it.fechaCreacion }
                .map { pedido ->
                    PedidoItemUiState(
                        pedido = pedido,
                        fechaFormateada = pedido.fechaCreacion.format(FECHA_FORMATTER),
                        totalFormateado = formatearPrecio(
                            pedido.detalles.sumOf { it.subtotal }
                        )
                    )
                }
            MisPedidosUiState(pedidos = items, isLoading = false)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = MisPedidosUiState(isLoading = true)
        )
}
