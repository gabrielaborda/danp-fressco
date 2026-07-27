package com.example.danpfressco.data.repository

import com.example.danpfressco.data.model.ItemCarrito
import com.example.danpfressco.data.model.OfertaProducto
import kotlinx.coroutines.flow.StateFlow

sealed class AgregarItemResult {
    data object Agregado : AgregarItemResult()
    data class ConflictoTienda(
        val tiendaActual: String,
        val tiendaNueva: String
    ) : AgregarItemResult()
}

interface CarritoRepository {
    val items: StateFlow<List<ItemCarrito>>
    suspend fun agregarItem(oferta: OfertaProducto, cantidad: Int): AgregarItemResult
    suspend fun confirmarCambioTienda(oferta: OfertaProducto, cantidad: Int)
    suspend fun actualizarCantidad(loteId: String, nuevaCantidad: Int)
    suspend fun eliminarItem(loteId: String)
    suspend fun vaciarCarrito()
}
