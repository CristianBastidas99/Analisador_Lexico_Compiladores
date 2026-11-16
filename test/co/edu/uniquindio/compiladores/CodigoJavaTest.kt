package co.edu.uniquindio.compiladores

import co.edu.uniquindio.compiladores.lexico.Categoria
import co.edu.uniquindio.compiladores.lexico.Token
import co.edu.uniquindio.compiladores.sintaxis.CodigoJava
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class CodigoJavaTest {

    private fun entero(l: String) = CodigoJava.numero(Token(l, Categoria.ENTERO, 1, 1))
    private fun decimal(l: String) = CodigoJava.numero(Token(l, Categoria.DECIMAL, 1, 1))

    @Test
    fun cadenasConEscapes() {
        assertEquals("\"a\\nb\"", CodigoJava.cadena("'a\\nb'"))
        assertEquals("\"a\\tb\"", CodigoJava.cadena("'a\\tb'"))
        assertEquals("\"it's\"", CodigoJava.cadena("'it\\'s'"))
        assertEquals("\"di\\\"jo\"", CodigoJava.cadena("'di\\\"jo'"))
        assertEquals("\"a\\\\b\"", CodigoJava.cadena("'a\\\\b'"))
        // Salto de linea real, tabulador real y comillas dobles sin escapar
        assertEquals("\"l1\\nl2\"", CodigoJava.cadena("'l1\nl2'"))
        assertEquals("\"a\\tb\"", CodigoJava.cadena("'a\tb'"))
        assertEquals("\"x\\\"y\"", CodigoJava.cadena("'x\"y'"))
    }

    @Test
    fun caracteresConEscapes() {
        assertEquals("'a'", CodigoJava.caracter("°a°"))
        assertEquals("'\\n'", CodigoJava.caracter("°\\n°"))
        assertEquals("'\\t'", CodigoJava.caracter("°\\t°"))
        assertEquals("'\\''", CodigoJava.caracter("°\\'°"))
        assertEquals("'\\\"'", CodigoJava.caracter("°\\\"°"))
        assertEquals("'\\\\'", CodigoJava.caracter("°\\\\°"))
        assertEquals("'n'", CodigoJava.caracter("°n°"))
        assertEquals("'\\n'", CodigoJava.caracter("°\n°"))
    }

    @Test
    fun enteros() {
        assertEquals("7", entero("#007"))
        assertEquals("0", entero("#0"))
        assertEquals("0", entero("#000"))
    }

    @Test
    fun decimales() {
        assertEquals("7.50", decimal("@007.50"))
        assertEquals("0.0", decimal("@."))
        assertEquals("1.0", decimal("@1."))
        assertEquals("0.5", decimal("@.5"))
        assertEquals("1.5", decimal("@1.5"))
    }
}
