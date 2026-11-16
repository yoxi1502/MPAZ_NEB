package com.example.mpaz_neb.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel as obtenerViewModel
import androidx.navigation.NavController
import com.example.mpaz_neb.data.model.Post
import com.example.mpaz_neb.model.ComunicadosUiState
import com.example.mpaz_neb.validacion.Validadores
import com.example.mpaz_neb.viewmodel.PostViewModel

/** Comunicados del equipo pedagógico obtenidos desde la API REST (JSONPlaceholder). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostScreen(
    navController: NavController,
    postViewModel: PostViewModel = obtenerViewModel()
) {
    // Observamos el flujo de datos del ViewModel
    val estado by postViewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var porEliminar by remember { mutableStateOf<Post?>(null) }

    // Muestra el resultado de cada petición (POST, PUT, DELETE o error)
    LaunchedEffect(estado.mensaje) {
        estado.mensaje?.let {
            snackbarHostState.showSnackbar(it)
            postViewModel.limpiarMensaje()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Comunicados (API REST)") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = postViewModel::cargarPosts, enabled = !estado.cargando) {
                        Icon(Icons.Default.Refresh, contentDescription = "Actualizar")
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    "Datos de prueba de jsonplaceholder.typicode.com: el servidor responde, pero no guarda los cambios.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            item {
                FormularioComunicado(
                    estado = estado,
                    onTituloChange = postViewModel::onTituloChange,
                    onCuerpoChange = postViewModel::onCuerpoChange,
                    onGuardar = postViewModel::guardar,
                    onCancelar = postViewModel::cancelarEdicion
                )
            }

            if (estado.cargando) {
                item {
                    Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            }

            estado.error?.let { mensaje ->
                item { ErrorDeConexion(mensaje, onReintentar = postViewModel::cargarPosts) }
            }

            items(estado.posts, key = { it.id ?: it.hashCode() }) { post ->
                TarjetaPost(
                    post = post,
                    onEditar = { postViewModel.editar(post) },
                    onEliminar = { porEliminar = post }
                )
            }
        }
    }

    // Confirmación antes de enviar el DELETE
    porEliminar?.let { post ->
        AlertDialog(
            onDismissRequest = { porEliminar = null },
            title = { Text("¿Eliminar comunicado?") },
            text = { Text("\"${post.title}\" se quitará de la lista.") },
            confirmButton = {
                TextButton(onClick = {
                    postViewModel.eliminar(post)
                    porEliminar = null
                }) { Text("Eliminar") }
            },
            dismissButton = {
                TextButton(onClick = { porEliminar = null }) { Text("Cancelar") }
            }
        )
    }
}

@Composable
private fun FormularioComunicado(
    estado: ComunicadosUiState,
    onTituloChange: (String) -> Unit,
    onCuerpoChange: (String) -> Unit,
    onGuardar: () -> Unit,
    onCancelar: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                if (estado.editando) "Editar comunicado (PUT)" else "Nuevo comunicado (POST)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            OutlinedTextField(
                value = estado.titulo,
                onValueChange = onTituloChange,
                label = { Text("Título") },
                singleLine = true,
                isError = estado.errorTitulo != null,
                supportingText = {
                    Text(estado.errorTitulo ?: "${estado.titulo.trim().length}/${Validadores.NOMBRE_MAX}")
                },
                trailingIcon = {
                    if (estado.errorTitulo != null) Icon(Icons.Default.Error, contentDescription = "Error")
                },
                enabled = !estado.guardando,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = estado.cuerpo,
                onValueChange = onCuerpoChange,
                label = { Text("Contenido") },
                minLines = 3,
                isError = estado.errorCuerpo != null,
                supportingText = {
                    Text(estado.errorCuerpo ?: "${estado.cuerpo.trim().length}/${Validadores.TEXTO_LARGO_MAX}")
                },
                trailingIcon = {
                    if (estado.errorCuerpo != null) Icon(Icons.Default.Error, contentDescription = "Error")
                },
                enabled = !estado.guardando,
                modifier = Modifier.fillMaxWidth()
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onGuardar, enabled = !estado.guardando) {
                    if (estado.guardando) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    } else {
                        Text(if (estado.editando) "Guardar cambios" else "Publicar")
                    }
                }
                if (estado.editando) {
                    OutlinedButton(onClick = onCancelar, enabled = !estado.guardando) {
                        Text("Cancelar")
                    }
                }
            }
        }
    }
}

@Composable
private fun ErrorDeConexion(mensaje: String, onReintentar: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(Icons.Default.CloudOff, contentDescription = null)
            Text(mensaje, modifier = Modifier.weight(1f))
            TextButton(onClick = onReintentar) { Text("Reintentar") }
        }
    }
}

@Composable
private fun TarjetaPost(post: Post, onEditar: () -> Unit, onEliminar: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = post.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = post.body, style = MaterialTheme.typography.bodyMedium)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = onEditar) {
                    Icon(Icons.Default.Edit, contentDescription = "Editar")
                }
                IconButton(onClick = onEliminar) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Eliminar",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}
