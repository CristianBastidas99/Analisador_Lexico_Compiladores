package co.edu.uniquindio.compiladores.sintaxis

import co.edu.uniquindio.compiladores.lexico.Token
import javafx.scene.control.TreeItem

class ExpresionLogica(var negacion:Token?, var exprRelacional:ExpresionRelacional, var operLogico:Token?, var exprLogica:ExpresionLogica?) : Expresion() {
    override fun getArbolVisual(): TreeItem<String> {
        val raiz = TreeItem("Expresion logica")

        if(negacion != null){
            raiz.children.add( TreeItem("${negacion!!.lexema}") )
        }

        raiz.children.add( exprRelacional.getArbolVisual() )

        if(operLogico != null){
            raiz.children.add( TreeItem("${operLogico!!.lexema}") )
        }

        if(exprLogica != null){
            raiz.children.add( exprLogica!!.getArbolVisual() )
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

        codigo += "(" + exprRelacional + ") "

        if(operLogico != null){
            codigo += stringOpeLogico() + " "
                if(exprLogica != null){
                    codigo += exprLogica!!.getCodeJava()
                }
        }
        return  codigo
    }

    fun stringOpeLogico():String{
        var codigo = "&&"

        if(operLogico!!.lexema.equals("xor")){
            codigo = "^"
        }else if(operLogico!!.lexema.equals("or")){
            codigo = "||"
        }
        return codigo
    }

}
