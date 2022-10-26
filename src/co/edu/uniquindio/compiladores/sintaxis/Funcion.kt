package co.edu.uniquindio.compiladores.sintaxis

import co.edu.uniquindio.compiladores.lexico.Token
import javafx.scene.control.TreeItem

class Funcion(var visibilidad:Token, var valorRetornado:Token, var nombre:Token, val listaParametros:ArrayList<Parametro>, val listaSentencias:ArrayList<Sentencia>) {

    fun getArbolVisual(): TreeItem<String>? {

        val raiz = TreeItem("Funcion")

        return raiz
    }

    override fun toString(): String {
        return "Funcion(visibilidad=${visibilidad.lexema}, valorRetornado=${valorRetornado.lexema}, nombre=${nombre.lexema}, listaParametros=$listaParametros, listaSentencias=$listaSentencias)"
    }


}
