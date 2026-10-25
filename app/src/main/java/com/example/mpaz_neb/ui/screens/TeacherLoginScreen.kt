package com.example.mpaz_neb.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel as obtenerViewModel
import androidx.navigation.NavController
import com.example.mpaz_neb.navigation.Screen
import com.example.mpaz_neb.repository.UsuarioRepository
import com.example.mpaz_neb.viewmodel.TeacherLoginViewModel

@Composable
fun TeacherLoginScreen(
    navController: NavController,
    teacherLoginViewModel: TeacherLoginViewModel = obtenerViewModel()
) {
    val estado by teacherLoginViewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current

    var mostrarClave by rememberSaveable { mutableStateOf(false) }

    fun ingresar() {
        focusManager.clearFocus()
        teacherLoginViewModel.ingresar {
            navController.navigate(Screen.AssignActivity.route) {
                popUpTo(Screen.Home.route) { inclusive = false }
                launchSingleTop = true
            }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 560.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                TextButton(
                    onClick = { navController.popBackStack() },
                    enabled = !estado.ingresando
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Volver")
                }

                Text(
                    text = "Equipo pedagógico",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Ingresa con tu cuenta protegida de prueba.",
                    style = MaterialTheme.typography.bodyLarge
                )

                OutlinedTextField(
                    value = estado.correo,
                    onValueChange = teacherLoginViewModel::actualizarCorreo,
                    label = { Text("Correo electrónico") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !estado.ingresando,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    ),
                    isError = estado.errorCorreo != null,
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                    trailingIcon = {
                        if (estado.errorCorreo != null) {
                            Icon(Icons.Default.Error, contentDescription = "Error en el correo")
                        }
                    },
                    supportingText = {
                        estado.errorCorreo?.let { Text(it) }
                    }
                )

                OutlinedTextField(
                    value = estado.clave,
                    onValueChange = teacherLoginViewModel::actualizarClave,
                    label = { Text("Contraseña") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !estado.ingresando,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(onDone = { ingresar() }),
                    visualTransformation = if (mostrarClave) {
                        VisualTransformation.None
                    } else {
                        PasswordVisualTransformation()
                    },
                    isError = estado.errorClave != null,
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                    trailingIcon = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (estado.errorClave != null) {
                                Icon(
                                    Icons.Default.Error,
                                    contentDescription = "Error en la contraseña"
                                )
                            }
                            IconButton(onClick = { mostrarClave = !mostrarClave }) {
                                Icon(
                                    imageVector = if (mostrarClave) {
                                        Icons.Default.VisibilityOff
                                    } else {
                                        Icons.Default.Visibility
                                    },
                                    contentDescription = if (mostrarClave) {
                                        "Ocultar contraseña"
                                    } else {
                                        "Mostrar contraseña"
                                    }
                                )
                            }
                        }
                    },
                    supportingText = {
                        estado.errorClave?.let { Text(it) }
                    }
                )

                // Error general de autenticación (credenciales incorrectas)
                AnimatedVisibility(visible = estado.errorGeneral != null) {
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer,
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(Icons.Default.Error, contentDescription = null)
                            Text(estado.errorGeneral.orEmpty())
                        }
                    }
                }

                Button(
                    onClick = { ingresar() },
                    enabled = !estado.ingresando,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 56.dp)
                ) {
                    if (estado.ingresando) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(Modifier.width(12.dp))
                        Text("Ingresando…")
                    } else {
                        Text("Ingresar")
                    }
                }

                CuentasDePrueba()
            }
        }
    }
}

@Composable
private fun CuentasDePrueba() {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.Info, contentDescription = null)
                Text("Cuentas de prueba (ficticias)", fontWeight = FontWeight.Bold)
            }
            UsuarioRepository.CUENTAS_PRUEBA.forEach { cuenta ->
                Text(
                    text = "${cuenta.perfil.etiqueta}: ${cuenta.correo} / ${cuenta.clave}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
