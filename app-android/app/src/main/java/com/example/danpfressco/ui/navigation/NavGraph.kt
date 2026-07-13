package com.example.danpfressco.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.danpfressco.ui.screens.CarritoScreen
import com.example.danpfressco.ui.screens.FormularioPedidoScreen
import com.example.danpfressco.ui.screens.LoginScreen
import com.example.danpfressco.ui.screens.MisPedidosScreen
import com.example.danpfressco.ui.screens.OfertasEspecialesScreen
import com.example.danpfressco.ui.screens.PrincipalScreen
import com.example.danpfressco.ui.screens.ProductosScreen
import com.example.danpfressco.ui.screens.RegistroScreen

@Composable
fun FresscoNavGraph() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Login.route
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.Principal.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(Screen.Registro.route)
                }
            )
        }
        composable(Screen.Registro.route) {
            RegistroScreen(
                onRegistroSuccess = {
                    navController.navigate(Screen.Principal.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.popBackStack()
                }
            )
        }
        composable(Screen.Principal.route) { PrincipalScreen() }
        composable(Screen.Productos.route) { ProductosScreen() }
        composable(Screen.Carrito.route) { CarritoScreen() }
        composable(Screen.FormularioPedido.route) { FormularioPedidoScreen() }
        composable(Screen.MisPedidos.route) { MisPedidosScreen() }
        composable(Screen.OfertasEspeciales.route) { OfertasEspecialesScreen() }
    }
}
