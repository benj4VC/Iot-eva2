
package cl.benja.dispensadoriot

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cl.benja.dispensadoriot.data.Horario
import cl.benja.dispensadoriot.data.Operacion
import cl.benja.dispensadoriot.ui.screens.ControlScreen
import cl.benja.dispensadoriot.ui.screens.HistorialScreen
import cl.benja.dispensadoriot.ui.screens.InicioScreen
import cl.benja.dispensadoriot.ui.screens.LoginScreen
import cl.benja.dispensadoriot.ui.screens.SeleccionarDispensadorScreen
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private enum class Pantalla {
    LOGIN,
    SELECCION_DISPENSADOR,
    INICIO,
    CONTROL,
    HISTORIAL
}

private fun fechaActual(): String {
    return SimpleDateFormat(
        "dd/MM/yyyy HH:mm",
        Locale.getDefault()
    ).format(Date())
}

private fun horarioAMap(horario: Horario): Map<String, Any> {
    return mapOf(
        "id" to horario.id,
        "hora" to horario.hora,
        "dias" to horario.dias,
        "activo" to horario.activo
    )
}

private fun operacionAMap(operacion: Operacion): Map<String, Any> {
    return mapOf(
        "accion" to operacion.accion,
        "dispositivo" to operacion.dispositivo,
        "fecha" to operacion.fecha
    )
}

@Composable
fun DispensadorApp() {

    var pantalla by remember {
        mutableStateOf(
            if (FirebaseAuth.getInstance().currentUser != null) {
                Pantalla.SELECCION_DISPENSADOR
            } else {
                Pantalla.LOGIN
            }
        )
    }

    var correo by remember { mutableStateOf("") }
    var contrasena by remember { mutableStateOf("") }

    // Identificador del dispensador seleccionado.
    var dispensadorSeleccionado by remember {
        mutableStateOf<String?>(null)
    }

    // Nombre utilizado para registrar desde qué dispositivo
    // se realizó una operación.
    var dispositivo by remember {
        mutableStateOf("Celular 1")
    }

    // Nivel simulado del dispensador seleccionado.
    var nivelComida by remember {
        mutableIntStateOf(75)
    }

    var mensaje by remember {
        mutableStateOf("Sin operaciones todavía")
    }

    val operaciones = remember {
        mutableStateListOf<Operacion>()
    }

    val horarios = remember {
        mutableStateListOf<Horario>()
    }

    val uid = FirebaseAuth.getInstance().currentUser?.uid

    val baseDatos = remember {
        FirebaseDatabase.getInstance(
            "https://dispensadoriot-6f9bf-default-rtdb.firebaseio.com"
        ).reference
    }

    // Escucha los datos del dispensador seleccionado.
    // Los horarios, las operaciones y el nivel quedan separados
    // por identificador de dispensador, no por cuenta de usuario.
    DisposableEffect(uid, pantalla, dispensadorSeleccionado) {

        val idDispensador = dispensadorSeleccionado

        if (
            uid == null ||
            pantalla == Pantalla.LOGIN ||
            pantalla == Pantalla.SELECCION_DISPENSADOR ||
            idDispensador == null
        ) {
            horarios.clear()
            operaciones.clear()
            mensaje = "Sin operaciones todavía"
            nivelComida = 75

            onDispose { }
        } else {

            val referenciaDispensador = baseDatos
                .child("dispensadores")
                .child(idDispensador)

            val referenciaHorarios = referenciaDispensador
                .child("horarios")

            val referenciaOperaciones = referenciaDispensador
                .child("operaciones")

            val referenciaNivel = referenciaDispensador
                .child("nivel")

            val listenerHorarios = object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {
                    horarios.clear()

                    snapshot.children.forEach { item ->

                        val id = item.child("id")
                            .getValue(Long::class.java)
                            ?: item.key?.toLongOrNull()

                        val hora = item.child("hora")
                            .getValue(String::class.java)

                        val activo = item.child("activo")
                            .getValue(Boolean::class.java) ?: true

                        val dias = item.child("dias")
                            .children
                            .mapNotNull {
                                it.getValue(String::class.java)
                            }

                        if (id != null && hora != null) {
                            horarios.add(
                                Horario(
                                    id = id,
                                    hora = hora,
                                    dias = dias,
                                    activo = activo
                                )
                            )
                        }
                    }

                    horarios.sortBy { it.hora }
                }

                override fun onCancelled(error: DatabaseError) {
                    mensaje =
                        "Error al cargar horarios: ${error.message}"
                }
            }

            val listenerOperaciones = object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {
                    operaciones.clear()

                    snapshot.children
                        .toList()
                        .asReversed()
                        .forEach { item ->

                            val accion = item.child("accion")
                                .getValue(String::class.java)

                            val nombreDispositivo =
                                item.child("dispositivo")
                                    .getValue(String::class.java)

                            val fecha = item.child("fecha")
                                .getValue(String::class.java)

                            if (
                                accion != null &&
                                nombreDispositivo != null &&
                                fecha != null
                            ) {
                                operaciones.add(
                                    Operacion(
                                        accion = accion,
                                        dispositivo = nombreDispositivo,
                                        fecha = fecha
                                    )
                                )
                            }
                        }

                    mensaje = if (operaciones.isNotEmpty()) {
                        "${operaciones.first().accion} · " +
                                operaciones.first().fecha
                    } else {
                        "Sin operaciones todavía"
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    mensaje =
                        "Error al cargar historial: ${error.message}"
                }
            }

            val listenerNivel = object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {
                    nivelComida = snapshot.getValue(Int::class.java) ?: 75
                }

                override fun onCancelled(error: DatabaseError) {
                    mensaje =
                        "Error al cargar nivel: ${error.message}"
                }
            }

            referenciaHorarios.addValueEventListener(listenerHorarios)
            referenciaOperaciones.addValueEventListener(
                listenerOperaciones
            )
            referenciaNivel.addValueEventListener(listenerNivel)

            onDispose {
                referenciaHorarios.removeEventListener(listenerHorarios)
                referenciaOperaciones.removeEventListener(
                    listenerOperaciones
                )
                referenciaNivel.removeEventListener(listenerNivel)
            }
        }
    }

    fun registrarOperacion(accion: String) {
        val idDispensador = dispensadorSeleccionado ?: return

        val operacion = Operacion(
            accion = accion,
            dispositivo = dispositivo,
            fecha = fechaActual()
        )

        mensaje = "${operacion.accion} · ${operacion.fecha}"

        if (uid != null) {
            baseDatos
                .child("dispensadores")
                .child(idDispensador)
                .child("operaciones")
                .push()
                .setValue(operacionAMap(operacion))
                .addOnFailureListener { error ->
                    mensaje =
                        "No se pudo guardar el historial: ${error.message}"
                }
        }
    }

    Scaffold(
        containerColor = Fondo,
        bottomBar = {
            if (
                pantalla != Pantalla.LOGIN
            ) {
                NavigationBar(
                    containerColor = Color.White
                ) {

                    NavigationBarItem(
                        selected = pantalla ==
                                Pantalla.SELECCION_DISPENSADOR,
                        onClick = {
                            pantalla = Pantalla.SELECCION_DISPENSADOR
                        },
                        icon = {
                            Text("▦", fontSize = 22.sp)
                        },
                        label = {
                            Text("Dispensadores")
                        }
                    )

                    NavigationBarItem(
                        selected = pantalla == Pantalla.INICIO,
                        onClick = {
                            if (dispensadorSeleccionado != null) {
                                pantalla = Pantalla.INICIO
                            } else {
                                pantalla =
                                    Pantalla.SELECCION_DISPENSADOR
                            }
                        },
                        icon = {
                            Text("⌂", fontSize = 22.sp)
                        },
                        label = {
                            Text("Inicio")
                        }
                    )

                    NavigationBarItem(
                        selected = pantalla == Pantalla.CONTROL,
                        onClick = {
                            if (dispensadorSeleccionado != null) {
                                pantalla = Pantalla.CONTROL
                            } else {
                                pantalla =
                                    Pantalla.SELECCION_DISPENSADOR
                            }
                        },
                        icon = {
                            Text("◉", fontSize = 22.sp)
                        },
                        label = {
                            Text("Control")
                        }
                    )

                    NavigationBarItem(
                        selected = pantalla == Pantalla.HISTORIAL,
                        onClick = {
                            if (dispensadorSeleccionado != null) {
                                pantalla = Pantalla.HISTORIAL
                            } else {
                                pantalla =
                                    Pantalla.SELECCION_DISPENSADOR
                            }
                        },
                        icon = {
                            Text("▤", fontSize = 22.sp)
                        },
                        label = {
                            Text("Historial")
                        }
                    )
                }
            }
        }
    ) { padding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {

            when (pantalla) {

                Pantalla.LOGIN -> LoginScreen(
                    correo = correo,
                    onCorreoChange = { correo = it },
                    contrasena = contrasena,
                    onContrasenaChange = { contrasena = it },
                    onLogin = {
                        pantalla =
                            Pantalla.SELECCION_DISPENSADOR
                    }
                )

                Pantalla.SELECCION_DISPENSADOR ->
                    SeleccionarDispensadorScreen(
                        onSeleccionar = { id ->
                            dispensadorSeleccionado = id
                            mensaje = "Cargando dispensador..."
                            pantalla = Pantalla.INICIO
                        }
                    )

                Pantalla.INICIO -> InicioScreen(
                    nivelComida = nivelComida,
                    mensaje = mensaje,
                    onControl = {
                        pantalla = Pantalla.CONTROL
                    },
                    onLogout = {
                        FirebaseAuth.getInstance().signOut()

                        correo = ""
                        contrasena = ""

                        dispensadorSeleccionado = null

                        horarios.clear()
                        operaciones.clear()

                        nivelComida = 75
                        mensaje = "Sin operaciones todavía"

                        pantalla = Pantalla.LOGIN
                    }
                )

                Pantalla.CONTROL -> ControlScreen(
                    dispositivo = dispositivo,

                    onCambiarDispositivo = {
                        dispositivo =
                            if (dispositivo == "Celular 1") {
                                "Celular 2"
                            } else {
                                "Celular 1"
                            }
                    },

                    nivelComida = nivelComida,
                    horarios = horarios,
                    mensaje = mensaje,

                    onDispensar = {
                        val idDispensador =
                            dispensadorSeleccionado

                        if (idDispensador != null) {
                            val nuevoNivel =
                                (nivelComida - 10).coerceAtLeast(0)

                            baseDatos
                                .child("dispensadores")
                                .child(idDispensador)
                                .child("nivel")
                                .setValue(nuevoNivel)
                                .addOnFailureListener { error ->
                                    mensaje =
                                        "Error al actualizar nivel: " +
                                                error.message
                                }

                            registrarOperacion(
                                "Dispensación simulada"
                            )
                        }
                    },

                    onGuardarHorario = { id, hora, dias ->

                        val idDispensador =
                            dispensadorSeleccionado

                        if (uid != null && idDispensador != null) {

                            val horarioExistente =
                                horarios.firstOrNull {
                                    it.id == id
                                }

                            val horario = Horario(
                                id = id ?: System.currentTimeMillis(),
                                hora = hora,
                                dias = dias,
                                activo = horarioExistente?.activo ?: true
                            )

                            baseDatos
                                .child("dispensadores")
                                .child(idDispensador)
                                .child("horarios")
                                .child(horario.id.toString())
                                .setValue(horarioAMap(horario))
                                .addOnFailureListener { error ->
                                    mensaje =
                                        "Error al guardar horario: " +
                                                error.message
                                }

                            registrarOperacion(
                                if (id == null) {
                                    "Horario guardado: $hora"
                                } else {
                                    "Horario editado: $hora"
                                }
                            )
                        }
                    },

                    onAlternarHorario = { id ->

                        val idDispensador =
                            dispensadorSeleccionado

                        val horario =
                            horarios.firstOrNull { it.id == id }

                        if (
                            horario != null &&
                            uid != null &&
                            idDispensador != null
                        ) {
                            val actualizado = horario.copy(
                                activo = !horario.activo
                            )

                            baseDatos
                                .child("dispensadores")
                                .child(idDispensador)
                                .child("horarios")
                                .child(id.toString())
                                .setValue(horarioAMap(actualizado))
                                .addOnFailureListener { error ->
                                    mensaje =
                                        "Error al cambiar horario: " +
                                                error.message
                                }

                            registrarOperacion(
                                if (actualizado.activo) {
                                    "Horario activado"
                                } else {
                                    "Horario desactivado"
                                }
                            )
                        }
                    },

                    onEliminarHorario = { id ->

                        val idDispensador =
                            dispensadorSeleccionado

                        val horario =
                            horarios.firstOrNull { it.id == id }

                        if (
                            horario != null &&
                            uid != null &&
                            idDispensador != null
                        ) {
                            baseDatos
                                .child("dispensadores")
                                .child(idDispensador)
                                .child("horarios")
                                .child(id.toString())
                                .removeValue()
                                .addOnFailureListener { error ->
                                    mensaje =
                                        "Error al eliminar horario: " +
                                                error.message
                                }

                            registrarOperacion(
                                "Horario eliminado: ${horario.hora}"
                            )
                        }
                    }
                )

                Pantalla.HISTORIAL -> HistorialScreen(
                    operaciones = operaciones
                )
            }
        }
    }
}
