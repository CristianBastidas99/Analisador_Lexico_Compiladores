package co.edu.uniquindio.compiladores.controlador

import co.edu.uniquindio.compiladores.lexico.AnalizadorLexico
import co.edu.uniquindio.compiladores.lexico.Categoria
import co.edu.uniquindio.compiladores.lexico.Error
import co.edu.uniquindio.compiladores.lexico.Token
import co.edu.uniquindio.compiladores.semantico.AnalizadorSemantico
import co.edu.uniquindio.compiladores.semantico.Simbolo
import co.edu.uniquindio.compiladores.sintaxis.AnalizadorSintactico
import co.edu.uniquindio.compiladores.sintaxis.UnidadDeCompilacion
import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.scene.control.Alert
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.Tab
import javafx.scene.control.TabPane
import javafx.scene.control.TableColumn
import javafx.scene.control.TableView
import javafx.scene.control.TextArea
import javafx.scene.control.TreeView
import javafx.scene.control.cell.PropertyValueFactory
import javafx.application.Platform
import javafx.stage.FileChooser
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

    @FXML lateinit var estado : Label
    @FXML lateinit var tabErroresLex : Tab
    @FXML lateinit var tabErroresSint : Tab
    @FXML lateinit var tabErroresSem : Tab
    @FXML lateinit var tablaErroresLex : TableView<Error>
    @FXML lateinit var colm_errorErroresLex : TableColumn<Error, String>
    @FXML lateinit var colm_filaErroresLex : TableColumn<Error, Int>
    @FXML lateinit var colm_columnaErroresLex : TableColumn<Error, Int>

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
    private var isCodigoJava: Boolean = false
    private val generacion = java.util.concurrent.atomic.AtomicInteger(0)
    lateinit var ucg: UnidadDeCompilacion


    @FXML
    fun initialize() {
        codigo_fuente.textProperty().addListener { _, _, _ ->
            if (isCodigoJava || tablaTabLexico.items.isNotEmpty()) {
                isCodigoJava = false
                estado.text = "El código cambió: vuelva a analizar."
            }
        }
        colm_lexemaTabLexico.setCellValueFactory(PropertyValueFactory<Token, String>("lexema"))
        colm_catTabLexico.setCellValueFactory(PropertyValueFactory<Token, Categoria>("categoria"))
        colm_filaTabLexico.setCellValueFactory(PropertyValueFactory<Token, Int>("fila"))
        colm_colmTabLexico.setCellValueFactory(PropertyValueFactory<Token, Int>("columna"))

        colm_errorErroresLex.setCellValueFactory(PropertyValueFactory<Error, String>("error"))
        colm_filaErroresLex.setCellValueFactory(PropertyValueFactory<Error, Int>("fila"))
        colm_columnaErroresLex.setCellValueFactory(PropertyValueFactory<Error, Int>("columna"))

        colm_errorErroresSint.setCellValueFactory(PropertyValueFactory<Error, String>("error"))
        colm_filaErroresSint.setCellValueFactory(PropertyValueFactory<Error, Int>("fila"))
        colm_columnaErroresSint.setCellValueFactory(PropertyValueFactory<Error, Int>("columna"))

        colm_tsNombre.setCellValueFactory(PropertyValueFactory<Simbolo, String>("nombre"))
        colm_tsTipo.setCellValueFactory(PropertyValueFactory<Simbolo, String>("tipo"))
        colm_tsModificable.setCellValueFactory(PropertyValueFactory<Simbolo, Boolean>("modificable"))
        colm_tsAmbito.setCellValueFactory(PropertyValueFactory<Simbolo, String>("ambito"))
        colm_tsAcceso.setCellValueFactory(PropertyValueFactory<Simbolo, String>("acceso"))
        colm_tsFila.setCellValueFactory(PropertyValueFactory<Simbolo, Int>("fila"))
        colm_tsColumna.setCellValueFactory(PropertyValueFactory<Simbolo, Int>("columna"))
        colm_tsTiposParametros.setCellValueFactory(PropertyValueFactory<Simbolo, ArrayList<String>>("tiposParametros"))

        colm_esemError.setCellValueFactory(PropertyValueFactory<Error, String>("error"))
        colm_esemFila.setCellValueFactory(PropertyValueFactory<Error, Int>("fila"))
        colm_esemColumna.setCellValueFactory(PropertyValueFactory<Error, Int>("columna"))
    }

    @FXML
    fun abrirArchivo(e : ActionEvent?){
        val selector = FileChooser()
        selector.title = "Abrir archivo de código"
        selector.extensionFilters.addAll(
            FileChooser.ExtensionFilter("Archivos de texto (*.txt)", "*.txt"),
            FileChooser.ExtensionFilter("Todos los archivos", "*.*")
        )
        val ejemplos = File("ejemplos")
        if (ejemplos.isDirectory) selector.initialDirectory = ejemplos.absoluteFile

        val archivo = selector.showOpenDialog(codigo_fuente.scene?.window) ?: return
        val texto: String
        try {
            texto = String(Files.readAllBytes(archivo.toPath()), StandardCharsets.UTF_8).replace("\r\n", "\n")
        } catch (ex: Exception) {
            val aviso = Alert(Alert.AlertType.ERROR, "No se pudo leer el archivo ${archivo.name}: ${ex.message ?: ex.javaClass.simpleName}")
            aviso.headerText = "Error al abrir el archivo"
            aviso.showAndWait()
            estado.text = "No se pudo abrir ${archivo.name}."
            return
        }
        // Cargar un archivo equivale a editar: se invalida el análisis anterior
        cancelarEjecucion()
        limpiarTodo()
        codigo_fuente.text = texto
        estado.text = "Archivo cargado: ${archivo.name}. Pulse Analizar Código."
    }

    private fun titulo(base: String, n: Int) = if (n > 0) "$base ($n)" else base

    private fun limpiarTodo() {
        tablaTabLexico.items.clear()
        tablaErroresLex.items.clear()
        tablaErroresSint.items.clear()
        tablaSimbolos.items.clear()
        tablaErroresSem.items.clear()
        arbolVisual.root = null
        codigoJava.clear()
        salidaPrograma.clear()
        isCodigoJava = false
        tabErroresLex.text = titulo("Errores Léxicos", 0)
        tabErroresSint.text = titulo("Errores Sintácticos", 0)
        tabErroresSem.text = titulo("Errores Semánticos", 0)
    }

    private fun cancelarEjecucion() {
        generacion.incrementAndGet()
        try { procesoActual?.destroyForcibly() } catch (_: Exception) { }
        button_traducir.isDisable = false
    }

    private fun plural(n: Int, singular: String, plural: String) = if (n == 1) "1 $singular" else "$n $plural"

    @FXML
    fun analizarCodigo(e : ActionEvent?){
        cancelarEjecucion()
        limpiarTodo()
        try {
            analizar()
        } catch (ex: Exception) {
            ex.printStackTrace()
            isCodigoJava = false
            estado.text = "Error interno del analizador: ${ex.message ?: ex.javaClass.simpleName}"
        }
    }

    private fun analizar() {
        if(codigo_fuente.text.isBlank()) {
            estado.text = "El área de código está vacía: escriba un programa para analizar."
            return
        }

        val analizador = AnalizadorLexico(codigo_fuente.text)
        analizador.analizar()
        println(analizador.listaTokens)

        tablaTabLexico.items.addAll(analizador.listaTokens)
        tablaErroresLex.items.addAll(analizador.listaErrores)
        tabErroresLex.text = titulo("Errores Léxicos", analizador.listaErrores.size)

        val analizadorSintactico = AnalizadorSintactico(analizador.listaTokens)
        val uc = analizadorSintactico.esUnidadDeCompilacion()

        if(uc != null) {
            arbolVisual.root = uc.getArbolVisual()
            println(uc.toString())
        }

        // Los errores sintácticos se muestran siempre
        tablaErroresSint.items.addAll(analizadorSintactico.listaErrores)
        tabErroresSint.text = titulo("Errores Sintácticos", analizadorSintactico.listaErrores.size)

        val nLex = analizador.listaErrores.size
        val nSint = analizadorSintactico.listaErrores.size
        if(nLex > 0 || nSint > 0 || uc == null) {
            val partes = ArrayList<String>()
            if(nLex > 0) partes.add(plural(nLex, "error léxico", "errores léxicos"))
            if(nSint > 0) partes.add(plural(nSint, "error sintáctico", "errores sintácticos"))
            if(partes.isEmpty()) partes.add("el programa no es una unidad de compilación válida")
            estado.text = "Hay " + partes.joinToString(" y ") + ": no se ejecuta el análisis semántico ni se puede traducir."
            if(nLex > 0) tabPane.selectionModel.select(tabErroresLex)
            else if(nSint > 0) tabPane.selectionModel.select(tabErroresSint)
            return
        }

        val semantica = AnalizadorSemantico(uc)
        semantica.llenarTablaSimbolos()
        semantica.analizarSemantica()
        println(semantica.tablaSimbolos)

        tablaSimbolos.items.addAll(semantica.tablaSimbolos.listaSimbolos)

        val nSem = semantica.tablaSimbolos.listaErrores.size
        tablaErroresSem.items.addAll(semantica.tablaSimbolos.listaErrores)
        tabErroresSem.text = titulo("Errores Semánticos", nSem)

        if(nSem > 0) {
            estado.text = plural(nSem, "error semántico", "errores semánticos") + ": corríjalos para poder traducir."
            tabPane.selectionModel.select(tabErroresSem)
        } else {
            isCodigoJava = true
            ucg = uc
            estado.text = "Análisis correcto: listo para traducir."
        }
    }

    @FXML
    fun traducirCodigo(e : ActionEvent?){
        if(!isCodigoJava){
            val msg = "No se puede traducir: analice primero un programa sin errores."
            estado.text = msg
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
        val gen = generacion.incrementAndGet()

        val hilo = Thread {
            try {
                compilarYEjecutar(codigoFuente, nombreClase, gen)
            } catch (ex: Exception) {
                escribirSalida("Error inesperado: ${ex.message}\n", gen)
            } finally {
                Platform.runLater { if (generacion.get() == gen) button_traducir.isDisable = false }
            }
        }
        hilo.isDaemon = true
        hilo.start()
    }

    private fun escribirSalida(texto: String, gen: Int) {
        Platform.runLater { if (generacion.get() == gen) salidaPrograma.appendText(texto) }
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
    private fun ejecutarConLimite(pb: ProcessBuilder, charset: java.nio.charset.Charset, mostrar: Boolean, acumulado: StringBuilder?, gen: Int): Pair<Boolean, Int> {
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
                        if (mostrar) escribirSalida(trozo, gen)
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

    private fun compilarYEjecutar(codigoFuente: String, nombreClase: String, gen: Int) {
        registrarHookCierre()
        // Directorio temporal propio; se elimina el anterior
        directorioTemporal?.let { borrarDirectorio(it) }
        val dir = Files.createTempDirectory("compilador")
        directorioTemporal = dir

        val archivo = dir.resolve("$nombreClase.java")
        Files.write(archivo, codigoFuente.toByteArray(StandardCharsets.UTF_8))

        escribirSalida("Compilando...\n", gen)
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
                escribirSalida("No se encontró un compilador de Java (javac). Ejecute la aplicación con un JDK, no con un JRE.\n", gen)
                return
            }
            val acumulado = StringBuilder()
            val pb = ProcessBuilder(javac.absolutePath, "-J-Dfile.encoding=UTF-8", "-encoding", "UTF-8", "-d", dir.toString(), archivo.toString())
            val (aTiempo, codigo) = ejecutarConLimite(pb, StandardCharsets.UTF_8, false, acumulado, gen)
            if (!aTiempo) {
                escribirSalida("Tiempo límite excedido (10 s) al compilar: el proceso javac fue terminado.\n", gen)
                return
            }
            compilado = codigo == 0
            mensajes = acumulado.toString()
        }

        if (!compilado) {
            escribirSalida("Errores de compilación:\n$mensajes\n", gen)
            return
        }
        if (mensajes.isNotBlank()) escribirSalida("$mensajes\n", gen)

        if (!Regex("static\\s+void\\s+main\\s*\\(\\s*String\\[\\]\\s+args").containsMatchIn(codigoFuente)) {
            escribirSalida("Compilado correctamente. El programa no tiene una función ^main de tipo vd, no se ejecuta.\n", gen)
            return
        }

        if (generacion.get() != gen) return
        escribirSalida("Ejecutando...\n", gen)
        val pb = ProcessBuilder(
            File(javaHome, "bin/java$exe").absolutePath,
            "-Dfile.encoding=UTF-8", "-Dstdout.encoding=UTF-8", "-Dstderr.encoding=UTF-8",
            "-Dsun.stdout.encoding=UTF-8", "-Dsun.stderr.encoding=UTF-8",
            "-cp", dir.toString(), nombreClase
        )
        val (aTiempo, codigo) = ejecutarConLimite(pb, StandardCharsets.UTF_8, true, null, gen)
        if (!aTiempo) {
            escribirSalida("\nTiempo límite excedido (10 s): el proceso fue terminado.\n", gen)
            return
        }
        escribirSalida("\nProceso terminado (código $codigo)\n", gen)
    }

    private fun borrarDirectorio(dir: Path) {
        try {
            dir.toFile().deleteRecursively()
        } catch (_: Exception) {
        }
    }

}
