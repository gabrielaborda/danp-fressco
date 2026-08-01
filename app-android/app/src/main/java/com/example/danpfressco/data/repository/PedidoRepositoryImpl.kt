package com.example.danpfressco.data.repository

import com.example.danpfressco.data.model.DetallePedido
import com.example.danpfressco.data.model.EstadoPedido
import com.example.danpfressco.data.model.Pedido
import com.example.danpfressco.data.remote.ApiService
import com.example.danpfressco.data.remote.dto.CrearPedidoRequestDto
import com.example.danpfressco.data.remote.dto.PedidoItemResponseDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PedidoRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : PedidoRepository {

    private val _pedidos = MutableStateFlow<List<Pedido>>(emptyList())
    override val pedidos: StateFlow<List<Pedido>> = _pedidos.asStateFlow()

    override suspend fun crearPedido(
        nombreContacto: String,
        telefonoContacto: String,
        horarioRecogida: String,
        notas: String?
    ): Result<Pedido> {
        return try {
            val responseDto = apiService.crearPedido(CrearPedidoRequestDto(notas = notas))
            val pedido = Pedido(
                id = responseDto.id.toString(),
                nombreContacto = nombreContacto,
                telefonoContacto = telefonoContacto,
                horarioRecogida = horarioRecogida,
                nombreTienda = "Tienda #${responseDto.tiendaId}",
                fechaCreacion = parseFecha(responseDto.fechaPedido),
                estado = responseDto.estado.toEstadoPedido(),
                detalles = responseDto.items.map { it.toDetallePedido() },
                notas = responseDto.notas
            )
            _pedidos.update { lista -> lista + pedido }
            Result.success(pedido)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun cargarPedidos(): Result<Unit> {
        return try {
            val responseList = apiService.getPedidos()
            val list = responseList.map { responseDto ->
                Pedido(
                    id = responseDto.id.toString(),
                    nombreContacto = "Contacto",
                    telefonoContacto = "N/A",
                    horarioRecogida = "N/A",
                    nombreTienda = "Tienda #${responseDto.tiendaId}",
                    fechaCreacion = parseFecha(responseDto.fechaPedido),
                    estado = responseDto.estado.toEstadoPedido(),
                    detalles = responseDto.items.map { it.toDetallePedido() },
                    notas = responseDto.notas
                )
            }
            _pedidos.value = list
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun parseFecha(fechaStr: String): LocalDateTime {
        return try {
            LocalDateTime.parse(fechaStr, DateTimeFormatter.ISO_DATE_TIME)
        } catch (e: Exception) {
            try {
                LocalDateTime.parse(fechaStr)
            } catch (ex: Exception) {
                LocalDateTime.now()
            }
        }
    }

    private fun String.toEstadoPedido(): EstadoPedido {
        return when (this.lowercase()) {
            "pendiente" -> EstadoPedido.PENDIENTE
            "confirmado" -> EstadoPedido.CONFIRMADO
            "cancelado" -> EstadoPedido.CANCELADO
            "entregado" -> EstadoPedido.ENTREGADO
            else -> EstadoPedido.PENDIENTE
        }
    }

    private fun PedidoItemResponseDto.toDetallePedido(): DetallePedido {
        val precio = this.precioUnitarioAplicado.toDoubleOrNull() ?: 0.0
        val subtotalDouble = this.subtotal.toDoubleOrNull() ?: (precio * this.cantidad)
        return DetallePedido(
            loteId = this.loteId.toString(),
            productoId = "",
            nombreProducto = this.nombreProducto ?: "Producto #${this.loteId}",
            precioPagadoUnitario = precio,
            cantidad = this.cantidad,
            subtotal = subtotalDouble
        )
    }
}
