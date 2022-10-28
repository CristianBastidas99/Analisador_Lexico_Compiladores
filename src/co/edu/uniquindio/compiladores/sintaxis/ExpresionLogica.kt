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

}
