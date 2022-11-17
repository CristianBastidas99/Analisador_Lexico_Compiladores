package co.edu.uniquindio.compiladores.sintaxis

import co.edu.uniquindio.compiladores.lexico.Token
import javafx.scene.control.TreeItem

class ExpresionRelacional(var expAritmetica1:ExpresionAritmetica, var opeRelacional:Token, var expAritmetica2:ExpresionAritmetica) : Expresion() {
    override fun getArbolVisual(): TreeItem<String> {
        val raiz = TreeItem("Expresion relacional")

        raiz.children.add( expAritmetica1.getArbolVisual() )

        raiz.children.add( TreeItem("${opeRelacional.lexema}"))

        raiz.children.add( expAritmetica2.getArbolVisual() )

        return raiz
    }

    override fun obtenerTipo(): String {
        return "bln"
    }

    override fun getCodeJava(): String {
        var codigo = "(" + expAritmetica1.getCodeJava() + ") " + opeRelacional.lexema + " (" + expAritmetica2.getCodeJava() + ")"
        return  codigo
    }

}
