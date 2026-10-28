package cl.benja.dispensadoriot.ui.screens

import android.app.TimePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cl.benja.dispensadoriot.Azul
import cl.benja.dispensadoriot.Turquesa
import cl.benja.dispensadoriot.data.Horario

@Composable
fun ControlScreen(
    dispositivo: String,
    onCambiarDispositivo: () -> Unit,
    nivelComida: Int,
    horarios: SnapshotStateList<Horario>,
    mensaje: String,
    onDispensar: () -> Unit,
    onGuardarHorario: (Long?, String, List<String>) -> Unit,
    onAlternarHorario: (Long) -> Unit,
    onEliminarHorario: (Long) -> Unit
) {
    var confirmarDispensacion by remember {
        mutableStateOf(false)
    }

    var mostrarDialogoHorario by remember {
        mutableStateOf(false)
    }

    var horarioEditando by remember {
        mutableStateOf<Long?>(null)
    }

    var horaElegida by remember {
        mutableStateOf("08:30")
    }

    var diasElegidos by remember {
        mutableStateOf(
            setOf("Lun", "Mar", "Mié", "Jue", "Vie")
        )
    }

    var errorHorario by remember {
        mutableStateOf("")
    }

    val contexto = LocalContext.current

    val diasSemana = listOf(
        "Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Control del dispensador",
            fontSize = 26.sp,
            color = Azul
        )

        // Tarjeta de control manual
        Card(
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            )
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Dispensador principal",
                    fontSize = 19.sp
                )

                Text("Nivel de alimento: $nivelComida %")

                Text("Estado: Listo para dispensar")

                Button(
                    onClick = {
                        confirmarDispensacion = true
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Turquesa
                    )
                ) {
                    Text(
                        text = "Dispensar ahora",
                        color = Color.White
                    )
                }

                Text(
                    text = "La operación se simulará en esta etapa.",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }

        // Selector del dispositivo de demostración
        OutlinedButton(
            onClick = onCambiarDispositivo,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Dispositivo de prueba: $dispositivo · Cambiar")
        }

        Text(
            text = "Última actividad: $mensaje",
            fontSize = 13.sp
        )

        HorizontalDivider()

        // Sección de programación
        Text(
            text = "Programar alimentación",
            fontSize = 23.sp,
            color = Azul
        )

        Button(
            onClick = {
                horarioEditando = null
                horaElegida = "08:30"
                diasElegidos = setOf(
                    "Lun", "Mar", "Mié", "Jue", "Vie"
                )
                errorHorario = ""
                mostrarDialogoHorario = true
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Azul
            )
        ) {
            Text("+ Agregar horario")
        }

        if (horarios.isEmpty()) {
            Text(
                text = "Todavía no hay horarios programados.",
                color = Color.Gray
            )
        }

        // Lista de horarios existentes
        horarios.forEach { horario ->
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = horario.hora,
                            fontSize = 25.sp,
                            color = Azul
                        )

                        Text(
                            text = if (horario.activo) {
                                "Activo"
                            } else {
                                "Inactivo"
                            },
                            color = if (horario.activo) {
                                Turquesa
                            } else {
                                Color.Gray
                            }
                        )
                    }

                    Text(
                        text = "Días: ${horario.dias.joinToString(", ")}"
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        TextButton(
                            onClick = {
                                horarioEditando = horario.id
                                horaElegida = horario.hora
                                diasElegidos = horario.dias.toSet()
                                errorHorario = ""
                                mostrarDialogoHorario = true
                            }
                        ) {
                            Text("Editar")
                        }

                        TextButton(
                            onClick = {
                                onAlternarHorario(horario.id)
                            }
                        ) {
                            Text(
                                if (horario.activo) {
                                    "Desactivar"
                                } else {
                                    "Activar"
                                }
                            )
                        }

                        TextButton(
                            onClick = {
                                onEliminarHorario(horario.id)
                            }
                        ) {
                            Text("Eliminar")
                        }
                    }
                }
            }
        }

        Text(
            text = "Los horarios son simulados y no activan un dispensador físico.",
            fontSize = 12.sp,
            color = Color.Gray
        )
    }

    // Confirmación de dispensación
    if (confirmarDispensacion) {
        AlertDialog(
            onDismissRequest = {
                confirmarDispensacion = false
            },
            title = {
                Text("Confirmar dispensación")
            },
            text = {
                Text("¿Deseas registrar una orden de dispensación?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDispensar()
                        confirmarDispensacion = false
                    }
                ) {
                    Text("Confirmar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        confirmarDispensacion = false
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Diálogo para crear o editar un horario
    if (mostrarDialogoHorario) {
        AlertDialog(
            onDismissRequest = {
                mostrarDialogoHorario = false
            },
            title = {
                Text(
                    if (horarioEditando == null) {
                        "Agregar horario"
                    } else {
                        "Editar horario"
                    }
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .heightIn(max = 400.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Hora de alimentación")

                    OutlinedButton(
                        onClick = {
                            val partes = horaElegida.split(":")
                            val horaInicial =
                                partes.getOrNull(0)?.toIntOrNull() ?: 8
                            val minutoInicial =
                                partes.getOrNull(1)?.toIntOrNull() ?: 30

                            TimePickerDialog(
                                contexto,
                                { _, hora, minuto ->
                                    horaElegida = String.format(
                                        java.util.Locale.getDefault(),
                                        "%02d:%02d",
                                        hora,
                                        minuto
                                    )
                                },
                                horaInicial,
                                minutoInicial,
                                true
                            ).show()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("◷  $horaElegida")
                    }

                    Text("Repetir los días")

                    diasSemana.forEach { dia ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    diasElegidos =
                                        if (dia in diasElegidos) {
                                            diasElegidos - dia
                                        } else {
                                            diasElegidos + dia
                                        }
                                },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = dia in diasElegidos,
                                onCheckedChange = { marcado ->
                                    diasElegidos =
                                        if (marcado) {
                                            diasElegidos + dia
                                        } else {
                                            diasElegidos - dia
                                        }
                                }
                            )

                            Text(dia)
                        }
                    }

                    if (errorHorario.isNotEmpty()) {
                        Text(
                            text = errorHorario,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (diasElegidos.isEmpty()) {
                            errorHorario =
                                "Selecciona al menos un día."
                        } else {
                            val diasOrdenados =
                                diasSemana.filter {
                                    it in diasElegidos
                                }

                            onGuardarHorario(
                                horarioEditando,
                                horaElegida,
                                diasOrdenados
                            )

                            mostrarDialogoHorario = false
                        }
                    }
                ) {
                    Text("Guardar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        mostrarDialogoHorario = false
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}