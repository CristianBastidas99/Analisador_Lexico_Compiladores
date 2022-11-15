package co.edu.uniquindio.compiladores.sintaxis

import co.edu.uniquindio.compiladores.lexico.Token
import co.edu.uniquindio.compiladores.semantico.TablaSimbolos
import javafx.scene.control.TreeItem
import co.edu.uniquindio.compiladores.lexico.Error


class UnidadDeCompilacion(var visibilidad:Token, var nombre:Token, val listaVariablesGlobales:ArrayList<VariableGlobal>, val listaFunciones:ArrayList<Funcion>) {

    fun getArbolVisual(): TreeItem<String>{

        val raiz = TreeItem("Unidad de compilacion")

        raiz.children.add( TreeItem("${visibilidad.lexema}"))

        raiz.children.add( TreeItem("${nombre.lexema}"))

        if(listaVariablesGlobales.isNotEmpty()) {

            val variablesGlobales = TreeItem("Variables Globales")
            raiz.children.add(variablesGlobales)
            for (variableGlobal in listaVariablesGlobales) {
                variablesGlobales.children.add(variableGlobal.getArbolVisual())
            }
        }

        if(listaFunciones.isNotEmpty()) {
            for (funcion in listaFunciones) {
                raiz.children.add(funcion.getArbolVisual())
            }
        }

        return raiz
    }

    fun llenarTablaSimbolos(listaErroresSemanticos: ArrayList<Error>, tablaSimbolos: TablaSimbolos) {
        if(listaVariablesGlobales.isNotEmpty()) {
            for (variableGlobal in listaVariablesGlobales) {
                variableGlobal.llenarTablaSimbolos(listaErroresSemanticos, tablaSimbolos, nombre.lexema)
            }
        }
        if(listaFunciones.isNotEmpty()) {
            for (funcion in listaFunciones) {
                funcion.llenarTablaSimbolos(listaErroresSemanticos, tablaSimbolos, nombre.lexema)
            }
        }
    }

    fun analizarSemantica(listaErroresSemanticos: ArrayList<Error>, tablaSimbolos: TablaSimbolos) {
        if(listaVariablesGlobales.isNotEmpty()) {
            for (variableGlobal in listaVariablesGlobales) {
                variableGlobal.analizarSemantica(listaErroresSemanticos, tablaSimbolos, nombre.lexema)
            }
        }
        if(listaFunciones.isNotEmpty()) {
            for (funcion in listaFunciones) {
                funcion.analizarSemantica(listaErroresSemanticos, tablaSimbolos)
            }
        }
    }


    override fun toString(): String {
        return "UnidadDeCompilacion(visibilidad=${visibilidad.lexema}, nombre=${nombre.lexema}, listaVariablesGlobales=$listaVariablesGlobales, listaFunciones=$listaFunciones)"
    }


}