package co.edu.uniquindio.compiladores

import co.edu.uniquindio.compiladores.lexico.AnalizadorLexico
import co.edu.uniquindio.compiladores.lexico.Categoria
import co.edu.uniquindio.compiladores.lexico.Token
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

/** Pruebas de los tokens validos que produce el lexico: lexema, categoria, fila y columna (desde 1) */
class LexicoTokensTest {

    private fun analizar(fuente: String): AnalizadorLexico {
        val lexico = AnalizadorLexico(fuente)
        lexico.analizar()
        return lexico
    }

    private fun assertToken(token: Token, lexema: String, categoria: Categoria, fila: Int, columna: Int) {
        assertEquals(lexema, token.lexema, "lexema de $token")
        assertEquals(categoria, token.categoria, "categoria de $token")
        assertEquals(fila, token.fila, "fila de $token")
        assertEquals(columna, token.columna, "columna de $token")
    }

    /** Analiza una fuente que contiene un unico token, sin errores, y lo comprueba en la posicion 1:1 */
    private fun assertUnico(fuente: String, categoria: Categoria, lexema: String = fuente) {
        val lexico = analizar(fuente)
        assertTrue(lexico.listaErrores.isEmpty(), "Errores inesperados: ${lexico.listaErrores}")
        assertEquals(1, lexico.listaTokens.size, "Tokens: ${lexico.listaTokens}")
        assertToken(lexico.listaTokens[0], lexema, categoria, 1, 1)
    }

    @ParameterizedTest
    @ValueSource(strings = ["ent", "dbe", "str", "pub", "pvt", "abs", "ptt", "cls", "bln", "crt", "mut", "inm",
        "whl", "ret", "rad", "tre", "fls", "if", "else", "vd", "imp", "cat", "array"])
    fun palabrasReservadas(palabra: String) {
        assertUnico(palabra, Categoria.PALABRA_RESERVADA)
    }

    @ParameterizedTest
    @ValueSource(strings = ["cth", "brk", "cas"])
    fun palabrasReservadasDelPdfCthBrkCas(palabra: String) {
        assertUnico(palabra, Categoria.PALABRA_RESERVADA)
    }

    @Test
    fun palabraReservadaConPosicion() {
        val lexico = analizar("\n   whl")
        assertEquals(1, lexico.listaTokens.size)
        assertToken(lexico.listaTokens[0], "whl", Categoria.PALABRA_RESERVADA, 2, 4)
    }

    @Test
    fun entero() {
        assertUnico("#7", Categoria.ENTERO)
        assertUnico("#12345", Categoria.ENTERO)
        val lexico = analizar("  #90")
        assertToken(lexico.listaTokens[0], "#90", Categoria.ENTERO, 1, 3)
    }

    @Test
    fun decimal() {
        assertUnico("@2.5", Categoria.DECIMAL)
        assertUnico("@12.50", Categoria.DECIMAL)
        val lexico = analizar("\n\n@0.25")
        assertToken(lexico.listaTokens[0], "@0.25", Categoria.DECIMAL, 3, 1)
    }

    @Test
    fun identificador() {
        assertUnico("^a", Categoria.IDENTIFICADOR)
        assertUnico("^abc12", Categoria.IDENTIFICADOR)
        // Diez caracteres incluyendo '^' es el maximo permitido
        assertUnico("^abcdefghi", Categoria.IDENTIFICADOR)
        val lexico = analizar("   ^x1")
        assertToken(lexico.listaTokens[0], "^x1", Categoria.IDENTIFICADOR, 1, 4)
    }

    @ParameterizedTest
    @ValueSource(strings = ["+", "-", "*", "/", "%"])
    fun operadoresAritmeticos(operador: String) {
        assertUnico(operador, Categoria.OPERADOR_ARITMETICO)
    }

    @ParameterizedTest
    @ValueSource(strings = ["not", "and", "or", "xor"])
    fun operadoresLogicos(operador: String) {
        assertUnico(operador, Categoria.OPERADOR_LOGICO)
    }

    @ParameterizedTest
    @ValueSource(strings = ["=", "+=", "-=", "*=", "/=", "%="])
    fun operadoresDeAsignacion(operador: String) {
        assertUnico(operador, Categoria.OPERADOR_ASIGNACION)
    }

    @Test
    fun operadorIncremento() {
        assertUnico("++", Categoria.OPERADOR_INCREMENTO)
    }

    @Test
    fun operadorDecremento() {
        assertUnico("--", Categoria.OPERADOR_DECREMENTO)
    }

    @ParameterizedTest
    @ValueSource(strings = ["==", "!=", "<", ">", "<=", ">="])
    fun operadoresRelacionales(operador: String) {
        assertUnico(operador, Categoria.OPERADOR_RELACIONAL)
    }

    @Test
    fun agrupadores() {
        assertUnico("(", Categoria.PARENTESIS_IZQUIERDO)
        assertUnico(")", Categoria.PARENTESIS_DERECHO)
        assertUnico("{", Categoria.LLAVE_IZQUIERDO)
        assertUnico("}", Categoria.LLAVE_DERECHO)
        assertUnico("[", Categoria.CORCHETE_IZQUIERDO)
        assertUnico("]", Categoria.CORCHETE_DERECHO)
    }

    @Test
    fun agrupadoresConPosicion() {
        val lexico = analizar("( ) { }\n[ ]")
        assertTrue(lexico.listaErrores.isEmpty(), "${lexico.listaErrores}")
        assertEquals(6, lexico.listaTokens.size)
        assertToken(lexico.listaTokens[0], "(", Categoria.PARENTESIS_IZQUIERDO, 1, 1)
        assertToken(lexico.listaTokens[1], ")", Categoria.PARENTESIS_DERECHO, 1, 3)
        assertToken(lexico.listaTokens[2], "{", Categoria.LLAVE_IZQUIERDO, 1, 5)
        assertToken(lexico.listaTokens[3], "}", Categoria.LLAVE_DERECHO, 1, 7)
        assertToken(lexico.listaTokens[4], "[", Categoria.CORCHETE_IZQUIERDO, 2, 1)
        assertToken(lexico.listaTokens[5], "]", Categoria.CORCHETE_DERECHO, 2, 3)
    }

    @Test
    fun finDeSentencia() {
        assertUnico("|", Categoria.FIN_SENTENCIA)
    }

    @Test
    fun punto() {
        assertUnico("_", Categoria.PUNTO)
    }

    @Test
    fun dosPuntos() {
        assertUnico(";", Categoria.DOS_PUNTOS)
    }

    @Test
    fun separador() {
        assertUnico(":", Categoria.SEPARADOR)
    }

    @Test
    fun comentarioDeLinea() {
        val lexico = analizar("& esto es un comentario\nent")
        assertTrue(lexico.listaErrores.isEmpty(), "${lexico.listaErrores}")
        assertEquals(2, lexico.listaTokens.size, "${lexico.listaTokens}")
        assertToken(lexico.listaTokens[0], "& esto es un comentario\n", Categoria.COMENTARIO_LINEA, 1, 1)
        assertToken(lexico.listaTokens[1], "ent", Categoria.PALABRA_RESERVADA, 2, 1)
    }

    @Test
    fun comentarioDeBloque() {
        val fuente = "\"\"\"un bloque\"\"\" ent"
        val lexico = analizar(fuente)
        assertTrue(lexico.listaErrores.isEmpty(), "${lexico.listaErrores}")
        assertEquals(2, lexico.listaTokens.size, "${lexico.listaTokens}")
        assertToken(lexico.listaTokens[0], "\"\"\"un bloque\"\"\"", Categoria.COMENTARIO_BLOQUE, 1, 1)
        assertToken(lexico.listaTokens[1], "ent", Categoria.PALABRA_RESERVADA, 1, 17)
    }

    @Test
    fun comentarioDeBloqueEnVariasLineas() {
        val lexico = analizar("\"\"\"a\nb\"\"\" ent")
        assertTrue(lexico.listaErrores.isEmpty(), "${lexico.listaErrores}")
        assertEquals(2, lexico.listaTokens.size, "${lexico.listaTokens}")
        assertToken(lexico.listaTokens[0], "\"\"\"a\nb\"\"\"", Categoria.COMENTARIO_BLOQUE, 1, 1)
        assertToken(lexico.listaTokens[1], "ent", Categoria.PALABRA_RESERVADA, 2, 6)
    }

    @Test
    fun cadenaDeCaracteres() {
        assertUnico("'hola mundo'", Categoria.CADENA_CARACTERES)
        assertUnico("''", Categoria.CADENA_CARACTERES)
        // Secuencia de escape valida dentro de la cadena
        assertUnico("'a\\\\b'", Categoria.CADENA_CARACTERES)
        assertUnico("'a\\nb\\tc\\'d\\\"e'", Categoria.CADENA_CARACTERES)
        // Segun el PDF un salto de linea real, un tabulador y comillas dobles son C validos
        assertUnico("'l1\nl2\t\"x\"'", Categoria.CADENA_CARACTERES)
        val lexico = analizar("  'x'")
        assertToken(lexico.listaTokens[0], "'x'", Categoria.CADENA_CARACTERES, 1, 3)
    }

    @Test
    fun caracter() {
        assertUnico("°a°", Categoria.CARACTER)
        assertUnico("°7°", Categoria.CARACTER)
        // Caracter escapado
        assertUnico("°\\\\°", Categoria.CARACTER)
        assertUnico("°\\n°", Categoria.CARACTER)
        assertUnico("°\\t°", Categoria.CARACTER)
        assertUnico("°\\'°", Categoria.CARACTER)
        assertUnico("°\\\"°", Categoria.CARACTER)
        assertUnico("°\"°", Categoria.CARACTER)
        assertUnico("°'°", Categoria.CARACTER)
        val lexico = analizar("\n  °z°")
        assertToken(lexico.listaTokens[0], "°z°", Categoria.CARACTER, 2, 3)
    }

    @Test
    fun secuenciaCompletaEnVariasLineas() {
        val fuente = "pub cls ^a {\n  ent ^x = #5 |\n  & nota\n  imp ( 'hola' ) |\n}"
        val lexico = analizar(fuente)
        assertTrue(lexico.listaErrores.isEmpty(), "${lexico.listaErrores}")
        val esperado = listOf(
            listOf("pub", Categoria.PALABRA_RESERVADA, 1, 1),
            listOf("cls", Categoria.PALABRA_RESERVADA, 1, 5),
            listOf("^a", Categoria.IDENTIFICADOR, 1, 9),
            listOf("{", Categoria.LLAVE_IZQUIERDO, 1, 12),
            listOf("ent", Categoria.PALABRA_RESERVADA, 2, 3),
            listOf("^x", Categoria.IDENTIFICADOR, 2, 7),
            listOf("=", Categoria.OPERADOR_ASIGNACION, 2, 10),
            listOf("#5", Categoria.ENTERO, 2, 12),
            listOf("|", Categoria.FIN_SENTENCIA, 2, 15),
            listOf("& nota\n", Categoria.COMENTARIO_LINEA, 3, 3),
            listOf("imp", Categoria.PALABRA_RESERVADA, 4, 3),
            listOf("(", Categoria.PARENTESIS_IZQUIERDO, 4, 7),
            listOf("'hola'", Categoria.CADENA_CARACTERES, 4, 9),
            listOf(")", Categoria.PARENTESIS_DERECHO, 4, 16),
            listOf("|", Categoria.FIN_SENTENCIA, 4, 18),
            listOf("}", Categoria.LLAVE_DERECHO, 5, 1)
        )
        assertEquals(esperado.size, lexico.listaTokens.size, "Tokens: ${lexico.listaTokens}")
        for ((i, e) in esperado.withIndex()) {
            assertToken(lexico.listaTokens[i], e[0] as String, e[1] as Categoria, e[2] as Int, e[3] as Int)
        }
    }

    @Test
    fun operadoresPegadosSeSeparanCorrectamente() {
        // "^i++|" -> identificador, incremento, fin de sentencia; "a+=b" no es incremento
        val lexico = analizar("^i++| ^j+=#1")
        assertTrue(lexico.listaErrores.isEmpty(), "${lexico.listaErrores}")
        assertEquals(6, lexico.listaTokens.size, "${lexico.listaTokens}")
        assertToken(lexico.listaTokens[0], "^i", Categoria.IDENTIFICADOR, 1, 1)
        assertToken(lexico.listaTokens[1], "++", Categoria.OPERADOR_INCREMENTO, 1, 3)
        assertToken(lexico.listaTokens[2], "|", Categoria.FIN_SENTENCIA, 1, 5)
        assertToken(lexico.listaTokens[3], "^j", Categoria.IDENTIFICADOR, 1, 7)
        assertToken(lexico.listaTokens[4], "+=", Categoria.OPERADOR_ASIGNACION, 1, 9)
        assertToken(lexico.listaTokens[5], "#1", Categoria.ENTERO, 1, 11)
    }
}
