package com.example.danpfressco.data.repository

import com.example.danpfressco.data.model.Lote
import com.example.danpfressco.data.model.OfertaProducto
import com.example.danpfressco.data.model.Producto
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProductoRepositoryImpl @Inject constructor() : ProductoRepository {

    private val mockOfertas = listOf(
        OfertaProducto(
            producto = Producto(
                id = "p1",
                nombre = "Yogurt Griego Fresa",
                descripcion = "Yogurt griego descremado sabor fresa 1L",
                imagenUrl = "https://picsum.photos/id/1080/400/400", // Imagen de fresas
                precioOriginal = 15.0
            ),
            lote = Lote(
                id = UUID.randomUUID().toString(),
                productoId = "p1",
                fechaVencimiento = LocalDate.now().plusDays(2),
                cantidadRestante = 5,
                precioDescuento = 7.5,
                nombreTienda = "Minimarket El Sol"
            )
        ),
        OfertaProducto(
            producto = Producto(
                id = "p2",
                nombre = "Pan de Molde Blanco",
                descripcion = "Pan de molde tradicional bolsa grande",
                imagenUrl = "https://picsum.photos/id/429/400/400", // Imagen de pan/comida
                precioOriginal = 8.5
            ),
            lote = Lote(
                id = UUID.randomUUID().toString(),
                productoId = "p2",
                fechaVencimiento = LocalDate.now().plusDays(1),
                cantidadRestante = 2,
                precioDescuento = 4.0,
                nombreTienda = "Bodega Doña Lucha"
            )
        ),
        OfertaProducto(
            producto = Producto(
                id = "p3",
                nombre = "Manzanas Rojas (1kg)",
                descripcion = "Malla de manzanas rojas frescas",
                imagenUrl = "https://picsum.photos/id/824/400/400", // Imagen de frutas/manzanas
                precioOriginal = 6.0
            ),
            lote = Lote(
                id = UUID.randomUUID().toString(),
                productoId = "p3",
                fechaVencimiento = LocalDate.now().plusDays(3),
                cantidadRestante = 10,
                precioDescuento = 3.5,
                nombreTienda = "Minimarket El Sol"
            )
        ),
        OfertaProducto(
            producto = Producto(
                id = "p4",
                nombre = "Queso Edam Laminado",
                descripcion = "Paquete de queso Edam en láminas 200g",
                imagenUrl = "https://picsum.photos/id/312/400/400", // Imagen genérica
                precioOriginal = 12.0
            ),
            lote = Lote(
                id = UUID.randomUUID().toString(),
                productoId = "p4",
                fechaVencimiento = LocalDate.now().plusDays(1),
                cantidadRestante = 3,
                precioDescuento = 5.0,
                nombreTienda = "Tienda Los Vecinos"
            )
        ),
        OfertaProducto(
            producto = Producto(
                id = "p5",
                nombre = "Leche Fresca Entera",
                descripcion = "Botella de leche fresca entera 1L",
                imagenUrl = "https://picsum.photos/id/200/400/400", // Imagen genérica
                precioOriginal = 5.5
            ),
            lote = Lote(
                id = UUID.randomUUID().toString(),
                productoId = "p5",
                fechaVencimiento = LocalDate.now().plusDays(2),
                cantidadRestante = 6,
                precioDescuento = 3.0,
                nombreTienda = "Bodega Doña Lucha"
            )
        ),
        OfertaProducto(
            producto = Producto(
                id = "p6",
                nombre = "Mermelada de Durazno",
                descripcion = "Frasco de mermelada artesanal 300g",
                imagenUrl = "https://picsum.photos/id/111/400/400", // Imagen genérica
                precioOriginal = 9.0
            ),
            lote = Lote(
                id = UUID.randomUUID().toString(),
                productoId = "p6",
                fechaVencimiento = LocalDate.now().plusDays(5),
                cantidadRestante = 4,
                precioDescuento = 6.0,
                nombreTienda = "Tienda Los Vecinos"
            )
        )
    )

    override suspend fun obtenerOfertas(): Result<List<OfertaProducto>> {
        delay(1000) // Simular latencia de red
        return Result.success(mockOfertas)
    }
}
