package co.edu.uniquindio.compiladores

import co.edu.uniquindio.compiladores.lexico.AnalizadorLexico
import co.edu.uniquindio.compiladores.sintaxis.AnalizadorSintactico
import co.edu.uniquindio.compiladores.sintaxis.UnidadDeCompilacion
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Los errores sintacticos no deben incluir ruido de alternativas descartadas (retroceso):
 * programa valido -> sin errores; programa invalido -> errores reales, con posicion y sin duplicados.
 */
class SintacticoErroresTest {

    private class Analisis(val unidad: UnidadDeCompilacion?, val sintactico: AnalizadorSintactico) {
        val errores get() = sintactico.listaErrores
    }

    private fun analizar(fuente: String): Analisis {
        val lexico = AnalizadorLexico(fuente)
        lexico.analizar()
        val sintactico = AnalizadorSintactico(lexico.listaTokens)
        return Analisis(sintactico.esUnidadDeCompilacion(), sintactico)
    }

    private fun assertValido(fuente: String) {
        val a = analizar(fuente)
        assertNotNull(a.unidad, "No se construyo la unidad. Errores: ${a.errores}")
        assertTrue(a.errores.isEmpty(), "Un programa valido no debe tener errores: ${a.errores}")
    }

    private fun assertSinDuplicados(a: Analisis) {
        val claves = a.errores.map { Triple(it.error, it.fila, it.columna) }
        assertEquals(claves.distinct(), claves, "Errores duplicados: ${a.errores}")
    }

    private val funcionConRetorno = "pub cls ^calc {\n" +
            "pvt ent ^total |\n" +
            "pub str ^titulo |\n" +
            "pub ent ^sumar ( ent ^a : ent ^b ) {\n" +
            "ent ^res |\n" +
            "^res = ^a |\n" +
            "^res += ^b |\n" +
            "imp ( ^res ) |\n" +
            "ret ^res |\n" +
            "}\n" +
            "pub vd ^main ( ) {\n" +
            "ent ^x = #3 |\n" +
            "ent ^y = #4 |\n" +
            "^sumar ( ^x : ^y ) |\n" +
            "^total = ^sumar ( ^x : ^y ) |\n" +
            "imp ( ^total ) |\n" +
            "}\n" +
            "}"

    private val flujo = "pvt cls ^flujo {\n" +
            "pvt vd ^main ( ) {\n" +
            "ent ^i |\n" +
            "^i = #5 |\n" +
            "^i ++ |\n" +
            "^i ++ |\n" +
            "^i -- |\n" +
            "if ( ( ( #3 ) > ( #2 ) ) and ( ( #1 ) <= ( #1 ) ) ) {\n" +
            "imp ( 'A' ) |\n" +
            "} else {\n" +
            "imp ( 'B' ) |\n" +
            "}\n" +
            "if ( not ( ( #1 ) > ( #2 ) ) ) {\n" +
            "imp ( 'C' ) |\n" +
            "}\n" +
            "if ( ( ( #1 ) >= ( #2 ) ) or ( ( #1 ) != ( #1 ) ) ) {\n" +
            "imp ( 'D' ) |\n" +
            "} else {\n" +
            "imp ( 'E' ) |\n" +
            "}\n" +
            "if ( ( ( #1 ) < ( #2 ) ) xor ( ( #1 ) < ( #2 ) ) ) {\n" +
            "imp ( 'F' ) |\n" +
            "} else {\n" +
            "imp ( 'G' ) |\n" +
            "}\n" +
            "whl ( ( ( #1 ) > ( #2 ) ) ) {\n" +
            "imp ( 'nunca' ) |\n" +
            "^i ++ |\n" +
            "}\n" +
            "imp ( ^i ) |\n" +
            "imp ( ( #2 + ( #3 * ( #4 ) ) ) > ( #13 ) ) |\n" +
            "}\n" +
            "}"

    private fun tipos(conCrt: Boolean): String {
        val crtMut = if (conCrt) "crt ^e |\n^e = °y° |\n" else ""
        val crtInm = if (conCrt) "crt ^j = °x° |\n" else ""
        val crtImp = if (conCrt) "imp ( ^e ) |\nimp ( ^j ) |\n" else ""
        return "pub cls ^tipos {\n" +
                "pub vd ^main ( ) {\n" +
                "ent ^a |\ndbe ^b |\nstr ^c |\nbln ^d |\n" +
                "^a = #7 |\n^b = @2.5 |\n^c = 'ab' |\n^d = ( #1 ) <= ( #1 ) |\n" +
                crtMut +
                "ent ^f = #5 |\ndbe ^g = @1.5 |\nstr ^h = 'hola' |\n" +
                "bln ^i = ( ( #1 ) < ( #2 ) ) and ( ( #2 ) < ( #3 ) ) |\n" +
                crtInm +
                "imp ( ^a ) |\nimp ( ^b ) |\nimp ( ^c ) |\nimp ( ^d ) |\n" +
                "imp ( ^f ) |\nimp ( ^g ) |\nimp ( ^h ) |\nimp ( ^i ) |\n" +
                crtImp +
                "}\n}"
    }

    private val lectura = "pub cls ^lee {\n" +
            "pub vd ^main ( ) {\n" +
            "str ^nombre |\n" +
            "^nombre = rad ( 'Escriba su nombre' ) |\n" +
            "str ^otro = rad ( 'Otro dato' ) |\n" +
            "imp ( ^nombre ) |\n" +
            "}\n" +
            "}"

    private val borde = "pub cls ^borde {\n" +
            "pub ent ^class ( ent ^int ) {\n" +
            "ent ^new = #1 |\n" +
            "if ( ( ( #1 ) <= ( #1 ) ) ) {\n" +
            "ent ^new = #2 |\n" +
            "imp ( ^new ) |\n" +
            "}\n" +
            "ent ^q = #7 |\n" +
            "if ( ( ( #1 ) <= ( #1 ) ) ) {\n" +
            "ent ^q = ^q |\n" +
            "imp ( ^q ) |\n" +
            "}\n" +
            "ret ^int |\n" +
            "}\n" +
            "pub ent ^buscar ( ) {\n" +
            "whl ( ( ( #2 ) > ( #1 ) ) ) {\n" +
            "ret #9 |\n" +
            "}\n" +
            "}\n" +
            "pub vd ^main ( ) {\n" +
            "ent ^v = #4 |\n" +
            "ent ^w |\n" +
            "^w = ^class ( ^v ) |\n" +
            "imp ( ^w ) |\n" +
            "imp ( 'a\\\\b' + #5 + @1.5 + °z° ) |\n" +
            "imp ( °a° + °b° ) |\n" +
            "}\n" +
            "}"

    @Test
    fun programasValidosNoTienenErrores() {
        assertValido("pub cls ^hola {\npub vd ^main ( ) {\nimp ( 'Hola mundo' ) |\n}\n}")
        assertValido(funcionConRetorno)
        assertValido(funcionConRetorno.replace("^total = ^sumar ( ^x : ^y ) |\n", "").replace("imp ( ^total ) |\n", ""))
        assertValido(flujo)
        assertValido(tipos(false))
        assertValido(tipos(true))
        assertValido(lectura)
        assertValido(borde)
    }

    @Test
    fun faltaClsDevuelveNuloConError() {
        val a = analizar("pub ^a { }")
        assertNull(a.unidad)
        assertEquals(1, a.errores.size, "${a.errores}")
        val e = a.errores[0]
        assertEquals("Falta palabra reservada cls", e.error)
        assertEquals(1, e.fila)
        assertEquals(5, e.columna)
    }

    @Test
    fun faltaFinDeSentencia() {
        val a = analizar("pub cls ^a {\npub vd ^main ( ) {\nimp ( 'x' )\n}\n}")
        assertNull(a.unidad, "${a.errores}")
        assertEquals(1, a.errores.size, "${a.errores}")
        assertEquals("Falta el fin de sentencia", a.errores[0].error)
        assertEquals(4, a.errores[0].fila)
        assertEquals(1, a.errores[0].columna)
        assertSinDuplicados(a)
    }

    @Test
    fun parentesisSinCerrarEnIf() {
        val a = analizar("pub cls ^a {\npub vd ^main ( ) {\nif ( ( ( #1 ) <= ( #1 ) ) {\nimp ( 'x' ) |\n}\n}\n}")
        assertNull(a.unidad, "${a.errores}")
        assertEquals(1, a.errores.size, "${a.errores}")
        assertEquals("Debe definir el parentesis derecho de la desicion", a.errores[0].error)
        assertEquals(3, a.errores[0].fila)
        assertSinDuplicados(a)
    }

    @Test
    fun funcionSinTipoDeRetorno() {
        val a = analizar("pub cls ^a {\npub ^main ( ) {\nimp ( 'x' ) |\n}\n}")
        assertNull(a.unidad, "${a.errores}")
        assertEquals(1, a.errores.size, "${a.errores}")
        assertEquals("Debe definir el valor retornado de la funcion", a.errores[0].error)
        assertEquals(2, a.errores[0].fila)
        assertEquals(5, a.errores[0].columna)
        assertSinDuplicados(a)
    }

    @Test
    fun llaveSinCerrar() {
        val a = analizar("pub cls ^a {\npub vd ^main ( ) {\nimp ( 'x' ) |\n}")
        assertNull(a.unidad, "${a.errores}")
        assertEquals(1, a.errores.size, "${a.errores}")
        assertEquals("Falta la llave derecha", a.errores[0].error)
        assertEquals(2, a.errores[0].columna)
        assertEquals(4, a.errores[0].fila)
        assertSinDuplicados(a)
    }

    @Test
    fun codigoDespuesDeLaClase() {
        val a = analizar("pub cls ^a {\n}\nimp ( 'x' ) |")
        assertEquals(1, a.errores.size, "${a.errores}")
        assertEquals("Hay codigo despues de la llave que cierra la clase", a.errores[0].error)
        assertEquals(3, a.errores[0].fila)
        assertEquals(1, a.errores[0].columna)
    }
}
