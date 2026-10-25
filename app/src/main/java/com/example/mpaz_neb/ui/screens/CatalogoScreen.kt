package com.example.mpaz_neb.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel as obtenerViewModel
import androidx.navigation.NavController
import com.example.mpaz_neb.model.Actividad
import com.example.mpaz_neb.model.EstadoActividad
import com.example.mpaz_neb.model.ModuloActividad
import com.example.mpaz_neb.navigation.Screen
import com.example.mpaz_neb.repository.SesionAulaRepository
import com.example.mpaz_neb.viewmodel.CatalogoViewModel

/** Catálogo de actividades: consulta, filtros y activación (perfil docente/UTP). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogoScreen(
    navController: NavController,
    catalogoViewModel: CatalogoViewModel = obtenerViewModel()
) {
    val estado by catalogoViewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Catálogo de actividades") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { navController.navigate(Screen.ActividadForm.crear(null)) },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Nueva actividad") }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    "Filtrar por módulo",
                    style = MaterialTheme.typography.labelLarge
                )
                Spacer(Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = estado.filtroModulo == null,
                        onClick = { catalogoViewModel.filtrarModulo(null) },
                        label = { Text("Todos") }
                    )
                    ModuloActividad.entries.forEach { modulo ->
                        FilterChip(
                            selected = estado.filtroModulo == modulo,
                            onClick = { catalogoViewModel.filtrarModulo(modulo) },
                            label = { Text(modulo.etiqueta) }
                        )
                    }
                }
            }

            item {
                Text("Filtrar por curso", style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(4.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        FilterChip(
                            selected = estado.filtroCurso == null,
                            onClick = { catalogoViewModel.filtrarCurso(null) },
                            label = { Text("Todos") }
                        )
                    }
                    items(SesionAulaRepository.CURSOS) { curso ->
                        FilterChip(
                            selected = estado.filtroCurso == curso,
                            onClick = { catalogoViewModel.filtrarCurso(curso) },
                            label = { Text(curso) }
                        )
                    }
                }
            }

            if (estado.cargando) {
                item { LinearProgressIndicator(modifier = Modifier.fillMaxWidth()) }
            }

            estado.error?.let { mensaje ->
                item { Text(mensaje, color = MaterialTheme.colorScheme.error) }
            }

            if (!estado.cargando && estado.filtradas.isEmpty()) {
                item { Text("No hay actividades con estos filtros.") }
            }

            items(estado.filtradas, key = { it.id }) { actividad ->
                TarjetaActividad(
                    actividad = actividad,
                    onEditar = { navController.navigate(Screen.ActividadForm.crear(actividad.id)) },
                    onDisponible = { catalogoViewModel.cambiarDisponible(actividad, it) },
                    modifier = Modifier.animateItem()
                )
            }
        }
    }
}

@Composable
private fun TarjetaActividad(
    actividad: Actividad,
    onEditar: () -> Unit,
    onDisponible: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val colorEstado = when (actividad.estado) {
        EstadoActividad.DISPONIBLE -> MaterialTheme.colorScheme.secondary
        EstadoActividad.BORRADOR -> MaterialTheme.colorScheme.tertiary
        EstadoActividad.DESACTIVADA -> MaterialTheme.colorScheme.outline
    }

    Card(modifier = modifier.fillMaxWidth().animateContentSize()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(actividad.nombre, fontWeight = FontWeight.Bold)
                    Text(
                        "${actividad.modulo.etiqueta} · ${actividad.curso} · ${actividad.dificultad.etiqueta}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                IconButton(onClick = onEditar) {
                    Icon(Icons.Default.Edit, contentDescription = "Editar ${actividad.nombre}")
                }
            }
            Text("Objetivo: ${actividad.objetivo}", style = MaterialTheme.typography.bodyMedium)
            Text(
                "${actividad.cantidadPreguntas} preguntas · ${actividad.intentosPermitidos} intentos permitidos",
                style = MaterialTheme.typography.bodyMedium
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                AssistChip(
                    onClick = {},
                    label = { Text(actividad.estado.etiqueta) },
                    colors = AssistChipDefaults.assistChipColors(labelColor = colorEstado)
                )
                Spacer(Modifier.weight(1f))
                Text("Disponible", style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.width(8.dp))
                Switch(
                    checked = actividad.estado == EstadoActividad.DISPONIBLE,
                    onCheckedChange = onDisponible
                )
            }
        }
    }
}
