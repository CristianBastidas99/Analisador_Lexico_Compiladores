package co.edu.uniquindio.compiladores.sintaxis

import co.edu.uniquindio.compiladores.lexico.Token
import javafx.scene.control.TreeItem


class UnidadDeCompilacion(var visibilidad:Token, var nombre:Token, val listaVariablesGlobales:ArrayList<VariableGlobal>, val listaFunciones:ArrayList<Funcion>) {

    fun getArbolVisual(): TreeItem<String>{

        val raiz = TreeItem("Unidad de compilacion")

        if(!listaVariablesGlobales.isEmpty()) {

            val VariablesGlobales = TreeItem("Variables Globales")
            raiz.children.add(VariablesGlobales)
            for (variableGlobal in listaVariablesGlobales) {
                VariablesGlobales.children.add(variableGlobal.getArbolVisual())
            }
        }

        if(!listaFunciones.isEmpty()) {
            for (funcion in listaFunciones) {
                raiz.children.add(funcion.getArbolVisual())
            }
        }

        return raiz
    }
}