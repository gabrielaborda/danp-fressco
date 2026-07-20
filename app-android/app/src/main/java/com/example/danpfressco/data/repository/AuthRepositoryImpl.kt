package com.example.danpfressco.data.repository

import com.example.danpfressco.data.model.Usuario
import com.example.danpfressco.data.remote.ApiService
import com.example.danpfressco.data.remote.dto.LoginRequestDto
import com.example.danpfressco.data.remote.dto.RegistroRequestDto
import com.example.danpfressco.data.session.SessionManager
import retrofit2.HttpException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val sessionManager: SessionManager
) : AuthRepository {

    override suspend fun login(email: String, password: String): Result<Usuario> {
        return try {
            val response = apiService.login(LoginRequestDto(email = email, password = password))
            val usuario = Usuario(
                id = response.id.toString(),
                nombre = response.nombre,
                email = email,
                token = response.accessToken
            )
            sessionManager.saveSession(usuario)
            Result.success(usuario)
        } catch (e: HttpException) {
            val mappedException = when (e.code()) {
                401 -> Exception("Correo o contraseña incorrectos")
                else -> Exception("Error de conexión, intenta de nuevo")
            }
            Result.failure(mappedException)
        } catch (e: Exception) {
            Result.failure(Exception("Error de conexión, intenta de nuevo"))
        }
    }

    override suspend fun registrar(
        nombre: String,
        email: String,
        password: String,
        telefono: String
    ): Result<Usuario> {
        return try {
            val response = apiService.registro(
                RegistroRequestDto(
                    nombre = nombre,
                    email = email,
                    password = password,
                    telefono = telefono
                )
            )
            val usuario = Usuario(
                id = response.id.toString(),
                nombre = response.nombre,
                email = email,
                token = response.accessToken
            )
            sessionManager.saveSession(usuario)
            Result.success(usuario)
        } catch (e: HttpException) {
            val mappedException = when (e.code()) {
                401 -> Exception("Credenciales inválidas")
                409 -> Exception("Este correo ya está registrado")
                else -> Exception("Error de conexión, intenta de nuevo")
            }
            Result.failure(mappedException)
        } catch (e: Exception) {
            Result.failure(Exception("Error de conexión, intenta de nuevo"))
        }
    }
}
