package co.edu.uniquindio.compiladores.sintaxis

import co.edu.uniquindio.compiladores.lexico.Categoria
import co.edu.uniquindio.compiladores.lexico.Token
import javafx.scene.control.TreeItem


class ExpresionAritmetica (var numero:Token, var operAritmetico:Token?, var exprAritmetica:ExpresionAritmetica?) : Expresion() {
    override fun getArbolVisual(): TreeItem<String> {
        val raiz = TreeItem("Expresión aritmética")

        raiz.children.add( TreeItem( "${numero.lexema}" ) )

        operAritmetico?.let { operador ->
            raiz.children.add(TreeItem("${operador.lexema}"))
        }

        exprAritmetica?.let { siguiente ->
            raiz.children.add( siguiente.getArbolVisual() )
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
            exprAritmetica?.let { siguiente ->
                returnValue = siguiente.obtenerTipo()
            }
        }

        return returnValue
    }

    override fun getCodeJava(): String {
        var codigo = CodigoJava.numero(numero) + " "

        operAritmetico?.let { operador ->
            codigo += operador.lexema + " "
            exprAritmetica?.let { siguiente ->
                codigo += "(" + siguiente.getCodeJava() + ")"
            }
        }

        return codigo
    }

}
