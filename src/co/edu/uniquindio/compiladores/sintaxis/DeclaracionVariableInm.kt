package co.edu.uniquindio.compiladores.sintaxis

import co.edu.uniquindio.compiladores.lexico.Error
import co.edu.uniquindio.compiladores.lexico.Token
import co.edu.uniquindio.compiladores.semantico.TablaSimbolos
import javafx.scene.control.TreeItem
import java.util.ArrayList

class DeclaracionVariableInm(var tipo:Token, var asignacion: Asignacion) : Sentencia() {

    override fun getArbolVisual(): TreeItem<String> {
        val raiz = TreeItem("Declaracion Variable Inmutable")

        raiz.children.add( TreeItem("${tipo.lexema}"))

        raiz.children.add( asignacion.getArbolVisual() )

        return  raiz
    }

    override fun llenarTablaSimbolos(listaErroresSemanticos: ArrayList<Error>, tablaSimbolos: TablaSimbolos, ambito: String) {
        tablaSimbolos.guardasSimboloValor(asignacion.identificador.lexema, tipo.lexema, true, ambito, "pvt", tipo.fila, tipo.columna)
    }

    override fun analizarSemantica(
        listaErroresSemanticos: ArrayList<Error>,
        tablaSimbolos: TablaSimbolos,
        ambito: String
    ) {
        asignacion.analizarSemantica(listaErroresSemanticos,tablaSimbolos, ambito)
    }

}
