package co.edu.uniquindio.compiladores.sintaxis

import co.edu.uniquindio.compiladores.lexico.Token
import javafx.scene.control.TreeItem

class ExpresionLogica(var negacion:Token?, var exprRelacional:ExpresionRelacional, var operLogico:Token?, var exprLogica:ExpresionLogica?) : Expresion() {
    override fun getArbolVisual(): TreeItem<String> {
        val raiz = TreeItem("Expresión lógica")

        negacion?.let {
            raiz.children.add( TreeItem("${it.lexema}") )
        }

        raiz.children.add( exprRelacional.getArbolVisual() )

        operLogico?.let {
            raiz.children.add( TreeItem("${it.lexema}") )
        }

        exprLogica?.let {
            raiz.children.add( it.getArbolVisual() )
        }

        return raiz
    }

    override fun obtenerTipo(): String {
        return "bln"
    }

    override fun getCodeJava(): String {
        var codigo = ""
        if(negacion != null){
            codigo += "!"
        }
        codigo += "(" + exprRelacional.getCodeJava() + ")"
        val siguiente = exprLogica
        if(operLogico != null && siguiente != null){
            codigo += " " + stringOpeLogico() + " (" + siguiente.getCodeJava() + ")"
        }
        return codigo
    }

    fun stringOpeLogico():String{
        var codigo = "&&"

        val operador = operLogico?.lexema
        if(operador == "xor"){
            codigo = "^"
        }else if(operador == "or"){
            codigo = "||"
        }
        return codigo
    }

}
