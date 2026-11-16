package co.edu.uniquindio.compiladores.app

import javafx.application.Application

/**
 * Punto de entrada del .jar ejecutable. No extiende Application: así JavaFX
 * puede arrancar desde el classpath sin exigir el módulo en --module-path.
 */
object Lanzador {

    @JvmStatic
    fun main(args: Array<String>) {
        Application.launch(Aplicacion::class.java, *args)
    }
}
