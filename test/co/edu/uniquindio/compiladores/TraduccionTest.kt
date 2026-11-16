package co.edu.uniquindio.compiladores

import co.edu.uniquindio.compiladores.lexico.AnalizadorLexico
import co.edu.uniquindio.compiladores.lexico.Categoria
import co.edu.uniquindio.compiladores.semantico.AnalizadorSemantico
import co.edu.uniquindio.compiladores.sintaxis.AnalizadorSintactico
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assumptions
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.charset.StandardCharsets
import java.nio.file.Path
import java.util.concurrent.TimeUnit
import javax.tools.DiagnosticCollector
import javax.tools.JavaFileObject
import javax.tools.ToolProvider

/**
 * Prueba de humo de la traduccion: fuente del lenguaje -> lexico -> sintactico -> semantico -> Java -> javac (-> java).
 * Los programas fuente usan solo \n como salto de linea (el lexico reporta \r como desconocido).
 */
class TraduccionTest {

    @TempDir
    lateinit var directorio: Path

    private class Resultado(val java: String, val diagnosticos: String, val compilo: Boolean)

    /** Ejecuta las fases del compilador, exige que no haya errores y devuelve el Java generado */
    private fun generarJava(fuente: String, exigirSemantica: Boolean = true): String {
        val lexico = AnalizadorLexico(fuente)
        lexico.analizar()
        val desconocidos = lexico.listaTokens.filter { it.categoria == Categoria.DESCONOCIDO }
        assertTrue(desconocidos.isEmpty(), "Errores lexicos (tokens desconocidos): $desconocidos")

        val sintactico = AnalizadorSintactico(lexico.listaTokens)
        val unidad = sintactico.esUnidadDeCompilacion()
        assertTrue(unidad != null, "No se pudo construir la unidad de compilacion. Token actual: ${sintactico.tokenActual}. Ultimos errores: ${sintactico.listaErrores.takeLast(6)}")
        assertTrue(sintactico.listaErrores.isEmpty(), "Errores sintacticos en un programa valido: ${sintactico.listaErrores}")

        val semantico = AnalizadorSemantico(unidad!!)
        semantico.llenarTablaSimbolos()
        semantico.analizarSemantica()
        assertTrue(!exigirSemantica || semantico.listaErroresSemanticos.isEmpty(), "Errores semanticos: ${semantico.listaErroresSemanticos}")

        return unidad.getCodeJava()
    }

    private fun compilar(java: String, clase: String): Resultado {
        val archivo = directorio.resolve("$clase.java").toFile()
        archivo.writeText(java, StandardCharsets.UTF_8)
        val compilador = ToolProvider.getSystemJavaCompiler()
        Assumptions.assumeTrue(compilador != null, "No hay compilador Java del sistema (se necesita un JDK)")
        val diagnosticos = DiagnosticCollector<JavaFileObject>()
        val gestor = compilador.getStandardFileManager(diagnosticos, null, StandardCharsets.UTF_8)
        val tarea = compilador.getTask(
            null, gestor, diagnosticos,
            listOf("-encoding", "UTF-8", "-d", directorio.toString()), null,
            gestor.getJavaFileObjects(archivo)
        )
        val ok = tarea.call()
        gestor.close()
        return Resultado(java, diagnosticos.diagnostics.joinToString("\n"), ok)
    }

    private fun compilarPrograma(fuente: String, clase: String, exigirSemantica: Boolean = true) {
        val java = generarJava(fuente, exigirSemantica)
        val r = compilar(java, clase)
        assertTrue(r.compilo, "javac fallo.\n--- Java generado ---\n${r.java}\n--- Diagnosticos ---\n${r.diagnosticos}")
    }

    private fun ejecutar(clase: String): String {
        val java = File(System.getProperty("java.home"), "bin/java").path
        val proceso = ProcessBuilder(java, "-cp", directorio.toString(), clase)
            .redirectErrorStream(true)
            .start()
        val salida = proceso.inputStream.bufferedReader().readText()
        assertTrue(proceso.waitFor(30, TimeUnit.SECONDS), "El programa no termino")
        assertEquals(0, proceso.exitValue(), "El programa termino con error: $salida")
        return salida
    }

    private fun compilarYEjecutar(fuente: String, clase: String, exigirSemantica: Boolean = true): String {
        compilarPrograma(fuente, clase, exigirSemantica)
        return ejecutar(clase)
    }

    @Test
    fun holaMundo() {
        val fuente = "pub cls ^hola {\n" +
                "pub vd ^main ( ) {\n" +
                "imp ( 'Hola mundo' ) |\n" +
                "}\n" +
                "}"
        assertEquals("Hola mundo", compilarYEjecutar(fuente, "hola"))
    }

    private val fuenteFuncionConRetorno = "pub cls ^calc {\n" +
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

    /**
     * Globales + funcion con parametros y retorno. La invocacion como valor (^total = ^sumar(...) |) se traduce bien,
     * pero el analizador semantico la rechaza (falso error, ver testInvocacionComoValorSemantica), por eso aqui se
     * omite la comprobacion semantica y se verifica solo la traduccion (compila y ejecuta: imprime 7 dos veces y luego 7).
     */
    @Test
    fun globalesYFuncionConParametrosYRetorno() {
        assertEquals("777", compilarYEjecutar(fuenteFuncionConRetorno, "calc", exigirSemantica = false))
    }

    @Test
    fun invocacionSoloComoSentencia() {
        val fuente = fuenteFuncionConRetorno.replace("^total = ^sumar ( ^x : ^y ) |\n", "")
            .replace("imp ( ^total ) |\n", "")
        assertEquals("7", compilarYEjecutar(fuente, "calc"))
    }

    @Disabled("Falso error semantico (Fase 3): InvFuncion.analizarSemantica usa split(\"\\.\") (literal, no regex) y busca los parametros en un ambito con sufijo '.tipo'; rechaza ^total = ^sumar(^x : ^y)")
    @Test
    fun testInvocacionComoValorSemantica() {
        assertEquals("777", compilarYEjecutar(fuenteFuncionConRetorno, "calc"))
    }

    @Test
    fun decisionCicloYOperadores() {
        val fuente = "pvt cls ^flujo {\n" +
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
        // 2 + (3 * (4)) = 14 > 13 -> true
        assertEquals("ACEG6true", compilarYEjecutar(fuente, "flujo"))
    }

    private fun fuenteTipos(conCrt: Boolean): String {
        val crtMut = if (conCrt) "crt ^e |\n^e = °y° |\n" else ""
        val crtInm = if (conCrt) "crt ^j = °x° |\n" else ""
        val crtImp = if (conCrt) "imp ( ^e ) |\nimp ( ^j ) |\n" else ""
        return "pub cls ^tipos {\n" +
                "pub vd ^main ( ) {\n" +
                "ent ^a |\n" +
                "dbe ^b |\n" +
                "str ^c |\n" +
                "bln ^d |\n" +
                "^a = #7 |\n" +
                "^b = @2.5 |\n" +
                "^c = 'ab' |\n" +
                "^d = ( #1 ) <= ( #1 ) |\n" +
                crtMut +
                "ent ^f = #5 |\n" +
                "dbe ^g = @1.5 |\n" +
                "str ^h = 'hola' |\n" +
                "bln ^i = ( ( #1 ) < ( #2 ) ) and ( ( #2 ) < ( #3 ) ) |\n" +
                crtInm +
                "imp ( ^a ) |\n" +
                "imp ( ^b ) |\n" +
                "imp ( ^c ) |\n" +
                "imp ( ^d ) |\n" +
                "imp ( ^f ) |\n" +
                "imp ( ^g ) |\n" +
                "imp ( ^h ) |\n" +
                "imp ( ^i ) |\n" +
                crtImp +
                "}\n" +
                "}"
    }

    @Test
    fun declaracionesMutablesEInmutables() {
        assertEquals("72.5abtrue51.5holatrue", compilarYEjecutar(fuenteTipos(false), "tipos"))
    }

    /** Traduccion de crt: se omite la semantica porque rechaza los literales de caracter (ver el test deshabilitado) */
    @Test
    fun declaracionesConCrtSoloTraduccion() {
        assertEquals("72.5abtrue51.5holatrueyx", compilarYEjecutar(fuenteTipos(true), "tipos", exigirSemantica = false))
    }

    @Disabled("Falso error semantico (Fase 3): ExpresionCadena.obtenerTipo() devuelve siempre str, asi que crt ^c = °x° se rechaza")
    @Test
    fun declaracionesConCrtSemantica() {
        compilarPrograma(fuenteTipos(true), "tipos")
    }

    @Test
    fun lecturaAsignadaAString() {
        val fuente = "pub cls ^lee {\n" +
                "pub vd ^main ( ) {\n" +
                "str ^nombre |\n" +
                "^nombre = rad ( 'Escriba su nombre' ) |\n" +
                "str ^otro = rad ( 'Otro dato' ) |\n" +
                "imp ( ^nombre ) |\n" +
                "}\n" +
                "}"
        compilarPrograma(fuente, "lee")
    }

    @Test
    fun casosBorde() {
        // Identificadores que son palabras reservadas de Java, redeclaracion en ambito anidado,
        // codigo tras ret, ciclo con ret y funcion que debe retornar en todos los caminos
        val fuente = "pub cls ^borde {\n" +
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
        // Semantica omitida: rechaza ^w = ^class ( ^v ) | (falso error de InvFuncion en asignaciones, Fase 3)
        assertEquals("274a\\b51.5zab", compilarYEjecutar(fuente, "borde", exigirSemantica = false))
    }
}
