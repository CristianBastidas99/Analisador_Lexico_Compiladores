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
