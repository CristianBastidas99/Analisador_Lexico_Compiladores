package co.edu.uniquindio.compiladores.sintaxis

import co.edu.uniquindio.compiladores.lexico.Categoria
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

    override fun obtenerTipo(): String {

        val tipo = numero.categoria
        var returnValue = "ent"

        if(tipo == Categoria.DECIMAL){
            returnValue = "dbe"
            return returnValue
        }else{
            if(exprAritmetica != null) {
                returnValue = exprAritmetica!!.obtenerTipo()
            }
        }

        return returnValue
    }

    override fun getCodeJava(): String {
        var codigo = numero.lexema.substring(1) + " "

        if(operAritmetico != null){
            codigo += operAritmetico!!.lexema + " "
            if(exprAritmetica != null){
                codigo += exprAritmetica!!.getCodeJava()
            }
        }

        return codigo
    }

}
