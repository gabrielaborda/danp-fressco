package com.example.danpfressco.data.repository

import com.example.danpfressco.data.model.Pedido
import kotlinx.coroutines.flow.StateFlow

interface PedidoRepository {
    val pedidos: StateFlow<List<Pedido>>

    suspend fun crearPedido(
        nombreContacto: String,
        telefonoContacto: String,
        horarioRecogida: String,
        notas: String? = null
    ): Result<Pedido>

    suspend fun cargarPedidos(): Result<Unit>
}
