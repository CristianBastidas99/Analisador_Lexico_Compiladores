package co.edu.uniquindio.compiladores.sintaxis

import co.edu.uniquindio.compiladores.lexico.Token
import javafx.scene.control.TreeItem

class ParametroConTipo(var tipo: Token, var nombre: Token) {

    fun getArbolVisual(): TreeItem<String>? {

        val raiz = TreeItem("Parámetro con tipo")

        raiz.children.add( TreeItem("${tipo.lexema}"))

        raiz.children.add( TreeItem("${nombre.lexema}"))

        return raiz
    }

    fun getCodeJava(): String {
        var codigo = CodigoJava.tipo(tipo.lexema) + " "
        codigo += CodigoJava.declarar(nombre.lexema)
        return  codigo
    }

}
