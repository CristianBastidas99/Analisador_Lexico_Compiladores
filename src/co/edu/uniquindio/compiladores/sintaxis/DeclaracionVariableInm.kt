package co.edu.uniquindio.compiladores.sintaxis

import co.edu.uniquindio.compiladores.lexico.Token
import javafx.scene.control.TreeItem

class DeclaracionVariableInm(var tipo:Token, var asignacion: Asignacion) : Sentencia() {

    override fun getArbolVisual(): TreeItem<String> {
        val raiz = TreeItem("Declaracion Variable Inmutable")

        raiz.children.add( TreeItem("${tipo.lexema}"))

        raiz.children.add( asignacion.getArbolVisual() )

        return  raiz
    }

}
