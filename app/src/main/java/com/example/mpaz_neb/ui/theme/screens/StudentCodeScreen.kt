package com.example.mpaz_neb.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.mpaz_neb.navigation.Screen
import com.example.mpaz_neb.viewmodel.MainViewModel

@Composable
fun StudentCodeScreen(
    navController: NavController,
    viewModel: MainViewModel
) {
    var code by remember { mutableStateOf("MPAZ-402") }
    val bluePrimary = Color(0xFF2563EB)

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFF8FAFC)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Botón Volver a la pantalla de inicio
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                TextButton(onClick = { navController.popBackStack() }) {
                    Text("← Volver", color = bluePrimary, fontSize = 14.sp)
                }
            }

            // Contenido Central
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Ingresa tu código",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Pídele el código de sesión a tu\nasistente o profesora",
                    fontSize = 14.sp,
                    color = Color(0xFF64748B),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(28.dp))

                // Input de Código
                OutlinedTextField(
                    value = code,
                    onValueChange = { code = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp),
                    shape = RoundedCornerShape(12.dp),
                    textStyle = LocalTextStyle.current.copy(
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        color = Color(0xFF0F172A)
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    )
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Botón Comenzar con validación y guardado
                Button(
                    onClick = {
                        if (code.isNotBlank()) {
                            // Guardar el código en el ViewModel (si tienes una función para ello)
                            // viewModel.setStudentCode(code)

                            // Navegar a la pantalla de Lectura
                            navController.navigate(Screen.Lectura.route) {
                                // Evita acumular pantallas duplicadas en la pila de navegación
                                launchSingleTop = true
                            }
                        }
                    },
                    enabled = code.isNotBlank(), // Deshabilita el botón si el código está vacío
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = bluePrimary,
                        disabledContainerColor = bluePrimary.copy(alpha = 0.5f)
                    )
                ) {
                    Text(
                        text = "¡COMENZAR!",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Tarjeta informativa inferior
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "¿No tienes un código?",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color(0xFF0F172A)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Tu asistente de aula te asignará una actividad de Lectura o Matemática para comenzar la sesión de hoy.",
                            fontSize = 12.sp,
                            color = Color(0xFF64748B),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Text(
                text = "Escuela Particular Marcela Paz • San Bernardo",
                fontSize = 11.sp,
                color = Color(0xFF94A3B8)
            )
        }
    }
}