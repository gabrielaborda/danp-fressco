package com.example.danpfressco.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.danpfressco.ui.screens.CarritoScreen
import com.example.danpfressco.ui.screens.FormularioPedidoScreen
import com.example.danpfressco.ui.screens.LoginScreen
import com.example.danpfressco.ui.screens.MisPedidosScreen
import com.example.danpfressco.ui.screens.OfertasEspecialesScreen
import com.example.danpfressco.ui.screens.PasarelaPagoScreen
import com.example.danpfressco.ui.screens.PrincipalScreen
import com.example.danpfressco.ui.screens.ProductosScreen
import com.example.danpfressco.ui.screens.RegistroScreen

@Composable
fun FresscoNavGraph() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val bottomNavRoutes = listOf(
        Screen.Principal.route,
        Screen.OfertasEspeciales.route,
        Screen.MisPedidos.route
    )
    val showBottomBar = currentRoute in bottomNavRoutes

    // Lambda de logout compartida — limpia todo el back stack hasta la raíz
    val onLogout: () -> Unit = {
        navController.navigate(Screen.Login.route) {
            popUpTo(0) { inclusive = true }
        }
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Home, contentDescription = "Inicio") },
                        label = { Text("Inicio") },
                        selected = currentRoute == Screen.Principal.route,
                        onClick = {
                            navController.navigate(Screen.Principal.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.LocalOffer, contentDescription = "Ofertas") },
                        label = { Text("Ofertas") },
                        selected = currentRoute == Screen.OfertasEspeciales.route,
                        onClick = {
                            navController.navigate(Screen.OfertasEspeciales.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Receipt, contentDescription = "Pedidos") },
                        label = { Text("Pedidos") },
                        selected = currentRoute == Screen.MisPedidos.route,
                        onClick = {
                            navController.navigate(Screen.MisPedidos.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Login.route,
            modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding())
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

            composable(Screen.Principal.route) {
                PrincipalScreen(
                    navController = navController,
                    onLogout = onLogout
                )
            }

            composable(
                route = Screen.Productos.route,
                arguments = listOf(navArgument("loteId") { type = NavType.StringType })
            ) {
                ProductosScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToCarrito = { navController.navigate(Screen.Carrito.route) }
                )
            }

            composable(Screen.Carrito.route) {
                CarritoScreen(navController = navController)
            }

            composable(Screen.FormularioPedido.route) {
                FormularioPedidoScreen(
                    onNavegaToPasarela = { nombre, telefono, horario ->
                        navController.navigate(
                            Screen.PasarelaPago.createRoute(nombre, telefono, horario)
                        )
                    },
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(
                route = Screen.PasarelaPago.route,
                arguments = listOf(
                    navArgument("nombre") { type = NavType.StringType },
                    navArgument("telefono") { type = NavType.StringType },
                    navArgument("horario") { type = NavType.StringType }
                )
            ) {
                PasarelaPagoScreen(
                    onPagoExitoso = {
                        navController.navigate(Screen.MisPedidos.route) {
                            // Limpiar el back stack hasta Principal para que
                            // "atrás" desde MisPedidos no regrese a la pasarela
                            popUpTo(Screen.Principal.route) { inclusive = false }
                        }
                    },
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.MisPedidos.route) {
                MisPedidosScreen(
                    onNavigateToPrincipal = {
                        navController.navigate(Screen.Principal.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                inclusive = true
                            }
                        }
                    },
                    onNavigateToCarrito = { navController.navigate(Screen.Carrito.route) },
                    onLogout = onLogout
                )
            }

            composable(Screen.OfertasEspeciales.route) {
                OfertasEspecialesScreen(
                    navController = navController,
                    onLogout = onLogout
                )
            }
        }
    }
}
