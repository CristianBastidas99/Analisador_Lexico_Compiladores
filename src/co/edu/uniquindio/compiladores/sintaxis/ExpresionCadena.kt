package co.edu.uniquindio.compiladores.sintaxis

import co.edu.uniquindio.compiladores.lexico.Categoria
import co.edu.uniquindio.compiladores.lexico.Token
import javafx.scene.control.TreeItem

class ExpresionCadena(var valorTipo: Token, var operAgrupacion: Token?, var expresionCadena: ExpresionCadena?) : Expresion() {
    override fun getArbolVisual(): TreeItem<String> {
        val raiz = TreeItem("Expresión cadena")

        raiz.children.add( TreeItem("${valorTipo.lexema}"))

        operAgrupacion?.let {
            raiz.children.add( TreeItem("${it.lexema}"))
        }

        expresionCadena?.let {
            raiz.children.add( it.getArbolVisual())
        }

        return raiz
    }

    override fun obtenerTipo(): String {
        // Un caracter suelto (sin concatenacion) es crt; con concatenacion el resultado es str
        if (operAgrupacion == null && expresionCadena == null && valorTipo.categoria == Categoria.CARACTER) {
            return "crt"
        }
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
        val siguiente = expresionCadena
        if (operAgrupacion != null && siguiente != null) {
            codigo += " + " + siguiente.partes()
        }
        return codigo
    }

    fun stringValorTipo(): String{
        return when (valorTipo.categoria) {
            Categoria.CADENA_CARACTERES -> CodigoJava.cadena(valorTipo.lexema)
            Categoria.CARACTER -> CodigoJava.caracter(valorTipo.lexema)
            else -> CodigoJava.numero(valorTipo)
        }
    }

}
