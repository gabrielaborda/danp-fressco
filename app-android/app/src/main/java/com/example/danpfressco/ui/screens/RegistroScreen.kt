package com.example.danpfressco.ui.screens

import com.example.danpfressco.ui.state.RegistroUiState
import com.example.danpfressco.ui.viewmodel.RegistroViewModel

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.danpfressco.ui.theme.LogoTextStyle

@Composable
fun RegistroScreen(
    onRegistroSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit,
    viewModel: RegistroViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current
    var passwordVisible by remember { mutableStateOf(false) }

    // Navegar a Principal cuando el registro es exitoso
    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onRegistroSuccess()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Logo Fressco — mismo estilo que LoginScreen
                Text(
                    text = buildAnnotatedString {
                        withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary)) {
                            append("Fress")
                        }
                        withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.secondary)) {
                            append("co")
                        }
                    },
                    style = LogoTextStyle,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Text(
                    text = "Crea tu cuenta y empieza a ahorrar",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 32.dp)
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Crear Cuenta",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(bottom = 20.dp)
                        )

                        // Campo: Nombre
                        OutlinedTextField(
                            value = uiState.nombre,
                            onValueChange = { viewModel.onNombreChanged(it) },
                            label = { Text("Nombre completo") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Nombre"
                                )
                            },
                            isError = uiState.nombreError != null,
                            supportingText = {
                                uiState.nombreError?.let {
                                    Text(text = it, color = MaterialTheme.colorScheme.error)
                                }
                            },
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Words,
                                keyboardType = KeyboardType.Text,
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = { focusManager.moveFocus(FocusDirection.Down) }
                            ),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // Campo: Email
                        OutlinedTextField(
                            value = uiState.email,
                            onValueChange = { viewModel.onEmailChanged(it) },
                            label = { Text("Correo Electrónico") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Email,
                                    contentDescription = "Email"
                                )
                            },
                            isError = uiState.emailError != null,
                            supportingText = {
                                uiState.emailError?.let {
                                    Text(text = it, color = MaterialTheme.colorScheme.error)
                                }
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = { focusManager.moveFocus(FocusDirection.Down) }
                            ),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // Campo: Teléfono
                        OutlinedTextField(
                            value = uiState.telefono,
                            onValueChange = { viewModel.onTelefonoChanged(it) },
                            label = { Text("Teléfono") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Phone,
                                    contentDescription = "Teléfono"
                                )
                            },
                            isError = uiState.telefonoError != null,
                            supportingText = {
                                uiState.telefonoError?.let {
                                    Text(text = it, color = MaterialTheme.colorScheme.error)
                                }
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Phone,
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = { focusManager.moveFocus(FocusDirection.Down) }
                            ),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // Campo: Contraseña
                        OutlinedTextField(
                            value = uiState.password,
                            onValueChange = { viewModel.onPasswordChanged(it) },
                            label = { Text("Contraseña") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "Contraseña"
                                )
                            },
                            trailingIcon = {
                                val image = if (passwordVisible)
                                    Icons.Default.Visibility
                                else
                                    Icons.Default.VisibilityOff
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        imageVector = image,
                                        contentDescription = if (passwordVisible) "Ocultar contraseña" else "Mostrar contraseña"
                                    )
                                }
                            },
                            visualTransformation = if (passwordVisible)
                                VisualTransformation.None
                            else
                                PasswordVisualTransformation(),
                            isError = uiState.passwordError != null,
                            supportingText = {
                                uiState.passwordError?.let {
                                    Text(text = it, color = MaterialTheme.colorScheme.error)
                                }
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    focusManager.clearFocus()
                                    viewModel.registrar()
                                }
                            ),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Error general del servidor
                        uiState.errorMessage?.let { errorMsg ->
                            Text(
                                text = errorMsg,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 12.dp)
                            )
                        }

                        // Botón Registrarse
                        Button(
                            onClick = {
                                focusManager.clearFocus()
                                viewModel.registrar()
                            },
                            enabled = !uiState.isLoading,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            if (uiState.isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(
                                    text = "Registrarse",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Enlace a Login
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "¿Ya tienes una cuenta? ",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    TextButton(
                        onClick = onNavigateToLogin,
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(
                            text = "Inicia sesión",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }
        }
    }
}
