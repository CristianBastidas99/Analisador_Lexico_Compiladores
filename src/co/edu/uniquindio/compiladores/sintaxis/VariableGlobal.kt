package co.edu.uniquindio.compiladores.sintaxis

import co.edu.uniquindio.compiladores.lexico.Token
import javafx.scene.control.TreeItem

class VariableGlobal(var visibilidad:Token, var tipo:Token, var nombre:Token) {
    fun getArbolVisual(): TreeItem<String>? {

        val raiz = TreeItem("${visibilidad.lexema} ${tipo.lexema} ${nombre.lexema}")

        return raiz
    }

    override fun toString(): String {
        return "VariableGlobal(visibilidad=${visibilidad.lexema}, tipo=${tipo.lexema}, nombre=${nombre.lexema})"
    }


}
