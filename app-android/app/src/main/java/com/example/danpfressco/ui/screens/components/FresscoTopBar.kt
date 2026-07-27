package com.example.danpfressco.ui.screens.components

import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight

/**
 * TopAppBar compartida para las pantallas principales autenticadas.
 *
 * Aplica SRP: centraliza la composición de los tres elementos del encabezado
 * (botón de logout, título, botón de carrito) en un solo componente reutilizable,
 * evitando duplicación en Principal, OfertasEspeciales y MisPedidos.
 *
 * NO debe usarse en Login, Registro, Carrito, FormularioPedido ni PasarelaPago.
 *
 * @param titulo Texto del encabezado de la pantalla.
 * @param onNavigateToCarrito Lambda invocada al pulsar el ícono del carrito.
 * @param onLogout Lambda de navegación invocada tras el logout exitoso.
 *                 Debe limpiar el back stack completo (popUpTo(0) inclusive).
 * @param mostrarCarrito Si false, oculta el [CarritoIconButton] (reservado para uso futuro).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FresscoTopBar(
    titulo: String,
    onNavigateToCarrito: () -> Unit,
    onLogout: () -> Unit,
    mostrarCarrito: Boolean = true
) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = titulo,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        },
        navigationIcon = {
            LogoutIconButton(onLogout = onLogout)
        },
        actions = {
            if (mostrarCarrito) {
                CarritoIconButton(onClick = onNavigateToCarrito)
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    )
}
