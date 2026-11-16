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
        if (operAgrupacion == null || expresionCadena == null) {
            return stringValorTipo()
        }
        // El prefijo "" garantiza concatenacion de texto (en Java char + char suma enteros)
        return "\"\" + " + partes()
    }

    private fun partes(): String {
        var codigo = stringValorTipo()
        if (operAgrupacion != null && expresionCadena != null) {
            codigo += " + " + expresionCadena!!.partes()
        }
        return codigo
    }

    fun stringValorTipo(): String{
        return when (valorTipo.categoria) {
            Categoria.CADENA_CARCTERES -> CodigoJava.cadena(valorTipo.lexema)
            Categoria.CARACTER -> CodigoJava.caracter(valorTipo.lexema)
            else -> CodigoJava.numero(valorTipo)
        }
    }

}
