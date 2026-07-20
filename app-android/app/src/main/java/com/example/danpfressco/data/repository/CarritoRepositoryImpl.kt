package com.example.danpfressco.data.repository

import com.example.danpfressco.data.model.ItemCarrito
import com.example.danpfressco.data.model.OfertaProducto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CarritoRepositoryImpl @Inject constructor() : CarritoRepository {

    private val _items = MutableStateFlow<List<ItemCarrito>>(emptyList())
    override val items: StateFlow<List<ItemCarrito>> = _items.asStateFlow()

    override suspend fun agregarItem(oferta: OfertaProducto, cantidad: Int): AgregarItemResult {
        val itemsActuales = _items.value

        if (itemsActuales.isEmpty()) {
            _items.value = listOf(ItemCarrito(oferta = oferta, cantidad = cantidad))
            return AgregarItemResult.Agregado
        }

        val tiendaActual = itemsActuales.first().oferta.lote.nombreTienda
        val tiendaNueva = oferta.lote.nombreTienda

        if (tiendaActual != tiendaNueva) {
            return AgregarItemResult.ConflictoTienda(
                tiendaActual = tiendaActual,
                tiendaNueva = tiendaNueva
            )
        }

        val itemExistente = itemsActuales.find { it.oferta.lote.id == oferta.lote.id }
        if (itemExistente != null) {
            val nuevaCantidad = minOf(
                itemExistente.cantidad + cantidad,
                oferta.lote.cantidadRestante
            )
            _items.value = itemsActuales.map {
                if (it.oferta.lote.id == oferta.lote.id) {
                    it.copy(cantidad = nuevaCantidad)
                } else {
                    it
                }
            }
        } else {
            val cantidadLimitada = minOf(cantidad, oferta.lote.cantidadRestante)
            _items.value = itemsActuales + ItemCarrito(oferta = oferta, cantidad = cantidadLimitada)
        }

        return AgregarItemResult.Agregado
    }

    override suspend fun confirmarCambioTienda(oferta: OfertaProducto, cantidad: Int) {
        val cantidadLimitada = minOf(cantidad, oferta.lote.cantidadRestante)
        _items.value = listOf(ItemCarrito(oferta = oferta, cantidad = cantidadLimitada))
    }

    override suspend fun actualizarCantidad(loteId: String, nuevaCantidad: Int) {
        val itemsActuales = _items.value
        _items.value = itemsActuales.map { item ->
            if (item.oferta.lote.id == loteId) {
                val cantidadValidada = nuevaCantidad.coerceIn(1, item.oferta.lote.cantidadRestante)
                item.copy(cantidad = cantidadValidada)
            } else {
                item
            }
        }
    }

    override suspend fun eliminarItem(loteId: String) {
        _items.value = _items.value.filter { it.oferta.lote.id != loteId }
    }

    override suspend fun vaciarCarrito() {
        _items.value = emptyList()
    }
}
