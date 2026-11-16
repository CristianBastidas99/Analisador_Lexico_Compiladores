package co.edu.uniquindio.compiladores.sintaxis

import co.edu.uniquindio.compiladores.lexico.Error
import co.edu.uniquindio.compiladores.lexico.Token
import co.edu.uniquindio.compiladores.semantico.TablaSimbolos
import javafx.scene.control.TreeItem
import java.util.ArrayList

class Impresion(var expresion: Expresion?, var identificador: Token?) : Sentencia() {
    override fun getArbolVisual(): TreeItem<String> {
        val raiz = TreeItem("Impresión")

        expresion?.let {
            raiz.children.add( it.getArbolVisual() )
        }

        identificador?.let {
            raiz.children.add( TreeItem("${it.lexema}"))
        }

        return raiz
    }

    override fun llenarTablaSimbolos(
        listaErroresSemanticos: ArrayList<Error>,
        tablaSimbolos: TablaSimbolos,
        ambito: String
    ) {
    }

    override fun analizarSemantica(listaErroresSemanticos: ArrayList<Error>, tablaSimbolos: TablaSimbolos, ambito: String) {

        identificador?.let { id ->
            val simbolo = tablaSimbolos.buscarSimboloValorEnAmbitos(id.lexema, ambito)

            if (simbolo == null) {
                listaErroresSemanticos.add(
                    Error(
                        "La variable (${id.lexema}) que intenta imprimir no se ha declarado",
                        id.fila,
                        id.columna
                    )
                )
            }
        }
    }

    override fun getCodeJava(): String {
        var codigo = "System.out.print("

        expresion?.let {
            codigo += it.getCodeJava()
        }

        identificador?.let {
            codigo += CodigoJava.usar(it.lexema)
        }

        codigo += ");\n"
        return codigo
    }

}
