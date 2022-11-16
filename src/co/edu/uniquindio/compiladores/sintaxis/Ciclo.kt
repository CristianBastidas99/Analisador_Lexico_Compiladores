package co.edu.uniquindio.compiladores.sintaxis

import co.edu.uniquindio.compiladores.lexico.Error
import co.edu.uniquindio.compiladores.semantico.TablaSimbolos
import javafx.scene.control.TreeItem

class Ciclo(var expresionLogica: ExpresionLogica, val listaSentencias:ArrayList<Sentencia>) : Sentencia() {
    override fun getArbolVisual(): TreeItem<String> {
        val raiz = TreeItem("Ciclo")

        raiz.children.add( expresionLogica.getArbolVisual() )

        if(listaSentencias.isNotEmpty()) {

            val sentencias = TreeItem("Sentencias")
            raiz.children.add(sentencias)
            for (sentencia in listaSentencias) {
                sentencias.children.add(sentencia.getArbolVisual())
            }
        }

        return raiz
    }

    override fun llenarTablaSimbolos(listaErroresSemanticos: java.util.ArrayList<Error>, tablaSimbolos: TablaSimbolos, ambito: String ) {
        if(listaSentencias.isNotEmpty()){
            for(s in listaSentencias){
                s.llenarTablaSimbolos(listaErroresSemanticos, tablaSimbolos, "$ambito/while")
            }
        }
    }

    override fun analizarSemantica(listaErroresSemanticos: java.util.ArrayList<Error>, tablaSimbolos: TablaSimbolos, ambito: String) {
        if(listaSentencias.isNotEmpty()){
            for(s in listaSentencias){
                s.analizarSemantica(listaErroresSemanticos, tablaSimbolos, "$ambito/while")
            }
        }
    }

}
