package com.example.danpfressco.data.repository

import com.example.danpfressco.data.model.Usuario

interface AuthRepository {
    suspend fun login(email: String, password: String): Result<Usuario>
    suspend fun registrar(nombre: String, email: String, password: String, telefono: String): Result<Usuario>
}
