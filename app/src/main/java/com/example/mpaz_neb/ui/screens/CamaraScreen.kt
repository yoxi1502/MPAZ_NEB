package com.example.mpaz_neb.ui.screens

import android.Manifest
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.MediaStore
import android.provider.Settings
import android.view.Surface
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.compose.CameraXViewfinder
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.core.SurfaceRequest
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CamaraScreen(navController: NavController) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val vista = LocalView.current
    val executor = remember(context) {
        ContextCompat.getMainExecutor(context)
    }

    var permisoConcedido by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    var surfaceRequest by remember {
        mutableStateOf<SurfaceRequest?>(null)
    }

    var camaraLista by remember { mutableStateOf(false) }
    var guardando by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var mensaje by rememberSaveable { mutableStateOf<String?>(null) }

    val preview = remember { Preview.Builder().build() }
    val imageCapture = remember {
        ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .build()
    }

    val solicitarPermiso = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { concedido ->
        permisoConcedido = concedido
        error = if (concedido) {
            null
        } else {
            "No se autorizó la cámara. Puedes seguir usando las otras actividades."
        }
    }

    // Actualiza el permiso al regresar desde los ajustes del teléfono.
    DisposableEffect(lifecycleOwner, context) {
        val observador = LifecycleEventObserver { _, evento ->
            if (evento == Lifecycle.Event.ON_RESUME) {
                permisoConcedido = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED
            }
        }

        lifecycleOwner.lifecycle.addObserver(observador)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observador)
        }
    }

    // Conecta la cámara solamente mientras esta pantalla está activa.
    DisposableEffect(permisoConcedido, lifecycleOwner) {
        var activo = true
        var provider: ProcessCameraProvider? = null

        camaraLista = false
        surfaceRequest = null

        if (permisoConcedido) {
            error = null

            val futuro = ProcessCameraProvider.getInstance(context)

            futuro.addListener({
                if (activo) {
                    try {
                        val camaraProvider = futuro.get()
                        provider = camaraProvider

                        val selector = when {
                            camaraProvider.hasCamera(
                                CameraSelector.DEFAULT_BACK_CAMERA
                            ) -> CameraSelector.DEFAULT_BACK_CAMERA

                            camaraProvider.hasCamera(
                                CameraSelector.DEFAULT_FRONT_CAMERA
                            ) -> CameraSelector.DEFAULT_FRONT_CAMERA

                            else -> null
                        }

                        if (selector == null) {
                            error = "Este dispositivo no tiene una cámara disponible."
                        } else {
                            preview.setSurfaceProvider { solicitud ->
                                if (activo) {
                                    surfaceRequest = solicitud
                                } else {
                                    solicitud.willNotProvideSurface()
                                }
                            }

                            camaraProvider.bindToLifecycle(
                                lifecycleOwner,
                                selector,
                                preview,
                                imageCapture
                            )

                            camaraLista = true
                        }
                    } catch (e: Exception) {
                        error = "No se pudo iniciar la cámara. Vuelve a intentarlo."
                    }
                }
            }, executor)
        }

        onDispose {
            activo = false
            provider?.unbind(preview, imageCapture)
            preview.setSurfaceProvider(null)
        }
    }

    BackHandler(enabled = guardando) {
        // Espera a que termine el guardado antes de salir.
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Fotografía tu actividad") },
                navigationIcon = {
                    IconButton(
                        enabled = !guardando,
                        onClick = { navController.popBackStack() }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                }
            )
        }
    ) { paddingInterior ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingInterior)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "¡Muéstranos tu trabajo!",
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Coloca tu dibujo o ejercicio frente a la cámara. " +
                        "La foto se guardará en la galería de este dispositivo.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )

            if (!permisoConcedido) {
                Spacer(modifier = Modifier.weight(1f))

                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = null,
                    modifier = Modifier.size(72.dp),
                    tint = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = "Necesitamos tu permiso para fotografiar la actividad.",
                    textAlign = TextAlign.Center
                )

                Button(
                    onClick = {
                        solicitarPermiso.launch(Manifest.permission.CAMERA)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Permitir cámara")
                }

                TextButton(
                    onClick = {
                        val intent = Intent(
                            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.parse("package:${context.packageName}")
                        )
                        context.startActivity(intent)
                    }
                ) {
                    Text("Abrir ajustes de permisos")
                }

                Spacer(modifier = Modifier.weight(1f))
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    val solicitud = surfaceRequest

                    if (solicitud != null) {
                        CameraXViewfinder(
                            surfaceRequest = solicitud,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else if (error == null) {
                        CircularProgressIndicator(color = Color.White)
                    } else {
                        Text(
                            text = "Cámara no disponible",
                            color = Color.White,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }

                Button(
                    enabled = camaraLista && !guardando,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 56.dp),
                    onClick = {
                        val tienePermiso = ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.CAMERA
                        ) == PackageManager.PERMISSION_GRANTED

                        if (!tienePermiso) {
                            permisoConcedido = false
                        } else {
                            guardando = true
                            error = null
                            mensaje = null

                            try {
                                imageCapture.targetRotation =
                                    vista.display?.rotation ?: Surface.ROTATION_0

                                val valores = ContentValues().apply {
                                    put(
                                        MediaStore.Images.Media.DISPLAY_NAME,
                                        "Actividad_${UUID.randomUUID()}.jpg"
                                    )
                                    put(
                                        MediaStore.Images.Media.MIME_TYPE,
                                        "image/jpeg"
                                    )
                                    put(
                                        MediaStore.Images.Media.RELATIVE_PATH,
                                        "Pictures/MPAZ_NEB"
                                    )
                                }

                                val opciones = ImageCapture.OutputFileOptions.Builder(
                                    context.contentResolver,
                                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                    valores
                                ).build()

                                imageCapture.takePicture(
                                    opciones,
                                    executor,
                                    object : ImageCapture.OnImageSavedCallback {
                                        override fun onImageSaved(
                                            outputFileResults: ImageCapture.OutputFileResults
                                        ) {
                                            guardando = false
                                            mensaje = "¡Foto guardada en la galería!"
                                        }

                                        override fun onError(
                                            exception: ImageCaptureException
                                        ) {
                                            guardando = false
                                            error = "No se pudo guardar la foto. Intenta otra vez."
                                        }
                                    }
                                )
                            } catch (e: Exception) {
                                guardando = false
                                error = "No se pudo tomar la foto. Intenta otra vez."
                            }
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (guardando) "Guardando..." else "Tomar foto")
                }
            }

            if (guardando) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            mensaje?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )
            }

            error?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}