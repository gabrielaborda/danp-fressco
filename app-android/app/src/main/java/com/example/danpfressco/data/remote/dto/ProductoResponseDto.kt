package com.example.danpfressco.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ProductoResponseDto(
    val id: Int,
    val nombre: String,
    val descripcion: String,
    @Json(name = "imagen_url") val imagenUrl: String,
    val categoria: String
)
