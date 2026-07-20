package com.example.danpfressco.data.repository

import com.example.danpfressco.data.model.DetallePedido
import com.example.danpfressco.data.model.EstadoPedido
import com.example.danpfressco.data.model.ItemCarrito
import com.example.danpfressco.data.model.Pedido
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.LocalDateTime
import java.util.UUID

/**
 * Implementación en memoria de [PedidoRepository].
 *
 * En Etapa 1 persiste los pedidos únicamente en [_pedidos] (se pierden al cerrar la app).
 * En Etapa 2/3 se reemplazará por llamadas Retrofit o Room sin cambiar la interfaz.
 *
 * Marcado como @Singleton en [AppModule] para que el mismo estado sea compartido
 * entre [FormularioPedidoViewModel] y el futuro MisPedidosViewModel.
 */
class PedidoRepositoryImpl : PedidoRepository {

    private val _pedidos = MutableStateFlow<List<Pedido>>(emptyList())
    override val pedidos: StateFlow<List<Pedido>> = _pedidos.asStateFlow()

    override suspend fun crearPedido(
        items: List<ItemCarrito>,
        nombreContacto: String,
        telefonoContacto: String,
        horarioRecogida: String
    ): Result<Pedido> {
        return try {
            // Snapshot del nombre de tienda (todos los ítems son de la misma tienda
            // porque CarritoRepository ya lo garantiza)
            val nombreTienda = items.firstOrNull()?.oferta?.lote?.nombreTienda ?: ""

            // Construir detalles con snapshot de precio — NUNCA recalcular después
            val detalles = items.map { item ->
                val precioUnitario = item.oferta.lote.precioDescuento
                DetallePedido(
                    loteId = item.oferta.lote.id,
                    productoId = item.oferta.producto.id,
                    nombreProducto = item.oferta.producto.nombre,
                    precioPagadoUnitario = precioUnitario,
                    cantidad = item.cantidad,
                    subtotal = precioUnitario * item.cantidad
                )
            }

            val pedido = Pedido(
                id = UUID.randomUUID().toString(),
                nombreContacto = nombreContacto,
                telefonoContacto = telefonoContacto,
                horarioRecogida = horarioRecogida,
                nombreTienda = nombreTienda,
                fechaCreacion = LocalDateTime.now(),
                estado = EstadoPedido.PENDIENTE,
                detalles = detalles
            )

            _pedidos.update { lista -> lista + pedido }
            Result.success(pedido)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
