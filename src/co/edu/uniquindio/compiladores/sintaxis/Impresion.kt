package co.edu.uniquindio.compiladores.sintaxis

import co.edu.uniquindio.compiladores.lexico.Error
import co.edu.uniquindio.compiladores.lexico.Token
import co.edu.uniquindio.compiladores.semantico.TablaSimbolos
import javafx.scene.control.TreeItem
import java.util.ArrayList

class Impresion(var expresion: Expresion?, var identificado: Token?) : Sentencia() {
    override fun getArbolVisual(): TreeItem<String> {
        val raiz = TreeItem("Impresion")

        if(expresion != null){
            raiz.children.add( expresion!!.getArbolVisual() )
        }

        if(identificado != null){
            raiz.children.add( TreeItem("${identificado!!.lexema}"))
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

        if(identificado != null) {
            var simbolo = tablaSimbolos.buscarSimboloValor(identificado!!.lexema, ambito)

            if (simbolo == null) {
                listaErroresSemanticos.add(
                    Error(
                        "La variable que intenta imprimir no se ha declarado",
                        identificado!!.fila,
                        identificado!!.columna
                    )
                )
            }
        }
    }

}
