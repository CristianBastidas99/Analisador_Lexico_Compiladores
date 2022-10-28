package co.edu.uniquindio.compiladores.sintaxis

import co.edu.uniquindio.compiladores.lexico.Token
import javafx.scene.control.TreeItem

class Impresion(var expresion: Expresion?, var identificado: Token?) : Sentencia() {
    override fun getArbolVisual(): TreeItem<String> {
        val raiz = TreeItem("Impresion")

        if(expresion != null){
            raiz.children.add( expresion!!.getArbolVisual() )
        }

        if(identificado != null){
            raiz.children.add( TreeItem("${identificado!!.lexema}"))
        }

        return raiz
    }

}
