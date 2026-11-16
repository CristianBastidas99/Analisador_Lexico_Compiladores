package co.edu.uniquindio.compiladores

import co.edu.uniquindio.compiladores.lexico.AnalizadorLexico
import co.edu.uniquindio.compiladores.lexico.Error
import co.edu.uniquindio.compiladores.semantico.AnalizadorSemantico
import co.edu.uniquindio.compiladores.sintaxis.AnalizadorSintactico
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assumptions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.concurrent.TimeUnit
import javax.tools.DiagnosticCollector
import javax.tools.JavaFileObject
import javax.tools.ToolProvider

/**
 * Comprueba que cada archivo de ejemplos/ produce exactamente lo que dice su nombre.
 * Un archivo nuevo sin expectativa en esta prueba hace fallar la prueba.
 */
class EjemplosTest {

    @TempDir
    lateinit var directorio: Path

    private class Fase(val lexicos: List<Error>, val sintacticos: List<Error>, val semanticos: List<Error>, val java: String?)

    private fun leer(archivo: File): String =
        String(Files.readAllBytes(archivo.toPath()), StandardCharsets.UTF_8).replace("\r\n", "\n")

    private fun analizar(fuente: String): Fase {
        val lexico = AnalizadorLexico(fuente)
        lexico.analizar()
        val sintactico = AnalizadorSintactico(lexico.listaTokens)
        val unidad = sintactico.esUnidadDeCompilacion()
        if (lexico.listaErrores.isNotEmpty() || sintactico.listaErrores.isNotEmpty() || unidad == null)
            return Fase(lexico.listaErrores, sintactico.listaErrores, emptyList(), null)
        val semantico = AnalizadorSemantico(unidad)
        semantico.llenarTablaSimbolos()
        semantico.analizarSemantica()
        val java = if (semantico.listaErroresSemanticos.isEmpty()) unidad.getCodeJava() else null
        return Fase(lexico.listaErrores, sintactico.listaErrores, semantico.listaErroresSemanticos, java)
    }

    private fun nombreClase(java: String): String =
        Regex("class[ ]+([A-Za-z0-9_]+)").find(java)!!.groupValues[1]

    private fun compilar(java: String, clase: String) {
        val archivo = directorio.resolve("$clase.java").toFile()
        archivo.writeText(java, StandardCharsets.UTF_8)
        val compilador = ToolProvider.getSystemJavaCompiler()
        Assumptions.assumeTrue(compilador != null, "No hay compilador Java del sistema (se necesita un JDK)")
        val diagnosticos = DiagnosticCollector<JavaFileObject>()
        val gestor = compilador.getStandardFileManager(diagnosticos, null, StandardCharsets.UTF_8)
        val ok = compilador.getTask(null, gestor, diagnosticos,
            listOf("-encoding", "UTF-8", "-d", directorio.toString()), null, gestor.getJavaFileObjects(archivo)).call()
        gestor.close()
        assertTrue(ok, "javac fallo.\n$java\n${diagnosticos.diagnostics.joinToString("\n")}")
    }

    private fun ejecutar(clase: String): String {
        val exe = if (System.getProperty("os.name").lowercase().contains("win")) ".exe" else ""
        val java = File(System.getProperty("java.home"), "bin/java$exe").path
        val proceso = ProcessBuilder(java, "-Dfile.encoding=UTF-8", "-Dstdout.encoding=UTF-8", "-cp", directorio.toString(), clase)
            .redirectErrorStream(true).start()
        val salida = StringBuilder()
        val lector = Thread {
            try { salida.append(proceso.inputStream.bufferedReader(StandardCharsets.UTF_8).readText()) } catch (_: Exception) { }
        }
        lector.isDaemon = true
        lector.start()
        val termino = proceso.waitFor(30, TimeUnit.SECONDS)
        if (!termino) proceso.destroyForcibly()
        lector.join(5000)
        assertTrue(termino, "El programa no termino en 30 s. Salida parcial: $salida")
        assertEquals(0, proceso.exitValue(), "El programa termino con error: $salida")
        return salida.toString()
    }

    /** Ejemplo correcto: sin errores, compila; si salidaEsperada no es null, se ejecuta y se compara la salida */
    private fun correcto(nombre: String, salidaEsperada: String?) {
        val f = analizar(leer(File("ejemplos", nombre)))
        assertTrue(f.lexicos.isEmpty(), "$nombre: errores lexicos ${f.lexicos}")
        assertTrue(f.sintacticos.isEmpty(), "$nombre: errores sintacticos ${f.sintacticos}")
        assertTrue(f.semanticos.isEmpty(), "$nombre: errores semanticos ${f.semanticos}")
        val java = f.java!!
        val clase = nombreClase(java)
        compilar(java, clase)
        if (salidaEsperada != null) assertEquals(salidaEsperada, ejecutar(clase), "$nombre: salida")
    }

    private fun pares(e: List<Error>) = e.map { Triple(it.error, it.fila, it.columna) }

    private val expectativas: Map<String, () -> Unit> = mapOf(
        "01-hola-mundo.txt" to { correcto("01-hola-mundo.txt", "Hola mundo\nMi primer programa compilado a Java\n") },
        "02-ciclos-y-decisiones.txt" to {
            correcto("02-ciclos-y-decisiones.txt",
                "i = 6\n3 == 3: verdadero\nand: verdadero\nnot: verdadero\nor: falso\nxor: verdadero\n" +
                        "Dentro del ciclo whl\nbuscar retornó 9\n")
        },
        "03-funciones.txt" to { correcto("03-funciones.txt", "Hola, estudiante\nsumar(3, 4) = 7\ndoble = 14\n") },
        "04-lectura.txt" to { correcto("04-lectura.txt", null) },
        "error-lexico.txt" to {
            val f = analizar(leer(File("ejemplos", "error-lexico.txt")))
            assertEquals(listOf(
                Triple("Carácter no reconocido: '$'", 6, 15),
                Triple("Carácter no reconocido: '~'", 8, 3)
            ), pares(f.lexicos))
        },
        "error-sintactico.txt" to {
            val f = analizar(leer(File("ejemplos", "error-sintactico.txt")))
            assertTrue(f.lexicos.isEmpty(), "${f.lexicos}")
            assertEquals(listOf(Triple("Falta el fin de sentencia", 7, 3)), pares(f.sintacticos))
        },
        "error-semantico.txt" to {
            val f = analizar(leer(File("ejemplos", "error-semantico.txt")))
            assertTrue(f.lexicos.isEmpty(), "${f.lexicos}")
            assertTrue(f.sintacticos.isEmpty(), "${f.sintacticos}")
            assertEquals(listOf(
                Triple("El campo con el nombre ^a ya existe dentro del ámbito ^errsem/^main:vd", 15, 7),
                Triple("El tipo de dato de la expresión (str) que desea retornar no es correcto en la función (ent)", 9, 3),
                Triple("La variable (^b) que intenta dar asignación no se ha declarado en el ámbito ^errsem/^main:vd", 17, 3),
                Triple("El tipo de dato de la expresión (str) no coincide con el tipo de la variable (ent)", 20, 3),
                Triple("La variable (^a) es inmutable y no se puede modificar", 22, 3),
                Triple("La variable (^z) que intenta imprimir no se ha declarado", 24, 9)
            ), pares(f.semanticos))
        }
    )

    @Test
    fun cadaEjemploProduceLoQueDiceSuNombre() {
        val carpeta = Paths.get("ejemplos").toFile()
        assertTrue(carpeta.isDirectory, "No existe la carpeta ejemplos/ (se espera ejecutar desde la raiz del proyecto)")
        val archivos = carpeta.listFiles { f -> f.isFile && !f.isHidden && f.name.endsWith(".txt") }!!.map { it.name }.sorted()
        val sinExpectativa = archivos.filter { it !in expectativas }
        assertTrue(sinExpectativa.isEmpty(), "Archivos en ejemplos/ sin expectativa en EjemplosTest: $sinExpectativa")
        val faltantes = expectativas.keys.filter { it !in archivos }
        assertTrue(faltantes.isEmpty(), "Faltan archivos esperados en ejemplos/: $faltantes")
        val fallos = ArrayList<String>()
        for (nombre in archivos) {
            try { expectativas.getValue(nombre)() } catch (e: Throwable) { fallos.add("== $nombre ==\n${e.javaClass.simpleName}: ${e.message}") }
        }
        assertTrue(fallos.isEmpty(), fallos.joinToString("\n\n"))
    }
}
