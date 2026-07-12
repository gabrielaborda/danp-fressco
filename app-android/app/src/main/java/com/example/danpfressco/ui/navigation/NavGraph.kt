package com.example.danpfressco.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.danpfressco.ui.screens.carrito.CarritoScreen
import com.example.danpfressco.ui.screens.formulariopedido.FormularioPedidoScreen
import com.example.danpfressco.ui.screens.login.LoginScreen
import com.example.danpfressco.ui.screens.mispedidos.MisPedidosScreen
import com.example.danpfressco.ui.screens.ofertasespeciales.OfertasEspecialesScreen
import com.example.danpfressco.ui.screens.principal.PrincipalScreen
import com.example.danpfressco.ui.screens.productos.ProductosScreen
import com.example.danpfressco.ui.screens.registro.RegistroScreen

@Composable
fun FresscoNavGraph() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Login.route
    ) {
        composable(Screen.Login.route) { LoginScreen() }
        composable(Screen.Registro.route) { RegistroScreen() }
        composable(Screen.Principal.route) { PrincipalScreen() }
        composable(Screen.Productos.route) { ProductosScreen() }
        composable(Screen.Carrito.route) { CarritoScreen() }
        composable(Screen.FormularioPedido.route) { FormularioPedidoScreen() }
        composable(Screen.MisPedidos.route) { MisPedidosScreen() }
        composable(Screen.OfertasEspeciales.route) { OfertasEspecialesScreen() }
    }
}
