package co.edu.uniquindio.compiladores.sintaxis

import javafx.scene.control.TreeItem

abstract class Sentencia{

    abstract fun getArbolVisual(): TreeItem<String>
}
