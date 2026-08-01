package com.example.danpfressco.data.repository

import com.example.danpfressco.data.model.ItemCarrito
import kotlinx.coroutines.flow.StateFlow

interface CarritoRepository {
    val items: StateFlow<List<ItemCarrito>>
    suspend fun agregarItem(loteId: String, cantidad: Int): Result<Unit>
    suspend fun actualizarCantidad(itemId: String, nuevaCantidad: Int): Result<Unit>
    suspend fun eliminarItem(itemId: String): Result<Unit>
    suspend fun vaciarCarrito(): Result<Unit>
    suspend fun cargarCarrito(): Result<Unit>
}
