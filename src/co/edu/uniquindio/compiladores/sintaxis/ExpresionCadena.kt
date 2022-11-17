package co.edu.uniquindio.compiladores.sintaxis

import co.edu.uniquindio.compiladores.lexico.Categoria
import co.edu.uniquindio.compiladores.lexico.Token
import javafx.scene.control.TreeItem

class ExpresionCadena(var valorTipo: Token, var operAgrupacion: Token?, var expresionCadena: ExpresionCadena?) : Expresion() {
    override fun getArbolVisual(): TreeItem<String> {
        val raiz = TreeItem("Expresion cadena")

        raiz.children.add( TreeItem("${valorTipo.lexema}"))

        if(operAgrupacion != null){
            raiz.children.add( TreeItem("${operAgrupacion!!.lexema}"))
        }

        if(expresionCadena != null){
            raiz.children.add( expresionCadena!!.getArbolVisual())
        }

        return raiz
    }

    override fun obtenerTipo(): String {
        return "str"
    }

    override fun getCodeJava(): String {
        var codigo = stringValorTipo() + " "

        if(operAgrupacion != null){
            codigo += operAgrupacion!!.lexema + " "
            if(expresionCadena != null){
                codigo += expresionCadena!!.getCodeJava()
            }
        }

        return codigo
    }

    fun stringValorTipo(): String{
        var codigo = ""
        if(valorTipo.categoria == Categoria.CADENA_CARCTERES){
            codigo += "\"" + valorTipo.lexema.substring(1, valorTipo.lexema.length - 1) + "\""
        }else if(valorTipo.categoria == Categoria.CARACTER){
            codigo += "\'" + valorTipo.lexema.substring(1, valorTipo.lexema.length - 1) + "\'"
        }else{
            codigo += valorTipo.lexema.substring(1)
        }
        return codigo
    }

}
