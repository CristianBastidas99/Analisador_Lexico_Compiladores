package co.edu.uniquindio.compiladores.sintaxis

import co.edu.uniquindio.compiladores.lexico.Error
import co.edu.uniquindio.compiladores.lexico.Token
import co.edu.uniquindio.compiladores.semantico.TablaSimbolos
import javafx.scene.control.TreeItem
import java.util.ArrayList

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

    override fun llenarTablaSimbolos(
        listaErroresSemanticos: ArrayList<Error>,
        tablaSimbolos: TablaSimbolos,
        ambito: String
    ) {
        TODO("Not yet implemented")
    }

    override fun analizarSemantica(
        listaErroresSemanticos: ArrayList<Error>,
        tablaSimbolos: TablaSimbolos,
        ambito: String
    ) {
        TODO("Not yet implemented")
    }

}
