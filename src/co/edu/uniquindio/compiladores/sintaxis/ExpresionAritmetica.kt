package co.edu.uniquindio.compiladores.sintaxis

import co.edu.uniquindio.compiladores.lexico.Token
import javafx.scene.control.TreeItem


class ExpresionAritmetica (var numero:Token, var operAritmetico:Token?, var exprAritmetica:ExpresionAritmetica?) : Expresion() {
    override fun getArbolVisual(): TreeItem<String> {
        val raiz = TreeItem("Expresion aritmetica")

        raiz.children.add( TreeItem( "${numero.lexema}" ) )

        if(operAritmetico != null) {
            raiz.children.add(TreeItem("${operAritmetico!!.lexema}"))
        }

        if(exprAritmetica != null) {
            raiz.children.add( exprAritmetica!!.getArbolVisual() )
        }

        return raiz
    }

}
