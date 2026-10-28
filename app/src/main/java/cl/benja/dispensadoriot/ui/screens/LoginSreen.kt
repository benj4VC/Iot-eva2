
package cl.benja.dispensadoriot.ui.screens

import android.util.Patterns
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import cl.benja.dispensadoriot.Azul
import cl.benja.dispensadoriot.Fondo

@Composable
fun LoginScreen(
    correo: String,
    onCorreoChange: (String) -> Unit,
    contrasena: String,
    onContrasenaChange: (String) -> Unit,
    onLogin: () -> Unit
) {
    var mostrarContrasena by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }
    var cargando by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Fondo)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("🐾", fontSize = 60.sp)

        Text(
            text = "Dispensador IoT",
            fontSize = 29.sp,
            color = Azul
        )

        Text(
            text = "Controla la alimentación de tu mascota",
            color = Color.DarkGray
        )

        Spacer(Modifier.height(28.dp))

        OutlinedTextField(
            value = correo,
            onValueChange = {
                onCorreoChange(it)
                error = ""
            },
            label = { Text("Correo electrónico") },
            singleLine = true,
            enabled = !cargando,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = contrasena,
            onValueChange = {
                onContrasenaChange(it)
                error = ""
            },
            label = { Text("Contraseña") },
            singleLine = true,
            enabled = !cargando,
            visualTransformation = if (mostrarContrasena) {
                VisualTransformation.None
            } else {
                PasswordVisualTransformation()
            },
            trailingIcon = {
                TextButton(
                    enabled = !cargando,
                    onClick = {
                        mostrarContrasena = !mostrarContrasena
                    }
                ) {
                    Text(if (mostrarContrasena) "Ocultar" else "Ver")
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        if (error.isNotEmpty()) {
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        Spacer(Modifier.height(20.dp))

        // Iniciar sesión
        Button(
            enabled = !cargando,
            onClick = {
                val email = correo.trim()

                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    error = "Introduce un correo electrónico válido."
                    return@Button
                }

                if (contrasena.isBlank()) {
                    error = "Introduce tu contraseña."
                    return@Button
                }

                cargando = true
                error = ""

                FirebaseAuth.getInstance()
                    .signInWithEmailAndPassword(email, contrasena)
                    .addOnCompleteListener { tarea ->
                        cargando = false

                        if (tarea.isSuccessful) {
                            onLogin()
                        } else {
                            error = when (
                                tarea.exception?.message
                            ) {
                                else -> "No se pudo iniciar sesión. Comprueba tus credenciales y la conexión."
                            }
                        }
                    }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Azul
            )
        ) {
            if (cargando) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                Text("Iniciar sesión")
            }
        }

        Spacer(Modifier.height(10.dp))

        // Crear cuenta
        OutlinedButton(
            enabled = !cargando,
            onClick = {
                val email = correo.trim()

                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    error = "Introduce un correo electrónico válido."
                    return@OutlinedButton
                }

                if (contrasena.length < 6) {
                    error = "La contraseña debe tener al menos 6 caracteres."
                    return@OutlinedButton
                }

                cargando = true
                error = ""

                FirebaseAuth.getInstance()
                    .createUserWithEmailAndPassword(email, contrasena)
                    .addOnCompleteListener { tarea ->
                        cargando = false

                        if (tarea.isSuccessful) {
                            onLogin()
                        } else {
                            error = when (
                                tarea.exception?.message
                            ) {
                                else -> "No se pudo crear la cuenta. Comprueba los datos o si el correo ya está registrado."
                            }
                        }
                    }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Text("Crear cuenta")
        }

        Spacer(Modifier.height(16.dp))

        Text(
            text = "Accede con tu cuenta o registra una nueva.",
            fontSize = 12.sp,
            color = Color.Gray
        )
    }
}
