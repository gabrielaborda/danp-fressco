package com.example.danpfressco.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.danpfressco.data.model.MetodoPago
import com.example.danpfressco.ui.state.EstadoPago
import com.example.danpfressco.ui.util.formatearPrecio
import com.example.danpfressco.ui.viewmodel.PagoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasarelaPagoScreen(
    onPagoExitoso: () -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: PagoViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val itemsCarrito by viewModel.itemsCarrito.collectAsState()
    val monto = itemsCarrito.sumOf { it.oferta.lote.precioDescuento * it.cantidad }
    val procesando = uiState.estadoPago == EstadoPago.Procesando

    // Bloquear el botón atrás mientras se procesa el pago
    BackHandler(enabled = procesando) { /* Bloqueado intencionalmente */ }

    // Gatillo de navegación al éxito
    LaunchedEffect(uiState.estadoPago) {
        if (uiState.estadoPago == EstadoPago.Exito) onPagoExitoso()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Pago seguro",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        enabled = !procesando
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Regresar"
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // ─── Monto total ──────────────────────────────────────────────────
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Total a pagar",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = formatearPrecio(monto),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            // ─── Selector de método de pago ───────────────────────────────────
            Text(
                text = "Método de pago",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetodoPago.entries.forEach { metodo ->
                    val seleccionado = uiState.metodoPago == metodo
                    FilterChip(
                        selected = seleccionado,
                        onClick = { viewModel.onMetodoPagoChanged(metodo) },
                        label = {
                            Text(
                                when (metodo) {
                                    MetodoPago.TARJETA -> "Tarjeta"
                                    MetodoPago.YAPE_PLIN -> "Yape / Plin"
                                }
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = when (metodo) {
                                    MetodoPago.TARJETA -> Icons.Default.CreditCard
                                    MetodoPago.YAPE_PLIN -> Icons.Default.PhoneAndroid
                                },
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        modifier = Modifier.weight(1f),
                        enabled = !procesando
                    )
                }
            }

            // ─── Panel según método ───────────────────────────────────────────
            when (uiState.metodoPago) {
                MetodoPago.TARJETA -> PanelTarjeta(
                    uiState = uiState,
                    habilitado = !procesando,
                    onNumeroChanged = viewModel::onNumeroTarjetaChanged,
                    onVencimientoChanged = viewModel::onVencimientoChanged,
                    onCvvChanged = viewModel::onCvvChanged
                )
                MetodoPago.YAPE_PLIN -> PanelYapePlin()
            }

            // ─── Mensaje de error ─────────────────────────────────────────────
            val estadoActual = uiState.estadoPago
            if (estadoActual is EstadoPago.Error) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = estadoActual.mensaje,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // ─── Botón confirmar pago ─────────────────────────────────────────
            Button(
                onClick = viewModel::confirmarPago,
                enabled = !procesando,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                if (procesando) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Procesando...")
                } else {
                    Text(
                        text = "Confirmar pago",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Botón reintentar tras error
            if (estadoActual is EstadoPago.Error) {
                OutlinedButton(
                    onClick = viewModel::reintentar,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Reintentar con otro método")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

private fun formatearVencimiento(input: String): String {
    val digitos = input.filter { it.isDigit() }.take(4)
    return if (digitos.length <= 2) digitos
    else "${digitos.substring(0, 2)}/${digitos.substring(2)}"
}

private fun formatearNumeroTarjeta(input: String): String {
    val digitos = input.filter { it.isDigit() }.take(16)
    val chunks = digitos.chunked(4)
    return chunks.joinToString(" ")
}

@Composable
private fun PanelTarjeta(
    uiState: com.example.danpfressco.ui.state.PagoUiState,
    habilitado: Boolean,
    onNumeroChanged: (String) -> Unit,
    onVencimientoChanged: (String) -> Unit,
    onCvvChanged: (String) -> Unit
) {
    var vencimientoValue by remember {
        mutableStateOf(TextFieldValue(uiState.vencimiento, selection = TextRange(uiState.vencimiento.length)))
    }
    var numeroTarjetaValue by remember {
        val formatted = formatearNumeroTarjeta(uiState.numeroTarjeta)
        mutableStateOf(TextFieldValue(formatted, selection = TextRange(formatted.length)))
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Datos de tarjeta",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedTextField(
                value = numeroTarjetaValue,
                onValueChange = { nuevo ->
                    if (nuevo.text != numeroTarjetaValue.text) {
                        val formateado = formatearNumeroTarjeta(nuevo.text)
                        numeroTarjetaValue = TextFieldValue(
                            text = formateado,
                            selection = TextRange(formateado.length)
                        )
                        onNumeroChanged(formateado.filter { it.isDigit() })
                    } else {
                        numeroTarjetaValue = nuevo
                    }
                },
                label = { Text("Número de tarjeta") },
                placeholder = { Text("1234 5678 9012 3456") },
                isError = uiState.numeroTarjetaError != null,
                supportingText = {
                    if (uiState.numeroTarjetaError != null) {
                        Text(uiState.numeroTarjetaError!!, color = MaterialTheme.colorScheme.error)
                    }
                },
                leadingIcon = { Icon(Icons.Default.CreditCard, contentDescription = null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                enabled = habilitado,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = vencimientoValue,
                    onValueChange = { nuevo ->
                        if (nuevo.text != vencimientoValue.text) {
                            val formateado = formatearVencimiento(nuevo.text)
                            vencimientoValue = TextFieldValue(
                               text = formateado,
                               selection = TextRange(formateado.length)
                            )
                            onVencimientoChanged(formateado)
                        } else {
                            vencimientoValue = nuevo
                        }
                    },
                    label = { Text("MM/AA") },
                    placeholder = { Text("12/27") },
                    isError = uiState.vencimientoError != null,
                    supportingText = {
                        if (uiState.vencimientoError != null) {
                            Text(uiState.vencimientoError!!, color = MaterialTheme.colorScheme.error)
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    enabled = habilitado,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = uiState.cvv,
                    onValueChange = onCvvChanged,
                    label = { Text("CVV") },
                    placeholder = { Text("123") },
                    isError = uiState.cvvError != null,
                    supportingText = {
                        if (uiState.cvvError != null) {
                            Text(uiState.cvvError!!, color = MaterialTheme.colorScheme.error)
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    enabled = habilitado,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }
    }
}

// ─── Panel Yape / Plin ────────────────────────────────────────────────────────

@Composable
private fun PanelYapePlin() {
    val colorScheme = MaterialTheme.colorScheme

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Escanea el código QR con tu app",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface
            )

            // QR simulado generado con Canvas — sin dependencias externas
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .border(
                        width = 2.dp,
                        color = colorScheme.outline,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(12.dp)
            ) {
                val qrColor = colorScheme.onSurface
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val cellSize = size.width / 10f
                    // Patrón QR decorativo (no funcional, solo visual)
                    val pattern = listOf(
                        Pair(0, 0), Pair(1, 0), Pair(2, 0), Pair(0, 1), Pair(2, 1),
                        Pair(0, 2), Pair(1, 2), Pair(2, 2),
                        Pair(7, 0), Pair(8, 0), Pair(9, 0), Pair(7, 1), Pair(9, 1),
                        Pair(7, 2), Pair(8, 2), Pair(9, 2),
                        Pair(0, 7), Pair(1, 7), Pair(2, 7), Pair(0, 8), Pair(2, 8),
                        Pair(0, 9), Pair(1, 9), Pair(2, 9),
                        Pair(4, 0), Pair(5, 0), Pair(4, 2), Pair(6, 1),
                        Pair(3, 4), Pair(5, 4), Pair(7, 4), Pair(4, 5), Pair(6, 5),
                        Pair(3, 6), Pair(5, 6), Pair(8, 6), Pair(4, 7), Pair(7, 7),
                        Pair(5, 8), Pair(8, 8), Pair(9, 9), Pair(6, 9), Pair(3, 9)
                    )
                    pattern.forEach { (col, row) ->
                        drawRect(
                            color = qrColor,
                            topLeft = Offset(col * cellSize, row * cellSize),
                            size = androidx.compose.ui.geometry.Size(cellSize - 1f, cellSize - 1f)
                        )
                    }
                }
            }

            Text(
                text = "Fressco · Pago de pedido",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Una vez realizado el pago, presiona\n\"Confirmar pago\" para continuar.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}
