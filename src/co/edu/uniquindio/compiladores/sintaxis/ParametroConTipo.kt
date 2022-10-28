package co.edu.uniquindio.compiladores.sintaxis

import co.edu.uniquindio.compiladores.lexico.Token
import javafx.scene.control.TreeItem

class ParametroConTipo(var tipo: Token, var nombre: Token) {

    fun getArbolVisual(): TreeItem<String>? {

        val raiz = TreeItem("Parametro con tipo")

        raiz.children.add( TreeItem("${tipo.lexema}"))

        raiz.children.add( TreeItem("${nombre.lexema}"))

        return raiz
    }

}
