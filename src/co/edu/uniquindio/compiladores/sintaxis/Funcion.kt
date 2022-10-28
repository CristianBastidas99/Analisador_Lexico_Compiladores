package co.edu.uniquindio.compiladores.sintaxis

import co.edu.uniquindio.compiladores.lexico.Token
import javafx.scene.control.TreeItem

class Funcion(var visibilidad:Token, var valorRetornado:Token, var nombre:Token, val listaParametrosConTipo:ArrayList<ParametroConTipo>, val listaSentencias:ArrayList<Sentencia>) {

    fun getArbolVisual(): TreeItem<String>? {

        val raiz = TreeItem("Funcion")

        raiz.children.add( TreeItem("${visibilidad.lexema}"))

        raiz.children.add( TreeItem("${valorRetornado.lexema}"))

        raiz.children.add( TreeItem("${nombre.lexema}"))

        if(listaParametrosConTipo.isNotEmpty()) {

            val parametrosConTipo = TreeItem("Parametros con tipo")
            raiz.children.add(parametrosConTipo)
            for (parametroConTipo in listaParametrosConTipo) {
                parametrosConTipo.children.add(parametroConTipo.getArbolVisual())
            }
        }

        if(listaSentencias.isNotEmpty()) {

            val sentencias = TreeItem("Sentencias")
            raiz.children.add(sentencias)
            for (sentencia in listaSentencias) {
                sentencias.children.add(sentencia.getArbolVisual())
            }
        }

        return raiz
    }

    override fun toString(): String {
        return "Funcion(visibilidad=${visibilidad.lexema}, valorRetornado=${valorRetornado.lexema}, nombre=${nombre.lexema}, listaParametros=$listaParametrosConTipo, listaSentencias=$listaSentencias)"
    }


}
