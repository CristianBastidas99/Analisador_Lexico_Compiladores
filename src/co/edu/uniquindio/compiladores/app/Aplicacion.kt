package co.edu.uniquindio.compiladores.app

import javafx.application.Application
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.stage.Stage

class Aplicacion : Application() {

    override fun start(p0: Stage?) {

        var loader = FXMLLoader( Aplicacion::class.java.getResource("/inicio.fxml") )
        var parent:Parent = loader.load()

        var scene = Scene( parent )

        p0?.scene = scene
        p0?.title = "Mi compilador"
        p0?.show()

    }

    companion object{

        @JvmStatic
        fun main( args : Array<String> ){
            launch( Aplicacion::class.java )
        }

    }

}