package com.example.danpfressco.ui.navigation

import java.net.URLDecoder
import java.net.URLEncoder

sealed class Screen(val route: String) {
    data object Login : Screen("login")
    data object Registro : Screen("registro")
    data object Principal : Screen("principal")
    data object Productos : Screen("productos/{loteId}") {
        fun createRoute(loteId: String) = "productos/$loteId"
    }
    data object Carrito : Screen("carrito")
    data object FormularioPedido : Screen("formulariopedido")
    data object MisPedidos : Screen("mispedidos")
    data object OfertasEspeciales : Screen("ofertasespeciales")
    data object PasarelaPago : Screen("pasarelapago/{nombre}/{telefono}/{horario}") {
        fun createRoute(nombre: String, telefono: String, horario: String): String {
            val encNombre = URLEncoder.encode(nombre, "UTF-8")
            val encTelefono = URLEncoder.encode(telefono, "UTF-8")
            val encHorario = URLEncoder.encode(horario, "UTF-8")
            return "pasarelapago/$encNombre/$encTelefono/$encHorario"
        }

        fun decodeArg(value: String): String =
            URLDecoder.decode(value, "UTF-8")
    }
}
