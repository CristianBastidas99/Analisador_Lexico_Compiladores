package co.edu.uniquindio.compiladores.sintaxis

import co.edu.uniquindio.compiladores.lexico.Token
import javafx.scene.control.TreeItem

class Asignacion(var identificador:Token, var operAsignacion:Token, var expresion:Expresion?, var identificador2:Token?, var sentencia: Sentencia?) :Sentencia() {

    override fun getArbolVisual(): TreeItem<String> {

        val raiz = TreeItem("Asignacion")

        raiz.children.add( TreeItem("${identificador.lexema}") )

        raiz.children.add( TreeItem("${operAsignacion.lexema}") )

        if(expresion != null){
            raiz.children.add( expresion!!.getArbolVisual() )
        }

        if(identificador2 != null){
            raiz.children.add( TreeItem("${identificador2!!.lexema}") )
        }

        if(sentencia != null){
            raiz.children.add( sentencia!!.getArbolVisual() )
        }

        return raiz
    }


}
