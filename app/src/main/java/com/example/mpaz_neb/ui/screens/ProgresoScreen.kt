package com.example.mpaz_neb.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel as obtenerViewModel
import androidx.navigation.NavController
import com.example.mpaz_neb.model.ProgresoUiState
import com.example.mpaz_neb.model.ResultadoActividad
import com.example.mpaz_neb.model.SesionAula
import com.example.mpaz_neb.repository.SesionAulaRepository
import com.example.mpaz_neb.validacion.Validadores
import com.example.mpaz_neb.viewmodel.ProgresoViewModel
import java.text.DateFormat
import java.util.Date

/**
 * Resultados: último resultado por módulo en el dispositivo e historial de sesiones
 * del microservicio ms-sesiones (ver, editar la observación y eliminar).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgresoScreen(
    navController: NavController,
    progresoViewModel: ProgresoViewModel = obtenerViewModel(factory = ProgresoViewModel.Factory)
) {
    val estado by progresoViewModel.uiState.collectAsState()
    val avisos = remember { SnackbarHostState() }

    LaunchedEffect(estado.mensaje) {
        estado.mensaje?.let {
            avisos.showSnackbar(it)
            progresoViewModel.limpiarMensaje()
        }
    }

    DialogoEditarObservacion(
        estado = estado,
        onCambio = progresoViewModel::cambiarObservacion,
        onGuardar = progresoViewModel::guardarObservacion,
        onCancelar = progresoViewModel::cancelarEdicion
    )

    estado.sesionAEliminar?.let { sesion ->
        AlertDialog(
            onDismissRequest = progresoViewModel::cancelarEliminar,
            title = { Text("¿Eliminar sesión?") },
            text = { Text("Se eliminará la sesión ${sesion.codigo} de ${sesion.estudiante} y sus resultados.") },
            confirmButton = {
                TextButton(onClick = progresoViewModel::confirmarEliminar) { Text("Eliminar") }
            },
            dismissButton = {
                TextButton(onClick = progresoViewModel::cancelarEliminar) { Text("Cancelar") }
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(avisos) },
        topBar = {
            TopAppBar(
                title = { Text("Resultados guardados") },
                navigationIcon = {
                    IconButton(
                        onClick = { navController.popBackStack() }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Últimos resultados",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            when {
                estado.cargando -> {
                    CircularProgressIndicator()
                    Text("Cargando resultados…")
                }

                estado.error != null -> {
                    Text(
                        text = estado.error.orEmpty(),
                        color = MaterialTheme.colorScheme.error
                    )
                    Button(onClick = progresoViewModel::cargarProgreso) {
                        Text("Reintentar")
                    }
                }

                else -> {
                    TarjetaResultadoGuardado(titulo = "Lectura", resultado = estado.lectura)
                    TarjetaResultadoGuardado(titulo = "Matemática", resultado = estado.matematica)
                    Text(
                        text = "Se muestra el último intento completado de cada actividad en este dispositivo.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            HorizontalDivider()

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Historial en el servidor",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = progresoViewModel::cargarHistorial,
                    enabled = !estado.cargandoHistorial
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "Actualizar historial")
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = estado.filtroEstudiante == null,
                    onClick = { progresoViewModel.filtrarEstudiante(null) },
                    label = { Text("Todos") }
                )
                SesionAulaRepository.ESTUDIANTES.forEach { estudiante ->
                    FilterChip(
                        selected = estado.filtroEstudiante == estudiante,
                        onClick = { progresoViewModel.filtrarEstudiante(estudiante) },
                        label = { Text(estudiante.substringBefore(" -")) }
                    )
                }
            }

            when {
                estado.cargandoHistorial -> LinearProgressIndicator(modifier = Modifier.fillMaxWidth())

                estado.errorHistorial != null -> {
                    Text(estado.errorHistorial.orEmpty(), color = MaterialTheme.colorScheme.error)
                    OutlinedButton(onClick = progresoViewModel::cargarHistorial) {
                        Text("Reintentar")
                    }
                }

                estado.historial.isEmpty() -> Text("Aún no hay sesiones registradas.")
            }

            estado.historial.forEach { sesion ->
                TarjetaSesionHistorial(
                    sesion = sesion,
                    eliminando = estado.eliminandoId == sesion.id,
                    onEditar = { progresoViewModel.editarObservacion(sesion) },
                    onEliminar = { progresoViewModel.pedirEliminar(sesion) }
                )
            }

            Text(
                text = "Resultados de apoyo a la práctica: no corresponden a notas ni diagnósticos.",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun DialogoEditarObservacion(
    estado: ProgresoUiState,
    onCambio: (String) -> Unit,
    onGuardar: () -> Unit,
    onCancelar: () -> Unit
) {
    val sesion = estado.sesionEditando ?: return
    AlertDialog(
        onDismissRequest = { if (!estado.guardandoObservacion) onCancelar() },
        title = { Text("Observación de ${sesion.estudiante.substringBefore(" -")}") },
        text = {
            OutlinedTextField(
                value = estado.observacionEditada,
                onValueChange = onCambio,
                label = { Text("Observación pedagógica") },
                isError = estado.errorObservacion != null,
                supportingText = {
                    Text(
                        estado.errorObservacion
                            ?: "${estado.observacionEditada.trim().length}/${Validadores.OBSERVACION_MAX}"
                    )
                },
                minLines = 3
            )
        },
        confirmButton = {
            TextButton(onClick = onGuardar, enabled = !estado.guardandoObservacion) {
                Text(if (estado.guardandoObservacion) "Guardando…" else "Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onCancelar, enabled = !estado.guardandoObservacion) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
private fun TarjetaSesionHistorial(
    sesion: SesionAula,
    eliminando: Boolean,
    onEditar: () -> Unit,
    onEliminar: () -> Unit
) {
    val fecha = remember(sesion.creadaEn) {
        DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)
            .format(Date(sesion.creadaEn))
    }
    var expandida by rememberSaveable(sesion.id) { mutableStateOf(false) }

    Card(
        onClick = { expandida = !expandida },
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text("${sesion.estudiante} · ${sesion.curso}", fontWeight = FontWeight.Bold)
            Text("${sesion.modulo.etiqueta}: ${sesion.contenido}")
            Text(
                "$fecha · ${sesion.estado.etiqueta} · ${sesion.intentosCompletados} intento(s)",
                style = MaterialTheme.typography.bodyMedium
            )
            sesion.ultimoResultado?.let {
                Text(
                    "Último intento: ${it.aciertos} aciertos, ${it.errores} errores",
                    color = MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.SemiBold
                )
            }

            if (expandida) {
                Text("Código: ${sesion.codigo}", style = MaterialTheme.typography.bodyMedium)
                if (sesion.asistente.isNotBlank()) {
                    Text("Asistente: ${sesion.asistente}", style = MaterialTheme.typography.bodyMedium)
                }
                sesion.resultados.forEachIndexed { indice, intento ->
                    Text(
                        "Intento ${indice + 1}: ${intento.aciertos}/${intento.total} correctas",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Text(
                    "Observación: ${sesion.observacion.ifBlank { "Sin observación registrada." }}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = onEditar, enabled = !eliminando) {
                        Icon(Icons.Default.Edit, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text("Observación")
                    }
                    OutlinedButton(onClick = onEliminar, enabled = !eliminando) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(if (eliminando) "Eliminando…" else "Eliminar")
                    }
                }
                if (sesion.abierta) {
                    Text(
                        "Solo se pueden eliminar sesiones finalizadas.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            } else {
                Text(
                    "Toca para ver el detalle",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun TarjetaResultadoGuardado(
    titulo: String,
    resultado: ResultadoActividad?
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = titulo,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            if (resultado == null) {
                Text("Todavía no hay un resultado guardado.")
            } else {
                val fecha = remember(resultado.fecha) {
                    DateFormat.getDateTimeInstance(
                        DateFormat.SHORT,
                        DateFormat.SHORT
                    ).format(Date(resultado.fecha))
                }

                Text(
                    text = "${resultado.aciertos} de ${resultado.total} respuestas correctas",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Fecha: $fecha",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
