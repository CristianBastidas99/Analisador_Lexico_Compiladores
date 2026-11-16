package co.edu.uniquindio.compiladores

import co.edu.uniquindio.compiladores.lexico.AnalizadorLexico
import co.edu.uniquindio.compiladores.lexico.Categoria
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTimeoutPreemptively
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.Duration

/** Pruebas del reporte de errores lexicos (fila y columna desde 1, igual que los tokens) */
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
        assertEquals(1, error.fila)
        assertEquals(14, error.columna)
        assertTrue(error.error.contains("'$'"), error.error)
    }

    @Test
    fun caracterNoReconocidoEnOtraLinea() {
        val lexico = analizar("pub cls ^a {\n  $\n}")
        assertEquals(1, lexico.listaErrores.size)
        assertEquals(2, lexico.listaErrores[0].fila)
        assertEquals(3, lexico.listaErrores[0].columna)
    }

    @Test
    fun cadenaSinCerrar() {
        val lexico = analizar("pub cls ^a {\n  imp ( 'hola ) |\n}")
        assertEquals(1, lexico.listaErrores.size, lexico.listaErrores.toString())
        val error = lexico.listaErrores[0]
        assertEquals(2, error.fila)
        assertEquals(9, error.columna)
        assertTrue(error.error.contains("Cadena sin cerrar"), error.error)
    }

    @Test
    fun comentarioDeBloqueSinCerrar() {
        val lexico = analizar("pub cls ^a {\n\"\"\" comentario\nsin cierre }")
        assertEquals(1, lexico.listaErrores.size, lexico.listaErrores.toString())
        val error = lexico.listaErrores[0]
        assertEquals(2, error.fila)
        assertEquals(1, error.columna)
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
        verificarUnError("ent ^a = # |", 1, 10, "Entero mal formado")
        verificarUnError("dbe ^a = @12 |", 1, 10, "Decimal mal formado")
        verificarUnError("ent ^ |", 1, 5, "Identificador mal formado")
        verificarUnError("ent ^abcdefghijkl |", 1, 5, "Identificador demasiado largo")
        verificarUnError("ent ^a = ° |", 1, 10, "Carácter sin cerrar o mal formado")
    }

    @Test
    fun malFormadosComoUltimosCaracteres() {
        verificarUnError("ent ^a = #", 1, 10, "Entero mal formado")
        verificarUnError("dbe ^a = @12", 1, 10, "Decimal mal formado")
        verificarUnError("ent ^", 1, 5, "Identificador mal formado")
        verificarUnError("ent ^a = °", 1, 10, "Carácter sin cerrar")
        verificarUnError("imp ( 'abc", 1, 7, "Cadena sin cerrar")
        verificarUnError("pub \"\"\"", 1, 5, "Comentario de bloque sin cerrar")
    }

    @Test
    fun comentarioDeLineaAlFinalSinSaltoEsError() {
        val lexico = analizarConLimite("pub cls ^a { }\n& comentario final")
        assertEquals(1, lexico.listaErrores.size, lexico.listaErrores.toString())
        assertEquals(2, lexico.listaErrores[0].fila)
        assertEquals(1, lexico.listaErrores[0].columna)
        assertTrue(lexico.listaErrores[0].error.contains("Comentario de línea sin salto de línea final"))
        assertEquals(Categoria.DESCONOCIDO, lexico.listaTokens.last().categoria)
        assertEquals("& comentario final", lexico.listaTokens.last().lexema)
    }

    @Test
    fun comentarioDeLineaConSaltoEsValido() {
        val lexico = analizarConLimite("& comentario\n")
        assertTrue(lexico.listaErrores.isEmpty())
        assertEquals(Categoria.COMENTARIO_LINEA, lexico.listaTokens.single().categoria)
    }

    @Test
    fun finDeLineaWindowsEquivaleASaltoUnix() {
        val unix = "pub cls ^a {\n  ent ^x = #1 |\n}"
        val windows = unix.replace("\n", "\r\n")
        val a = analizar(unix)
        val b = analizar(windows)
        assertTrue(b.listaErrores.isEmpty(), b.listaErrores.toString())
        fun firma(l: AnalizadorLexico) = l.listaTokens.map { listOf(it.lexema, it.categoria, it.fila, it.columna) }
        assertEquals(firma(a), firma(b))
    }

    @Test
    fun cadenaVaciaNoGeneraTokensNiErrores() {
        val lexico = analizar("")
        assertEquals(0, lexico.listaTokens.size)
        assertEquals(0, lexico.listaErrores.size)
    }

    @Test
    fun igualdadEsOperadorRelacionalYAsignacionSigueIgual() {
        val igual = analizar("==")
        assertTrue(igual.listaErrores.isEmpty())
        assertEquals(1, igual.listaTokens.size)
        assertEquals(Categoria.OPERADOR_RELACIONAL, igual.listaTokens[0].categoria)
        assertEquals("==", igual.listaTokens[0].lexema)

        val asig = analizar("=")
        assertEquals(1, asig.listaTokens.size)
        assertEquals(Categoria.OPERADOR_ASIGNACION, asig.listaTokens[0].categoria)

        val mixto = analizar("^a = ^b == ^c <= ^d != ^e")
        assertEquals(
            listOf(Categoria.IDENTIFICADOR, Categoria.OPERADOR_ASIGNACION, Categoria.IDENTIFICADOR, Categoria.OPERADOR_RELACIONAL,
                Categoria.IDENTIFICADOR, Categoria.OPERADOR_RELACIONAL, Categoria.IDENTIFICADOR, Categoria.OPERADOR_RELACIONAL, Categoria.IDENTIFICADOR),
            mixto.listaTokens.map { it.categoria })
    }

    @Test
    fun decimalesValidosSegunElAutomata() {
        val lexico = analizar("@1.5 @1. @.5 @.")
        assertTrue(lexico.listaErrores.isEmpty(), lexico.listaErrores.toString())
        assertEquals(listOf("@1.5", "@1.", "@.5", "@."), lexico.listaTokens.map { it.lexema })
        assertTrue(lexico.listaTokens.all { it.categoria == Categoria.DECIMAL })
    }

    @Test
    fun decimalesSinPuntoSonError() {
        verificarUnError("@12", 1, 1, "Decimal mal formado")
        verificarUnError("@", 1, 1, "Decimal mal formado")
    }

    @Test
    fun primerTokenEmpiezaEnFilaYColumnaUno() {
        val lexico = analizar("pub")
        assertEquals(1, lexico.listaTokens[0].fila)
        assertEquals(1, lexico.listaTokens[0].columna)
    }

    @Test
    fun limiteDeLongitudDelIdentificador() {
        val valido = analizar("ent ^abcdefghi |")
        assertTrue(valido.listaErrores.isEmpty(), valido.listaErrores.toString())
        verificarUnError("ent ^abcdefghij |", 1, 5, "Identificador demasiado largo")
    }

    @Test
    fun errorTrasComentarioDeBloqueMultilinea() {
        verificarUnError("\"\"\"a\nb\"\"\" $", 2, 6, "'$'")
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

    @Test
    fun exclamacionSueltaEsError() {
        val lexico = analizar("^a ! ^b")
        assertEquals(1, lexico.listaErrores.size, lexico.listaErrores.toString())
        assertEquals(1, lexico.listaErrores[0].fila)
        assertEquals(4, lexico.listaErrores[0].columna)
    }

    @Test
    fun menorIgualSeguidoDeIgual() {
        val lexico = analizar("<==")
        assertTrue(lexico.listaErrores.isEmpty())
        assertEquals(listOf("<=", "="), lexico.listaTokens.map { it.lexema })
        assertEquals(listOf(Categoria.OPERADOR_RELACIONAL, Categoria.OPERADOR_ASIGNACION), lexico.listaTokens.map { it.categoria })
    }

    @Test
    fun retornoDeCarroSueltoEsEspacio() {
        val lexico = analizar("^a\r^b")
        assertTrue(lexico.listaErrores.isEmpty())
        assertEquals(listOf("^a", "^b"), lexico.listaTokens.map { it.lexema })
    }

    @Test
    fun escapeInvalidoEnCadena() {
        // \ seguido de una letra que no es n ni t
        val lexico = analizar("'a\\xb'")
        assertEquals(1, lexico.listaErrores.size, lexico.listaErrores.toString())
        assertTrue(lexico.listaErrores[0].error.contains("secuencia de escape no"), lexico.listaErrores[0].error)
        assertEquals(1, lexico.listaErrores[0].columna)
        // \ seguido de un salto de linea real tampoco es valido
        val l2 = analizar("'a\\\nb'")
        assertTrue(l2.listaErrores.any { it.error.contains("secuencia de escape no") }, l2.listaErrores.toString())
    }

    @Test
    fun escapeInvalidoEnCaracter() {
        val lexico = analizar("°\\x°")
        assertTrue(lexico.listaErrores.any { it.error.contains("secuencia de escape no") }, lexico.listaErrores.toString())
        assertEquals(1, lexico.listaErrores[0].fila)
        assertEquals(1, lexico.listaErrores[0].columna)
    }

    private fun assertUnErrorUnToken(fuente: String) {
        val lexico = analizar(fuente)
        assertEquals(1, lexico.listaErrores.size, "<$fuente>: ${lexico.listaErrores}")
        assertEquals(1, lexico.listaTokens.size, "<$fuente>: ${lexico.listaTokens}")
        assertEquals(Categoria.DESCONOCIDO, lexico.listaTokens[0].categoria)
        assertEquals(fuente, lexico.listaTokens[0].lexema)
        assertEquals(1, lexico.listaErrores[0].fila)
        assertEquals(1, lexico.listaErrores[0].columna)
    }

    @Test
    fun cadenaVaciaEsValida() {
        val lexico = analizar("''")
        assertTrue(lexico.listaErrores.isEmpty())
        assertEquals(Categoria.CADENA_CARACTERES, lexico.listaTokens[0].categoria)
    }

    @Test
    fun caracterMalFormadoGeneraUnSoloError() {
        assertUnErrorUnToken("°°")
        assertUnErrorUnToken("°\\°°")
        assertUnErrorUnToken("°ab°")
        assertUnErrorUnToken("°\\x°")
    }

    @Test
    fun cadenaConBarraAlFinalSinCerrar() {
        assertUnErrorUnToken("'\\'")
        assertUnErrorUnToken("'abc\\")
    }

    @Test
    fun escapeAlFinalDelArchivo() {
        assertUnErrorUnToken("°\\")
        assertUnErrorUnToken("°")
    }

    @Test
    fun palabraReservadaSeguidaDeLetraDejaErrorPorLaLetra() {
        // Igual que 'entx': se reconoce la palabra reservada y la letra sobrante es un error
        for (palabra in listOf("cas", "ent")) {
            val lexico = analizar(palabra + "x")
            assertEquals(listOf(palabra, "x"), lexico.listaTokens.map { it.lexema })
            assertEquals(Categoria.PALABRA_RESERVADA, lexico.listaTokens[0].categoria)
            assertEquals(1, lexico.listaErrores.size, lexico.listaErrores.toString())
            assertEquals(4, lexico.listaErrores[0].columna)
        }
    }

    @Test
    fun tabuladorCuentaComoUnaColumna() {
        // Convencion: el tabulador ocupa 1 columna (no se expande a 4 u 8)
        val lexico = analizar("\t^a")
        assertEquals(1, lexico.listaTokens[0].fila)
        assertEquals(2, lexico.listaTokens[0].columna)
    }
}
