package co.edu.uniquindio.compiladores.sintaxis

import co.edu.uniquindio.compiladores.lexico.Token
import javafx.scene.control.TreeItem

class DeclaracionVariableMut(var tipo: Token, var identificador: Token) : Sentencia() {

    override fun getArbolVisual(): TreeItem<String> {
        val raiz = TreeItem("Declaracion Variable Mutable")

        raiz.children.add(TreeItem("${tipo.lexema}"))

        raiz.children.add(TreeItem("${identificador.lexema}"))

        return raiz
    }

}
