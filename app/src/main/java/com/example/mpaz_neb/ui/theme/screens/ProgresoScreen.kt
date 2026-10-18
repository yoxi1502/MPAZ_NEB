package com.example.mpaz_neb.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel as obtenerViewModel
import androidx.navigation.NavController
import com.example.mpaz_neb.model.ResultadoActividad
import com.example.mpaz_neb.viewmodel.ProgresoViewModel
import java.text.DateFormat
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgresoScreen(
    navController: NavController,
    progresoViewModel: ProgresoViewModel = obtenerViewModel()
) {
    val estado by progresoViewModel.uiState.collectAsState()

    Scaffold(
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

                    Button(
                        onClick = progresoViewModel::cargarProgreso
                    ) {
                        Text("Reintentar")
                    }
                }

                else -> {
                    TarjetaResultadoGuardado(
                        titulo = "Lectura",
                        resultado = estado.lectura
                    )

                    TarjetaResultadoGuardado(
                        titulo = "Matemática",
                        resultado = estado.matematica
                    )

                    Text(
                        text = "Se muestra el último intento completado de cada actividad en este dispositivo.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
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