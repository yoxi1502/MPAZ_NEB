package com.example.mpaz_neb.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Error
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel as obtenerViewModel
import androidx.navigation.NavController
import com.example.mpaz_neb.navigation.Screen
import com.example.mpaz_neb.viewmodel.TeacherLoginViewModel

@Composable
fun TeacherLoginScreen(
    navController: NavController,
    teacherLoginViewModel: TeacherLoginViewModel = obtenerViewModel()
) {
    val estado by teacherLoginViewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current

    var mostrarClave by rememberSaveable {
        mutableStateOf(false)
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            TextButton(
                onClick = { navController.popBackStack() }
            ) {
                Text("← Volver")
            }

            Text(
                text = "Equipo pedagógico",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Ingresa tu correo y contraseña para continuar.",
                style = MaterialTheme.typography.bodyLarge
            )

            OutlinedTextField(
                value = estado.correo,
                onValueChange = teacherLoginViewModel::actualizarCorreo,
                label = { Text("Correo electrónico") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email
                ),
                isError = estado.errorCorreo != null,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = null
                    )
                },
                trailingIcon = {
                    if (estado.errorCorreo != null) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = "Error en el correo"
                        )
                    }
                },
                supportingText = {
                    estado.errorCorreo?.let { mensaje ->
                        Text(text = mensaje)
                    }
                }
            )

            OutlinedTextField(
                value = estado.clave,
                onValueChange = teacherLoginViewModel::actualizarClave,
                label = { Text("Contraseña") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password
                ),
                visualTransformation = if (mostrarClave) {
                    VisualTransformation.None
                } else {
                    PasswordVisualTransformation()
                },
                isError = estado.errorClave != null,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null
                    )
                },
                trailingIcon = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (estado.errorClave != null) {
                            Icon(
                                imageVector = Icons.Default.Error,
                                contentDescription = "Error en la contraseña"
                            )
                        }

                        IconButton(
                            onClick = {
                                mostrarClave = !mostrarClave
                            }
                        ) {
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
                    estado.errorClave?.let { mensaje ->
                        Text(text = mensaje)
                    }
                }
            )

            Button(
                onClick = {
                    if (teacherLoginViewModel.validarFormulario()) {
                        focusManager.clearFocus()

                        navController.navigate(Screen.AssignActivity.route) {
                            popUpTo(Screen.Home.route) {
                                inclusive = false
                            }
                            launchSingleTop = true
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp)
            ) {
                Text("Ingresar")
            }
        }
    }
}