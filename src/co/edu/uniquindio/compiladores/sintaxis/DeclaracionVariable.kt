package co.edu.uniquindio.compiladores.sintaxis

import co.edu.uniquindio.compiladores.lexico.Token
import javafx.scene.control.TreeItem

class DeclaracionVariable(var tipoAcceso:Token, var tipo:Token, var nombre:Token?, var asignacion:Asignacion? ) :Sentencia() {
    override fun getArbolVisual(): TreeItem<String> {
        val raiz = TreeItem("Declaracion Variable")

        raiz.children.add( TreeItem("${tipoAcceso.lexema} ${tipo.lexema}"))

        if(nombre != null){
            raiz.children.add( TreeItem("${nombre!!.lexema}"))
        }

        if(asignacion != null){
            raiz.children.add( asignacion!!.getArbolVisual() )
        }

        return  raiz
    }

}
