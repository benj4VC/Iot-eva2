package cl.benja.dispensadoriot.data

data class Horario(
    val id: Long,
    val hora: String,
    val dias: List<String>,
    val activo: Boolean = true
)