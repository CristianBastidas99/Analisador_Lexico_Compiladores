package co.edu.uniquindio.compiladores.controlador

import co.edu.uniquindio.compiladores.lexico.AnalizadorLexico
import co.edu.uniquindio.compiladores.lexico.Categoria
import co.edu.uniquindio.compiladores.lexico.Error
import co.edu.uniquindio.compiladores.lexico.Token
import co.edu.uniquindio.compiladores.semantico.AnalizadorSemantico
import co.edu.uniquindio.compiladores.semantico.Simbolo
import co.edu.uniquindio.compiladores.sintaxis.AnalizadorSintactico
import co.edu.uniquindio.compiladores.sintaxis.UnidadDeCompilacion
import javafx.collections.FXCollections
import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.scene.control.Button
import javafx.scene.control.Tab
import javafx.scene.control.TabPane
import javafx.scene.control.TableColumn
import javafx.scene.control.TableView
import javafx.scene.control.TextArea
import javafx.scene.control.TreeView
import javafx.scene.control.cell.PropertyValueFactory
import javafx.application.Platform
import java.io.File
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.TimeUnit
import javax.tools.DiagnosticCollector
import javax.tools.JavaFileObject
import javax.tools.ToolProvider


class InicioControler {

    @FXML lateinit var codigo_fuente : TextArea
    @FXML lateinit var arbolVisual : TreeView<String>
    @FXML lateinit var tabPane : TabPane
    @FXML lateinit var tablaTabLexico : TableView<Token>
    @FXML lateinit var colm_lexemaTabLexico : TableColumn<Token, String>
    @FXML lateinit var colm_catTabLexico : TableColumn<Token, Categoria>
    @FXML lateinit var colm_filaTabLexico : TableColumn<Token, Int>
    @FXML lateinit var colm_colmTabLexico : TableColumn<Token, Int>

    @FXML lateinit var tablaErroresSint : TableView<Error>
    @FXML lateinit var colm_errorErroresSint : TableColumn<Error, String>
    @FXML lateinit var colm_filaErroresSint : TableColumn<Error, Int>
    @FXML lateinit var colm_columnaErroresSint : TableColumn<Error, Int>

    @FXML lateinit var tablaSimbolos : TableView<Simbolo>
    @FXML lateinit var colm_tsNombre : TableColumn<Simbolo, String>
    @FXML lateinit var colm_tsTipo : TableColumn<Simbolo, String>
    @FXML lateinit var colm_tsModificable : TableColumn<Simbolo, Boolean>
    @FXML lateinit var colm_tsAmbito : TableColumn<Simbolo, String>
    @FXML lateinit var colm_tsAcceso : TableColumn<Simbolo, String>
    @FXML lateinit var colm_tsFila : TableColumn<Simbolo, Int>
    @FXML lateinit var colm_tsColumna : TableColumn<Simbolo, Int>
    @FXML lateinit var colm_tsTiposParametros : TableColumn<Simbolo, ArrayList<String>>

    @FXML lateinit var tablaErroresSem : TableView<Error>
    @FXML lateinit var colm_esemError : TableColumn<Error, String>
    @FXML lateinit var colm_esemFila : TableColumn<Error, Int>
    @FXML lateinit var colm_esemColumna : TableColumn<Error, Int>

    @FXML lateinit var codigoJava : TextArea
    @FXML lateinit var salidaPrograma : TextArea
    @FXML lateinit var tabSalida : Tab
    @FXML lateinit var button_traducir : Button
    @Volatile private var directorioTemporal: Path? = null
    @Volatile private var procesoActual: Process? = null
    private val hookRegistrado = java.util.concurrent.atomic.AtomicBoolean(false)
    var isCodigoJava: Boolean = false
    lateinit var ucg: UnidadDeCompilacion


    @FXML
    fun analizarCodigo(e : ActionEvent?){

        if(codigo_fuente.length > 0) {
            var analizador = AnalizadorLexico(codigo_fuente.text)
            analizador.analizar()
            println(analizador.listaTokens)

            val list = FXCollections.observableArrayList<Token>()
            list.addAll(analizador.listaTokens)

            tablaTabLexico.items.clear()

            colm_lexemaTabLexico.setCellValueFactory(PropertyValueFactory<Token, String>("lexema"))
            colm_catTabLexico.setCellValueFactory(PropertyValueFactory<Token, Categoria>("categoria"))
            colm_filaTabLexico.setCellValueFactory(PropertyValueFactory<Token, Int>("fila"))
            colm_colmTabLexico.setCellValueFactory(PropertyValueFactory<Token, Int>("columna"))

            tablaTabLexico.items.addAll(list)


            var analizadorSintactico = AnalizadorSintactico(analizador.listaTokens)
            var uc = analizadorSintactico.esUnidadDeCompilacion()

            if(uc != null) {
                arbolVisual.setRoot(uc!!.getArbolVisual())

                println(uc.toString())

                val listErrorSint = FXCollections.observableArrayList<Error>()
                listErrorSint.addAll(analizadorSintactico.listaErrores)

                tablaErroresSint.items.clear()

                colm_errorErroresSint.setCellValueFactory(PropertyValueFactory<Error, String>("error"))
                colm_filaErroresSint.setCellValueFactory(PropertyValueFactory<Error, Int>("fila"))
                colm_columnaErroresSint.setCellValueFactory(PropertyValueFactory<Error, Int>("columna"))

                tablaErroresSint.items.addAll(listErrorSint)

                val semantica = AnalizadorSemantico(uc)
                semantica.llenarTablaSimbolos()
                semantica.analizarSemantica()
                println(semantica.tablaSimbolos)

                if(semantica.tablaSimbolos.listaSimbolos.isNotEmpty()) {

                    val listTablaSim = FXCollections.observableArrayList<Simbolo>()
                    listTablaSim.addAll(semantica.tablaSimbolos.listaSimbolos)

                    tablaSimbolos.items.clear()

                    colm_tsNombre.setCellValueFactory(PropertyValueFactory<Simbolo, String>("nombre"))
                    colm_tsTipo.setCellValueFactory(PropertyValueFactory<Simbolo, String>("tipo"))
                    colm_tsModificable.setCellValueFactory(PropertyValueFactory<Simbolo, Boolean>("modificable"))
                    colm_tsAmbito.setCellValueFactory(PropertyValueFactory<Simbolo, String>("ambito"))
                    colm_tsAcceso.setCellValueFactory(PropertyValueFactory<Simbolo, String>("acceso"))
                    colm_tsFila.setCellValueFactory(PropertyValueFactory<Simbolo, Int>("fila"))
                    colm_tsColumna.setCellValueFactory(PropertyValueFactory<Simbolo, Int>("columna"))
                    colm_tsTiposParametros.setCellValueFactory(PropertyValueFactory<Simbolo, ArrayList<String>>("tiposParametros"))

                    tablaSimbolos.items.addAll(listTablaSim)

                }else{
                    tablaErroresSem.items.clear()
                }

                if(semantica.tablaSimbolos.listaErrores.isNotEmpty()) {

                    val listErrorSema = FXCollections.observableArrayList<Error>()
                    listErrorSema.addAll(semantica.tablaSimbolos.listaErrores)

                    tablaErroresSem.items.clear()

                    colm_esemError.setCellValueFactory(PropertyValueFactory<Error, String>("error"))
                    colm_esemFila.setCellValueFactory(PropertyValueFactory<Error, Int>("fila"))
                    colm_esemColumna.setCellValueFactory(PropertyValueFactory<Error, Int>("columna"))

                    tablaErroresSem.items.addAll(listErrorSema)
                }else{
                    tablaErroresSem.items.clear()
                    isCodigoJava = true
                    ucg = uc
                }

            }
        }
    }

    @FXML
    fun traducirCodigo(e : ActionEvent?){
        if(!isCodigoJava){
            val msg = "Hay errores semanticos"
            codigoJava.setText(msg)
            salidaPrograma.setText(msg)
            tabPane.selectionModel.select(tabSalida)
            return
        }

        val codigoFuente = ucg.getCodeJava()
        val nombreClase = ucg.nombre.lexema.substring(1)
        codigoJava.setText(codigoFuente)
        salidaPrograma.clear()
        tabPane.selectionModel.select(tabSalida)
        button_traducir.isDisable = true

        val hilo = Thread {
            try {
                compilarYEjecutar(codigoFuente, nombreClase)
            } catch (ex: Exception) {
                escribirSalida("Error inesperado: ${ex.message}\n")
            } finally {
                Platform.runLater { button_traducir.isDisable = false }
            }
        }
        hilo.isDaemon = true
        hilo.start()
    }

    private fun escribirSalida(texto: String) {
        Platform.runLater { salidaPrograma.appendText(texto) }
    }

    private fun registrarHookCierre() {
        if (hookRegistrado.compareAndSet(false, true)) {
            Runtime.getRuntime().addShutdownHook(Thread {
                try { procesoActual?.destroyForcibly() } catch (_: Exception) { }
                directorioTemporal?.let { borrarDirectorio(it) }
            })
        }
    }

    /** Lanza el proceso, vuelca su salida a la UI y devuelve true si termino a tiempo. */
    private fun ejecutarConLimite(pb: ProcessBuilder, charset: java.nio.charset.Charset, mostrar: Boolean, acumulado: StringBuilder?): Pair<Boolean, Int> {
        pb.redirectErrorStream(true)
        val proceso = pb.start()
        procesoActual = proceso
        val lector = Thread {
            try {
                proceso.inputStream.bufferedReader(charset).use { r ->
                    val buf = CharArray(1024)
                    while (true) {
                        val n = r.read(buf)
                        if (n < 0) break
                        val trozo = String(buf, 0, n)
                        acumulado?.append(trozo)
                        if (mostrar) escribirSalida(trozo)
                    }
                }
            } catch (_: Exception) {
            }
        }
        lector.isDaemon = true
        lector.start()

        val aTiempo = proceso.waitFor(10, TimeUnit.SECONDS)
        if (!aTiempo) {
            proceso.destroyForcibly()
            proceso.waitFor()
        }
        lector.join(5000)
        procesoActual = null
        return Pair(aTiempo, if (aTiempo) proceso.exitValue() else -1)
    }

    private fun compilarYEjecutar(codigoFuente: String, nombreClase: String) {
        registrarHookCierre()
        // Directorio temporal propio; se elimina el anterior
        directorioTemporal?.let { borrarDirectorio(it) }
        val dir = Files.createTempDirectory("compilador")
        directorioTemporal = dir

        val archivo = dir.resolve("$nombreClase.java")
        Files.write(archivo, codigoFuente.toByteArray(StandardCharsets.UTF_8))

        escribirSalida("Compilando...\n")
        val javaHome = System.getProperty("java.home")
        val exe = if (System.getProperty("os.name").lowercase().contains("win")) ".exe" else ""

        val compilador = ToolProvider.getSystemJavaCompiler()
        val compilado: Boolean
        val mensajes: String
        if (compilador != null) {
            val diagnosticos = DiagnosticCollector<JavaFileObject>()
            val gestor = compilador.getStandardFileManager(diagnosticos, null, StandardCharsets.UTF_8)
            try {
                val unidades = gestor.getJavaFileObjects(archivo.toFile())
                val opciones = listOf("-encoding", "UTF-8", "-d", dir.toString())
                compilado = compilador.getTask(null, gestor, diagnosticos, opciones, null, unidades).call()
            } finally {
                gestor.close()
            }
            mensajes = diagnosticos.diagnostics.joinToString("\n") {
                "Línea ${it.lineNumber}, columna ${it.columnNumber}: ${it.kind} - ${it.getMessage(null)}"
            }
        } else {
            val javac = File(javaHome, "bin/javac$exe")
            if (!javac.exists()) {
                escribirSalida("No se encontro un compilador de Java (javac). Ejecute la aplicacion con un JDK, no con un JRE.\n")
                return
            }
            val acumulado = StringBuilder()
            val pb = ProcessBuilder(javac.absolutePath, "-J-Dfile.encoding=UTF-8", "-encoding", "UTF-8", "-d", dir.toString(), archivo.toString())
            val (aTiempo, codigo) = ejecutarConLimite(pb, StandardCharsets.UTF_8, false, acumulado)
            if (!aTiempo) {
                escribirSalida("Tiempo limite excedido (10 s) al compilar: el proceso javac fue terminado.\n")
                return
            }
            compilado = codigo == 0
            mensajes = acumulado.toString()
        }

        if (!compilado) {
            escribirSalida("Errores de compilacion:\n$mensajes\n")
            return
        }
        if (mensajes.isNotBlank()) escribirSalida("$mensajes\n")

        if (!Regex("static\\s+void\\s+main\\s*\\(\\s*String\\[\\]\\s+args").containsMatchIn(codigoFuente)) {
            escribirSalida("Compilado correctamente. El programa no tiene una función ^main de tipo vd, no se ejecuta.\n")
            return
        }

        escribirSalida("Ejecutando...\n")
        val pb = ProcessBuilder(
            File(javaHome, "bin/java$exe").absolutePath,
            "-Dfile.encoding=UTF-8", "-Dstdout.encoding=UTF-8", "-Dstderr.encoding=UTF-8",
            "-Dsun.stdout.encoding=UTF-8", "-Dsun.stderr.encoding=UTF-8",
            "-cp", dir.toString(), nombreClase
        )
        val (aTiempo, codigo) = ejecutarConLimite(pb, StandardCharsets.UTF_8, true, null)
        if (!aTiempo) {
            escribirSalida("\nTiempo limite excedido (10 s): el proceso fue terminado.\n")
            return
        }
        escribirSalida("\nProceso terminado (código $codigo)\n")
    }

    private fun borrarDirectorio(dir: Path) {
        try {
            dir.toFile().deleteRecursively()
        } catch (_: Exception) {
        }
    }

}
