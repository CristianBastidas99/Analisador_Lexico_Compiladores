package co.edu.uniquindio.compiladores.sintaxis

import co.edu.uniquindio.compiladores.lexico.Error
import co.edu.uniquindio.compiladores.lexico.Token
import co.edu.uniquindio.compiladores.semantico.TablaSimbolos
import javafx.scene.control.TreeItem
import java.util.ArrayList

class DeclaracionVariableMut(var tipo: Token, var identificador: Token) : Sentencia() {

    override fun getArbolVisual(): TreeItem<String> {
        val raiz = TreeItem("Declaracion Variable Mutable")

        raiz.children.add(TreeItem("${tipo.lexema}"))

        raiz.children.add(TreeItem("${identificador.lexema}"))

        return raiz
    }

    override fun llenarTablaSimbolos(listaErroresSemanticos: ArrayList<Error>, tablaSimbolos: TablaSimbolos, ambito: String) {
        tablaSimbolos.guardasSimboloValor(identificador.lexema, tipo.lexema, true, ambito, "pvt", tipo.fila, tipo.columna)
    }

    override fun analizarSemantica(
        listaErroresSemanticos: ArrayList<Error>,
        tablaSimbolos: TablaSimbolos,
        ambito: String
    ) {
    }

    override fun getCodeJava(): String {
        return obtenerStringTipo() + " " + identificador.lexema.substring(1) + ";\n"
    }

    fun obtenerStringTipo(): String{
        var stringTipo = "int"
        if(tipo.lexema.equals("dbe")){
            stringTipo = "double"
        }else if(tipo.lexema.equals("str")){
            stringTipo = "String"
        }else if(tipo.lexema.equals("bln")){
            stringTipo = "boolean"
        }
        else if(tipo.lexema.equals("crt")){
            stringTipo = "char"
        }
        return stringTipo
    }

}
