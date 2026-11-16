package co.edu.uniquindio.compiladores

import co.edu.uniquindio.compiladores.lexico.AnalizadorLexico
import co.edu.uniquindio.compiladores.lexico.Categoria
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTimeoutPreemptively
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.Duration

/** Pruebas del reporte de errores lexicos (fila y columna base 0, igual que los tokens) */
class LexicoErroresTest {

    private fun analizarConLimite(fuente: String): AnalizadorLexico =
        assertTimeoutPreemptively<AnalizadorLexico>(Duration.ofSeconds(5)) { analizar(fuente) }

    private fun analizar(fuente: String): AnalizadorLexico {
        val lexico = AnalizadorLexico(fuente)
        lexico.analizar()
        return lexico
    }

    @Test
    fun caracterNoReconocido() {
        val lexico = analizar("pub cls ^a { $ }")
        assertEquals(1, lexico.listaErrores.size, lexico.listaErrores.toString())
        val error = lexico.listaErrores[0]
        assertEquals(0, error.fila)
        assertEquals(13, error.columna)
        assertTrue(error.error.contains("'$'"), error.error)
    }

    @Test
    fun caracterNoReconocidoEnOtraLinea() {
        val lexico = analizar("pub cls ^a {\n  $\n}")
        assertEquals(1, lexico.listaErrores.size)
        assertEquals(1, lexico.listaErrores[0].fila)
        assertEquals(2, lexico.listaErrores[0].columna)
    }

    @Test
    fun cadenaSinCerrar() {
        val lexico = analizar("pub cls ^a {\n  imp ( 'hola ) |\n}")
        assertEquals(1, lexico.listaErrores.size, lexico.listaErrores.toString())
        val error = lexico.listaErrores[0]
        assertEquals(1, error.fila)
        assertEquals(8, error.columna)
        assertTrue(error.error.contains("Cadena sin cerrar"), error.error)
    }

    @Test
    fun comentarioDeBloqueSinCerrar() {
        val lexico = analizar("pub cls ^a {\n\"\"\" comentario\nsin cierre }")
        assertEquals(1, lexico.listaErrores.size, lexico.listaErrores.toString())
        val error = lexico.listaErrores[0]
        assertEquals(1, error.fila)
        assertEquals(0, error.columna)
        assertTrue(error.error.contains("Comentario de bloque sin cerrar"), error.error)
    }

    /** Verifica que haya exactamente un error con la fila, columna y fragmento de mensaje dados */
    private fun verificarUnError(fuente: String, fila: Int, columna: Int, mensaje: String) {
        val lexico = analizarConLimite(fuente)
        assertEquals(1, lexico.listaErrores.size, "$fuente -> ${lexico.listaErrores}")
        val error = lexico.listaErrores[0]
        assertEquals(fila, error.fila, "fila de $fuente")
        assertEquals(columna, error.columna, "columna de $fuente")
        assertTrue(error.error.contains(mensaje), "mensaje de $fuente: ${error.error}")
    }

    @Test
    fun otrosMalFormados() {
        verificarUnError("ent ^a = # |", 0, 9, "Entero mal formado")
        verificarUnError("dbe ^a = @1. |", 0, 9, "Decimal mal formado")
        verificarUnError("ent ^ |", 0, 4, "Identificador mal formado")
        verificarUnError("ent ^abcdefghijkl |", 0, 4, "Identificador demasiado largo")
        verificarUnError("ent ^a = ° |", 0, 9, "Carácter sin cerrar o mal formado")
    }

    @Test
    fun malFormadosComoUltimosCaracteres() {
        verificarUnError("ent ^a = #", 0, 9, "Entero mal formado")
        verificarUnError("dbe ^a = @1.", 0, 9, "Decimal mal formado")
        verificarUnError("ent ^", 0, 4, "Identificador mal formado")
        verificarUnError("ent ^a = °", 0, 9, "Carácter sin cerrar")
        verificarUnError("imp ( 'abc", 0, 6, "Cadena sin cerrar")
        verificarUnError("pub \"\"\"", 0, 4, "Comentario de bloque sin cerrar")
    }

    @Test
    fun comentarioDeLineaAlFinalSinSaltoEsValido() {
        val lexico = analizarConLimite("pub cls ^a { }\n& comentario final")
        assertTrue(lexico.listaErrores.isEmpty(), lexico.listaErrores.toString())
        assertEquals(Categoria.COMENTARIO_LINEA, lexico.listaTokens.last().categoria)
    }

    @Test
    fun limiteDeLongitudDelIdentificador() {
        val valido = analizar("ent ^abcdefghi |")
        assertTrue(valido.listaErrores.isEmpty(), valido.listaErrores.toString())
        verificarUnError("ent ^abcdefghij |", 0, 4, "Identificador demasiado largo")
    }

    @Test
    fun errorTrasComentarioDeBloqueMultilinea() {
        verificarUnError("\"\"\"a\nb\"\"\" $", 1, 5, "'$'")
    }

    @Test
    fun programaValidoSinErrores() {
        val fuente = "pub cls ^hola {\n" +
                "pub vd ^main ( ) {\n" +
                "imp ( 'Hola mundo' ) |\n" +
                "}\n" +
                "}"
        val lexico = analizar(fuente)
        assertTrue(lexico.listaErrores.isEmpty(), lexico.listaErrores.toString())
    }
}
