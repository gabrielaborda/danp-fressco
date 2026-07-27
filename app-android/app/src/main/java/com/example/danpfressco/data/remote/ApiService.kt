package com.example.danpfressco.data.remote

import com.example.danpfressco.data.remote.dto.AuthResponseDto
import com.example.danpfressco.data.remote.dto.LoginRequestDto
import com.example.danpfressco.data.remote.dto.RegistroRequestDto
import com.example.danpfressco.data.remote.dto.ProductoResponseDto
import com.example.danpfressco.data.remote.dto.LoteResponseDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
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
}
