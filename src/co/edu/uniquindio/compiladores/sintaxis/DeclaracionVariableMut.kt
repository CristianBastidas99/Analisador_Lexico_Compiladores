package co.edu.uniquindio.compiladores.sintaxis

import co.edu.uniquindio.compiladores.lexico.Error
import co.edu.uniquindio.compiladores.lexico.Token
import co.edu.uniquindio.compiladores.semantico.TablaSimbolos
import javafx.scene.control.TreeItem
import java.util.ArrayList

class DeclaracionVariableMut(var tipo: Token, var identificador: Token) : Sentencia() {

    override fun getArbolVisual(): TreeItem<String> {
        val raiz = TreeItem("Declaración Variable Mutable")

        raiz.children.add(TreeItem("${tipo.lexema}"))

        raiz.children.add(TreeItem("${identificador.lexema}"))

        return raiz
    }

    override fun llenarTablaSimbolos(listaErroresSemanticos: ArrayList<Error>, tablaSimbolos: TablaSimbolos, ambito: String) {
        tablaSimbolos.guardarSimboloValor(identificador.lexema, tipo.lexema, true, ambito, "pvt", identificador.fila, identificador.columna)
    }

    override fun analizarSemantica(
        listaErroresSemanticos: ArrayList<Error>,
        tablaSimbolos: TablaSimbolos,
        ambito: String
    ) {
    }

    override fun getCodeJava(): String {
        return CodigoJava.tipo(tipo.lexema) + " " + CodigoJava.declarar(identificador.lexema) + " = " + CodigoJava.valorPorDefecto(tipo.lexema) + ";\n"
    }

}
