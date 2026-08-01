package com.example.danpfressco.data.remote

import com.example.danpfressco.data.remote.dto.AuthResponseDto
import com.example.danpfressco.data.remote.dto.LoginRequestDto
import com.example.danpfressco.data.remote.dto.RegistroRequestDto
import com.example.danpfressco.data.remote.dto.ProductoResponseDto
import com.example.danpfressco.data.remote.dto.LoteResponseDto
import com.example.danpfressco.data.remote.dto.CarritoResponseDto
import com.example.danpfressco.data.remote.dto.CarritoItemResponseDto
import com.example.danpfressco.data.remote.dto.CarritoItemCreateDto
import com.example.danpfressco.data.remote.dto.CarritoItemUpdateDto
import com.example.danpfressco.data.remote.dto.PedidoResponseDto
import com.example.danpfressco.data.remote.dto.CrearPedidoRequestDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @POST("auth/login")
    suspend fun login(@Body body: LoginRequestDto): AuthResponseDto

    @POST("auth/registro")
    suspend fun registro(@Body body: RegistroRequestDto): AuthResponseDto

    @GET("productos")
    suspend fun getProductos(
        @Query("busqueda") busqueda: String? = null,
        @Query("categoria") categoria: String? = null
    ): List<ProductoResponseDto>

    @GET("lotes-disponibles")
    suspend fun getLotesDisponibles(
        @Query("solo_con_descuento") soloConDescuento: Boolean? = null
    ): List<LoteResponseDto>

    // --- Carrito ---
    @GET("carrito")
    suspend fun getCarrito(): CarritoResponseDto

    @POST("carrito/items")
    suspend fun agregarAlCarrito(@Body body: CarritoItemCreateDto): CarritoItemResponseDto

    @PUT("carrito/items/{itemId}")
    suspend fun actualizarItemCarrito(
        @Path("itemId") itemId: Int,
        @Body body: CarritoItemUpdateDto
    ): CarritoItemResponseDto

    @DELETE("carrito/items/{itemId}")
    suspend fun eliminarItemCarrito(@Path("itemId") itemId: Int)

    @DELETE("carrito")
    suspend fun vaciarCarritoRemoto()

    // --- Pedidos ---
    @GET("pedidos")
    suspend fun getPedidos(): List<PedidoResponseDto>

    @POST("pedidos")
    suspend fun crearPedido(@Body body: CrearPedidoRequestDto): PedidoResponseDto

    @GET("pedidos/{id}")
    suspend fun getPedidoDetalle(@Path("id") id: Int): PedidoResponseDto

    @PUT("pedidos/{id}/cancelar")
    suspend fun cancelarPedido(@Path("id") id: Int): PedidoResponseDto
}
