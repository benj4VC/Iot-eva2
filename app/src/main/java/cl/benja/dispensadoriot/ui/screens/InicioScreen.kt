package cl.benja.dispensadoriot.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cl.benja.dispensadoriot.Azul
import cl.benja.dispensadoriot.Fondo
import cl.benja.dispensadoriot.Turquesa

@Composable
fun InicioScreen(
    nivelComida: Int,
    mensaje: String,
    onControl: () -> Unit,
    onLogout: () -> Unit
) {
    var mostrarConfirmacion by remember {
        mutableStateOf(false)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Fondo)
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Encabezado y cierre de sesión
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "¡Hola!",
                fontSize = 28.sp,
                color = Azul
            )

            TextButton(
                onClick = {
                    mostrarConfirmacion = true
                }
            ) {
                Text(
                    text = "Cerrar sesión",
                    color = Azul
                )
            }
        }

        Text(
            text = "Estado de tu dispensador",
            fontSize = 16.sp,
            color = Color.DarkGray
        )

        Card(
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "🐾  Dispensador principal",
                    fontSize = 20.sp,
                    color = Azul
                )

                Text(
                    text = "● Conectado (simulado)",
                    color = Turquesa
                )

                HorizontalDivider()

                Text(
                    text = "Estado actual",
                    color = Color.Gray
                )

                Text(
                    text = "Listo para dispensar",
                    fontSize = 18.sp
                )

                Text(
                    text = "Nivel de alimento estimado",
                    color = Color.Gray
                )

                Text(
                    text = "$nivelComida %",
                    fontSize = 32.sp,
                    color = Azul
                )

                LinearProgressIndicator(
                    progress = { nivelComida / 100f },
                    modifier = Modifier.fillMaxWidth(),
                    color = Turquesa
                )
            }
        }

        Card(
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Última actividad",
                    color = Color.Gray
                )

                Spacer(Modifier.height(6.dp))

                Text(text = mensaje)
            }
        }

        Button(
            onClick = onControl,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Azul
            )
        ) {
            Text("Controlar dispensador")
        }

        Text(
            text = "Los valores mostrados son de demostración.",
            fontSize = 12.sp,
            color = Color.Gray
        )
    }

    // Confirmación antes de cerrar la sesión
    if (mostrarConfirmacion) {
        AlertDialog(
            onDismissRequest = {
                mostrarConfirmacion = false
            },
            title = {
                Text("Cerrar sesión")
            },
            text = {
                Text("¿Estás seguro de que deseas cerrar sesión?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        mostrarConfirmacion = false
                        onLogout()
                    }
                ) {
                    Text("Cerrar sesión")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        mostrarConfirmacion = false
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}