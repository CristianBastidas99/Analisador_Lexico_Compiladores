package co.edu.uniquindio.compiladores

import co.edu.uniquindio.compiladores.lexico.AnalizadorLexico
import co.edu.uniquindio.compiladores.semantico.AnalizadorSemantico
import co.edu.uniquindio.compiladores.sintaxis.AnalizadorSintactico
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.File
import java.nio.charset.StandardCharsets
import java.nio.file.Files

/**
 * Evita que la documentacion se desactualice: el ejemplo completo de docs/traduccion.md (fuente y Java)
 * debe coincidir con lo que genera el compilador.
 */
class DocumentacionTest {

    private fun leer(ruta: String): String =
        String(Files.readAllBytes(File(ruta).toPath()), StandardCharsets.UTF_8).replace("\r\n", "\n")

    /** Contenido del bloque de codigo (``` ... ```) que sigue al marcador HTML dado */
    private fun bloque(documento: String, marcador: String): String {
        val regex = Regex("<!-- $marcador -->\\s*```[^\\n]*\\n(.*?)\\n```", RegexOption.DOT_MATCHES_ALL)
        val coincidencia = regex.find(documento)
        assertNotNull(coincidencia, "No se encontro el bloque marcado con <!-- $marcador --> en docs/traduccion.md")
        return coincidencia!!.groupValues[1]
    }

    @Test
    fun elEjemploDeTraduccionCoincideConElCompilador() {
        val documento = leer("docs/traduccion.md")
        val fuente = bloque(documento, "ejemplo-fuente")
        val javaDocumentado = bloque(documento, "ejemplo-java")

        // El fuente documentado es el archivo de ejemplos
        assertEquals(leer("ejemplos/03-funciones.txt").trimEnd(), fuente.trimEnd(),
            "El fuente de docs/traduccion.md debe ser igual a ejemplos/03-funciones.txt")

        val lexico = AnalizadorLexico(fuente)
        lexico.analizar()
        assertTrue(lexico.listaErrores.isEmpty(), "Errores lexicos: ${lexico.listaErrores}")
        val sintactico = AnalizadorSintactico(lexico.listaTokens)
        val unidad = sintactico.esUnidadDeCompilacion()
        assertTrue(sintactico.listaErrores.isEmpty() && unidad != null, "Errores sintacticos: ${sintactico.listaErrores}")
        val semantico = AnalizadorSemantico(unidad!!)
        semantico.llenarTablaSimbolos()
        semantico.analizarSemantica()
        assertTrue(semantico.listaErroresSemanticos.isEmpty(), "Errores semanticos: ${semantico.listaErroresSemanticos}")

        val javaGenerado = unidad.getCodeJava().replace("\r\n", "\n")
        assertEquals(javaGenerado.trimEnd(), javaDocumentado.trimEnd(),
            "El Java de docs/traduccion.md no coincide con el que genera el compilador")
    }
}
