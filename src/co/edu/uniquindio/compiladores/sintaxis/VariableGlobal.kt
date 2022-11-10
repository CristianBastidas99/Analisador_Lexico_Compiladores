package co.edu.uniquindio.compiladores.sintaxis

import co.edu.uniquindio.compiladores.lexico.Error
import co.edu.uniquindio.compiladores.lexico.Token
import co.edu.uniquindio.compiladores.semantico.TablaSimbolos
import javafx.scene.control.TreeItem
import java.util.ArrayList

class VariableGlobal(var visibilidad:Token, var tipo:Token, var nombre:Token) {
    fun getArbolVisual(): TreeItem<String>? {

        val raiz = TreeItem("Variable global")

        raiz.children.add(TreeItem("${visibilidad.lexema}"))

        raiz.children.add(TreeItem("${tipo.lexema}"))

        raiz.children.add(TreeItem("${nombre.lexema}"))

        return raiz
    }

    override fun toString(): String {
        return "VariableGlobal(visibilidad=${visibilidad.lexema}, tipo=${tipo.lexema}, nombre=${nombre.lexema})"
    }

    fun llenarTablaSimbolos(listaErroresSemanticos: ArrayList<Error>, tablaSimbolos: TablaSimbolos, lexema: String) {
        tablaSimbolos.guardasSimboloValor(nombre.lexema, tipo.lexema, true, lexema, visibilidad.lexema, visibilidad.fila, visibilidad.columna)
    }


}
