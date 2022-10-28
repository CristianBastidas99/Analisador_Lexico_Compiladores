package co.edu.uniquindio.compiladores.sintaxis

import co.edu.uniquindio.compiladores.lexico.Token
import javafx.scene.control.TreeItem

class IncrDecr (var identificador:Token, var tipo:Token) : Sentencia() {

    override fun getArbolVisual(): TreeItem<String> {
        val raiz = TreeItem("Incremento/Decremento")

        raiz.children.add( TreeItem("${identificador.lexema}"))

        raiz.children.add( TreeItem("${tipo.lexema}"))

        return raiz
    }

}
