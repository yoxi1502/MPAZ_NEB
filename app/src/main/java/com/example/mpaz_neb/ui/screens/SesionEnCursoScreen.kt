package com.example.mpaz_neb.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel as obtenerViewModel
import androidx.navigation.NavHostController
import com.example.mpaz_neb.model.EstadoSesion
import com.example.mpaz_neb.validacion.Validadores
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Error
import com.example.mpaz_neb.navigation.Screen
import com.example.mpaz_neb.viewmodel.SesionAulaViewModel

@Composable
fun SesionEnCursoScreen(
    navController: NavHostController,
    sesionViewModel: SesionAulaViewModel = obtenerViewModel()
) {
    val estado by sesionViewModel.uiState.collectAsState()
    val sesion = estado.sesion
    var confirmarCierre by rememberSaveable { mutableStateOf(false) }
    BackHandler(enabled = estado.guardando) { }

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize().safeDrawingPadding()
                .verticalScroll(rememberScrollState()).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            TextButton(enabled = !estado.guardando, onClick = { navController.popBackStack() }) {
                Text("← Volver al panel")
            }
            Text("Sesión en curso", style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold)
            if (estado.cargando) CircularProgressIndicator()
            estado.error?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
                TextButton(enabled = !estado.guardando, onClick = sesionViewModel::cargarSesion) {
                    Text("Reintentar lectura")
                }
            }

            if (!estado.cargando && sesion == null) {
                Text("Todavía no hay una sesión. Vuelve al panel para asignar una actividad.")
            }
            if (sesion != null) {
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Código de acceso")
                        Text(sesion.codigo, style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Text(if (sesion.estado == EstadoSesion.FINALIZADA) "Este código ya está cerrado."
                        else "Válido en este dispositivo hasta finalizar la sesión.")
                    }
                }
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(sesion.estudiante, fontWeight = FontWeight.Bold)
                        Text(sesion.curso)
                        if (sesion.asistente.isNotBlank()) {
                            Text("Asistente responsable: ${sesion.asistente}")
                        }
                        Text("${sesion.modulo.etiqueta} · ${sesion.contenido}")
                        Text("Estado: ${sesion.estado.etiqueta}")
                        Text("Prácticas completadas: ${sesion.intentosCompletados}")
                        sesion.ultimoResultado?.let {
                            Text("Último resultado: ${it.aciertos} aciertos y ${it.errores} errores.")
                        }
                    }
                }

                FormularioObservacion(
                    texto = estado.observacion,
                    error = estado.errorObservacion,
                    guardando = estado.guardandoObservacion,
                    guardada = estado.observacionGuardada,
                    onCambio = sesionViewModel::actualizarObservacion,
                    onGuardar = sesionViewModel::guardarObservacion
                )

                if (sesion.estado != EstadoSesion.FINALIZADA) {
                    Button(enabled = !estado.guardando && !estado.cargando,
                        onClick = {
                            navController.navigate(Screen.StudentCode.route) {
                                popUpTo(Screen.Home.route) { inclusive = false }
                                launchSingleTop = true
                            }
                        }, modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)) {
                        Text("Entregar dispositivo al estudiante")
                    }
                    OutlinedButton(enabled = !estado.guardando && !estado.cargando,
                        onClick = { confirmarCierre = true },
                        modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)) {
                        Text(if (estado.guardando) "Finalizando…" else "Finalizar sesión")
                    }
                }
                OutlinedButton(enabled = !estado.guardando, onClick = {
                    navController.navigate(Screen.Progreso.route) { launchSingleTop = true }
                }, modifier = Modifier.fillMaxWidth()) { Text("Ver últimos resultados guardados") }
            }
        }
    }

    if (confirmarCierre) {
        AlertDialog(
            onDismissRequest = { if (!estado.guardando) confirmarCierre = false },
            title = { Text("¿Finalizar la sesión?") },
            text = { Text("El código dejará de funcionar. Las prácticas completadas quedarán guardadas. " +
                    "Si el estudiante aún está respondiendo, espera a que termine.") },
            confirmButton = {
                TextButton(enabled = !estado.guardando, onClick = {
                    sesionViewModel.finalizarSesion {
                        confirmarCierre = false
                        navController.popBackStack()
                    }
                }) { Text("Finalizar") }
            },
            dismissButton = {
                TextButton(enabled = !estado.guardando, onClick = { confirmarCierre = false }) {
                    Text("Continuar sesión")
                }
            }
        )
    }
}

/**
 * Formulario para que la asistente registre una observación pedagógica general
 * y ficticia de la sesión. La validación vive en Validadores (vía ViewModel).
 */
@Composable
private fun FormularioObservacion(
    texto: String,
    error: String?,
    guardando: Boolean,
    guardada: Boolean,
    onCambio: (String) -> Unit,
    onGuardar: () -> Unit
) {
    Card(Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Observación pedagógica", fontWeight = FontWeight.Bold)
            Text(
                "Registra solo comentarios generales sobre la práctica. " +
                        "No incluyas diagnósticos, notas ni datos personales.",
                style = MaterialTheme.typography.bodyMedium
            )

            OutlinedTextField(
                value = texto,
                onValueChange = onCambio,
                enabled = !guardando,
                label = { Text("Observación") },
                placeholder = { Text("Ej: Necesitó apoyo para ubicar la idea principal.") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                isError = error != null,
                leadingIcon = { Icon(Icons.Default.EditNote, contentDescription = null) },
                trailingIcon = {
                    when {
                        error != null -> Icon(
                            Icons.Default.Error,
                            contentDescription = "Error en la observación"
                        )
                        guardada -> Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = "Observación guardada",
                            tint = MaterialTheme.colorScheme.secondary
                        )
                    }
                },
                supportingText = {
                    Row(Modifier.fillMaxWidth()) {
                        Text(
                            text = error ?: if (guardada) "Observación guardada." else "",
                            modifier = Modifier.weight(1f)
                        )
                        Text("${texto.trim().length}/${Validadores.OBSERVACION_MAX}")
                    }
                }
            )

            AnimatedVisibility(visible = guardando) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            Button(
                onClick = onGuardar,
                enabled = !guardando,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 52.dp)
            ) {
                Text(if (guardando) "Guardando…" else "Guardar observación")
            }
        }
    }
}
