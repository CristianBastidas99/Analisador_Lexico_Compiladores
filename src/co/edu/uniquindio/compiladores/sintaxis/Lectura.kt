package co.edu.uniquindio.compiladores.sintaxis

import co.edu.uniquindio.compiladores.lexico.Error
import co.edu.uniquindio.compiladores.lexico.Token
import co.edu.uniquindio.compiladores.semantico.TablaSimbolos
import javafx.scene.control.TreeItem
import java.util.ArrayList


class Lectura(var cadena:Token) : Sentencia() {
    override fun getArbolVisual(): TreeItem<String> {
        val raiz = TreeItem("Lectura")

        raiz.children.add( TreeItem("${cadena.lexema}"))

        return raiz
    }

    override fun llenarTablaSimbolos(
        listaErroresSemanticos: ArrayList<Error>,
        tablaSimbolos: TablaSimbolos,
        ambito: String
    ) {

    }

    override fun analizarSemantica(listaErroresSemanticos: java.util.ArrayList<Error>, tablaSimbolos: TablaSimbolos, ambito: String) {

        if(ambito != "str"){
            listaErroresSemanticos.add(Error("El tipo de la variable ($ambito) no coincide con la lectura (str)", cadena.fila, cadena.columna))
        }
    }

    override fun getCodeJava(): String {
        return "JOptionPane.showInputDialog(\"" + cadena.lexema.substring(1, cadena.lexema.length - 1) + "\");\n\n"
    }

}
