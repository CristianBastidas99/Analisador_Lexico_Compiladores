package co.edu.uniquindio.compiladores.sintaxis

import co.edu.uniquindio.compiladores.lexico.Token
import javafx.scene.control.TreeItem

class ParametroSinTipo(var nombre: Token) {

    fun getArbolVisual(): TreeItem<String>? {

        val raiz = TreeItem("Parametro sin tipo")

        raiz.children.add( TreeItem("${nombre.lexema}"))

        return raiz
    }

}
