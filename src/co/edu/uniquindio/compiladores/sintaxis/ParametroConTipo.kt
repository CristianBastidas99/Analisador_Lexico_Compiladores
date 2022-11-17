package co.edu.uniquindio.compiladores.sintaxis

import co.edu.uniquindio.compiladores.lexico.Token
import javafx.scene.control.TreeItem

class ParametroConTipo(var tipo: Token, var nombre: Token) {

    fun getArbolVisual(): TreeItem<String>? {

        val raiz = TreeItem("Parametro con tipo")

        raiz.children.add( TreeItem("${tipo.lexema}"))

        raiz.children.add( TreeItem("${nombre.lexema}"))

        return raiz
    }

    fun getCodeJava(): String {
        var codigo = obtenerStringTipo() + " "
        codigo += nombre.lexema.substring(1)
        return  codigo
    }

    fun obtenerStringTipo(): String{
        var stringTipo = "int"
        if(tipo.lexema.equals("dbe")){
            stringTipo = "double"
        }else if(tipo.lexema.equals("str")){
            stringTipo = "String"
        }else if(tipo.lexema.equals("bln")){
            stringTipo = "boolean"
        }
        else if(tipo.lexema.equals("crt")){
            stringTipo = "char"
        }
        return stringTipo
    }

}
