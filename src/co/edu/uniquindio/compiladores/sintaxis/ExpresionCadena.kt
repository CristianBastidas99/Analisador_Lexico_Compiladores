package co.edu.uniquindio.compiladores.sintaxis

import co.edu.uniquindio.compiladores.lexico.Token
import javafx.scene.control.TreeItem

class ExpresionCadena(var tipo: Token, var operAgrupacion: Token?, var expresionCadena: ExpresionCadena?) : Expresion() {
    override fun getArbolVisual(): TreeItem<String> {
        val raiz = TreeItem("Expresion cadena")

        raiz.children.add( TreeItem("${tipo.lexema}"))

        if(operAgrupacion != null){
            raiz.children.add( TreeItem("${operAgrupacion!!.lexema}"))
        }

        if(expresionCadena != null){
            raiz.children.add( expresionCadena!!.getArbolVisual())
        }

        return raiz
    }

}
