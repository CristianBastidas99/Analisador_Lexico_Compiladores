package co.edu.uniquindio.compiladores.sintaxis

import co.edu.uniquindio.compiladores.lexico.Token
import javafx.scene.control.TreeItem


class Lectura(var cadena:Token) : Sentencia() {
    override fun getArbolVisual(): TreeItem<String> {
        val raiz = TreeItem("Lectura")

        raiz.children.add( TreeItem("${cadena.lexema}"))

        return raiz
    }

}
