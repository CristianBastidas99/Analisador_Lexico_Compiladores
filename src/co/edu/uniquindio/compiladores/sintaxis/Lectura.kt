package co.edu.uniquindio.compiladores.sintaxis

import co.edu.uniquindio.compiladores.lexico.Error
import co.edu.uniquindio.compiladores.lexico.Token
import co.edu.uniquindio.compiladores.semantico.TablaSimbolos
import javafx.scene.control.TreeItem
import java.util.ArrayList


class Lectura(var cadena:Token) : Sentencia() {

    /** Tipo y nombre de la variable que recibe la lectura; los fija Asignacion durante el analisis semantico */
    var tipoDestino: String? = null
    var nombreDestino: String? = null
    var posicionDestino: Token? = null

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

        val tipo = tipoDestino ?: return
        if(tipo != "str" && tipo != "ent" && tipo != "dbe"){
            listaErroresSemanticos.add(Error("La variable ($nombreDestino) es de tipo $tipo: la lectura (rad) solo se puede asignar a variables str, ent o dbe", (posicionDestino ?: cadena).fila, (posicionDestino ?: cadena).columna))
        }
    }

    override fun getCodeJava(): String {
        val lectura = "JOptionPane.showInputDialog(" + CodigoJava.cadena(cadena.lexema) + ")"
        return when (tipoDestino) {
            "ent" -> "Integer.parseInt($lectura);\n"
            "dbe" -> "Double.parseDouble($lectura);\n"
            else -> "$lectura;\n"
        }
    }

}
