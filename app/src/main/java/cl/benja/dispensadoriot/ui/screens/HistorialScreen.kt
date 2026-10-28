package cl.benja.dispensadoriot.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cl.benja.dispensadoriot.Azul
import cl.benja.dispensadoriot.data.Operacion

@Composable
fun HistorialScreen(
    operaciones: List<Operacion>
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Historial de operaciones",
            fontSize = 26.sp,
            color = Azul
        )

        Text(
            text = "Actividad reciente del dispensador",
            color = Color.Gray
        )

        if (operaciones.isEmpty()) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                )
            ) {
                Text(
                    text = "Todavía no hay operaciones registradas. Cuando uses el control, aparecerán aquí.",
                    modifier = Modifier.padding(20.dp)
                )
            }
        } else {
            operaciones.forEach { operacion ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Text(
                            text = operacion.accion,
                            fontSize = 17.sp,
                            color = Azul
                        )

                        Text(
                            text = operacion.fecha,
                            color = Color.Gray,
                            fontSize = 13.sp
                        )

                        Text(
                            text = "Origen: ${operacion.dispositivo}"
                        )
                    }
                }
            }
        }
    }
}