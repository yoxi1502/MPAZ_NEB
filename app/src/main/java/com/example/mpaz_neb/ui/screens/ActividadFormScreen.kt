package com.example.mpaz_neb.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Title
import androidx.compose.material.icons.filled.Topic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel as obtenerViewModel
import androidx.navigation.NavController
import com.example.mpaz_neb.model.CampoActividad
import com.example.mpaz_neb.model.Dificultad
import com.example.mpaz_neb.model.EstadoActividad
import com.example.mpaz_neb.model.ModuloActividad
import com.example.mpaz_neb.repository.SesionAulaRepository
import com.example.mpaz_neb.validacion.Validadores
import com.example.mpaz_neb.viewmodel.ActividadFormViewModel

/** Formulario validado para crear o editar una actividad del catálogo. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActividadFormScreen(
    navController: NavController,
    formViewModel: ActividadFormViewModel = obtenerViewModel()
) {
    val estado by formViewModel.uiState.collectAsState()
    val scroll = rememberScrollState()

    // Si hay errores, se vuelve arriba para que se vean
    LaunchedEffect(estado.errorGeneral) {
        if (estado.errorGeneral != null) scroll.animateScrollTo(0)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (estado.esEdicion) "Editar actividad" else "Nueva actividad") },
                navigationIcon = {
                    IconButton(
                        onClick = { navController.popBackStack() },
                        enabled = !estado.guardando
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
                .verticalScroll(scroll)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 640.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (estado.cargando) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }

                AnimatedVisibility(visible = estado.errorGeneral != null) {
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer,
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Error, contentDescription = null)
                            Text(estado.errorGeneral.orEmpty())
                        }
                    }
                }

                CampoTexto(
                    valor = estado.nombre,
                    onCambio = formViewModel::cambiarNombre,
                    etiqueta = "Nombre de la actividad",
                    icono = Icons.Default.Title,
                    error = estado.errores[CampoActividad.NOMBRE],
                    maximo = Validadores.NOMBRE_MAX,
                    habilitado = !estado.guardando
                )

                Text("Módulo", fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ModuloActividad.entries.forEach { modulo ->
                        FilterChip(
                            selected = estado.modulo == modulo,
                            onClick = { formViewModel.cambiarModulo(modulo) },
                            label = { Text(modulo.etiqueta) },
                            enabled = !estado.guardando
                        )
                    }
                }

                SelectorLista(
                    titulo = "Curso recomendado",
                    valor = estado.curso,
                    opciones = SesionAulaRepository.CURSOS,
                    texto = { it },
                    habilitado = !estado.guardando,
                    onSeleccion = formViewModel::cambiarCurso
                )

                CampoTexto(
                    valor = estado.contenido,
                    onCambio = formViewModel::cambiarContenido,
                    etiqueta = "Contenido (ej: Comprensión lectora)",
                    icono = Icons.Default.Topic,
                    error = estado.errores[CampoActividad.CONTENIDO],
                    maximo = Validadores.NOMBRE_MAX,
                    habilitado = !estado.guardando
                )

                CampoTexto(
                    valor = estado.objetivo,
                    onCambio = formViewModel::cambiarObjetivo,
                    etiqueta = "Objetivo pedagógico",
                    icono = Icons.Default.Flag,
                    error = estado.errores[CampoActividad.OBJETIVO],
                    maximo = Validadores.TEXTO_LARGO_MAX,
                    habilitado = !estado.guardando,
                    variasLineas = true
                )

                SelectorLista(
                    titulo = "Dificultad",
                    valor = estado.dificultad,
                    opciones = Dificultad.entries,
                    texto = { it.etiqueta },
                    habilitado = !estado.guardando,
                    onSeleccion = formViewModel::cambiarDificultad
                )

                CampoTexto(
                    valor = estado.instrucciones,
                    onCambio = formViewModel::cambiarInstrucciones,
                    etiqueta = "Instrucciones para el estudiante",
                    icono = Icons.Default.Description,
                    error = estado.errores[CampoActividad.INSTRUCCIONES],
                    maximo = Validadores.TEXTO_LARGO_MAX,
                    habilitado = !estado.guardando,
                    variasLineas = true
                )

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    CampoTexto(
                        valor = estado.cantidadPreguntas,
                        onCambio = formViewModel::cambiarCantidadPreguntas,
                        etiqueta = "N.º de preguntas",
                        icono = Icons.Default.List,
                        error = estado.errores[CampoActividad.CANTIDAD_PREGUNTAS],
                        habilitado = !estado.guardando,
                        numerico = true,
                        modifier = Modifier.weight(1f)
                    )
                    CampoTexto(
                        valor = estado.intentosPermitidos,
                        onCambio = formViewModel::cambiarIntentos,
                        etiqueta = "Intentos",
                        icono = Icons.Default.Repeat,
                        error = estado.errores[CampoActividad.INTENTOS],
                        habilitado = !estado.guardando,
                        numerico = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                SelectorLista(
                    titulo = "Estado",
                    valor = estado.estado,
                    opciones = EstadoActividad.entries,
                    texto = { it.etiqueta },
                    habilitado = !estado.guardando,
                    onSeleccion = formViewModel::cambiarEstado
                )

                Text(
                    "Solo las actividades en estado Disponible se pueden asignar a estudiantes.",
                    style = MaterialTheme.typography.bodyMedium
                )

                Button(
                    onClick = { formViewModel.guardar { navController.popBackStack() } },
                    enabled = !estado.guardando && !estado.cargando,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 56.dp)
                ) {
                    Text(
                        when {
                            estado.guardando -> "Guardando…"
                            estado.esEdicion -> "Guardar cambios"
                            else -> "Crear actividad"
                        }
                    )
                }
            }
        }
    }
}

/** Campo de texto con ícono, mensaje de error y contador de caracteres. */
@Composable
private fun CampoTexto(
    valor: String,
    onCambio: (String) -> Unit,
    etiqueta: String,
    icono: ImageVector,
    error: String?,
    habilitado: Boolean,
    modifier: Modifier = Modifier,
    maximo: Int? = null,
    variasLineas: Boolean = false,
    numerico: Boolean = false
) {
    OutlinedTextField(
        value = valor,
        onValueChange = onCambio,
        label = { Text(etiqueta) },
        enabled = habilitado,
        singleLine = !variasLineas,
        minLines = if (variasLineas) 2 else 1,
        isError = error != null,
        keyboardOptions = if (numerico) {
            KeyboardOptions(keyboardType = KeyboardType.Number)
        } else {
            KeyboardOptions.Default
        },
        leadingIcon = { Icon(icono, contentDescription = null) },
        trailingIcon = {
            if (error != null) Icon(Icons.Default.Error, contentDescription = "Error")
        },
        supportingText = {
            Row(Modifier.fillMaxWidth()) {
                Text(error.orEmpty(), modifier = Modifier.weight(1f))
                if (maximo != null) Text("${valor.trim().length}/$maximo")
            }
        },
        modifier = modifier.fillMaxWidth()
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun <T> SelectorLista(
    titulo: String,
    valor: T,
    opciones: List<T>,
    texto: (T) -> String,
    habilitado: Boolean,
    onSeleccion: (T) -> Unit
) {
    var expandido by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expandido && habilitado,
        onExpandedChange = { if (habilitado) expandido = !expandido }
    ) {
        OutlinedTextField(
            value = texto(valor),
            onValueChange = {},
            readOnly = true,
            enabled = habilitado,
            label = { Text(titulo) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandido) },
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expandido && habilitado,
            onDismissRequest = { expandido = false }
        ) {
            opciones.forEach { opcion ->
                DropdownMenuItem(
                    text = { Text(texto(opcion)) },
                    onClick = {
                        onSeleccion(opcion)
                        expandido = false
                    }
                )
            }
        }
    }
}
