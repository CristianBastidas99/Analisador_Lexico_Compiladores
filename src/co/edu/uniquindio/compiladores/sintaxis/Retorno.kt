package co.edu.uniquindio.compiladores.sintaxis

import co.edu.uniquindio.compiladores.lexico.Token
import javafx.scene.control.TreeItem

class Retorno(var identificado:Token?, var expresion: Expresion?) : Sentencia() {

    override fun getArbolVisual(): TreeItem<String> {
        val raiz = TreeItem("Retorno")

        if(identificado != null) {
            raiz.children.add(TreeItem("${identificado!!.lexema}"))
        }

        if(expresion != null) {
            raiz.children.add( expresion!!.getArbolVisual() )
        }

        return raiz
    }

}
