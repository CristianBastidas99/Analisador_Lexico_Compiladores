package co.edu.uniquindio.compiladores.sintaxis

import javafx.scene.control.TreeItem

abstract class Expresion() {

    abstract fun getArbolVisual(): TreeItem<String>
}