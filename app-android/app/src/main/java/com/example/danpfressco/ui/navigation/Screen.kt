package com.example.danpfressco.ui.navigation

sealed class Screen(val route: String) {
    data object Login : Screen("login")
    data object Registro : Screen("registro")
    data object Principal : Screen("principal")
    data object Productos : Screen("productos")
    data object Carrito : Screen("carrito")
    data object FormularioPedido : Screen("formulariopedido")
    data object MisPedidos : Screen("mispedidos")
    data object OfertasEspeciales : Screen("ofertasespeciales")
}
