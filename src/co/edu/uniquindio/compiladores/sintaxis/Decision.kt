package co.edu.uniquindio.compiladores.sintaxis

import co.edu.uniquindio.compiladores.lexico.Error
import co.edu.uniquindio.compiladores.semantico.TablaSimbolos
import javafx.scene.control.TreeItem

class Decision(var exprLogica:ExpresionLogica, val listaSentenciasIf:ArrayList<Sentencia>, val listaSentenciasElse:ArrayList<Sentencia> ) :Sentencia() {
    override fun getArbolVisual(): TreeItem<String> {
        val raiz = TreeItem("Decision")

        raiz.children.add( exprLogica.getArbolVisual() )

        if(listaSentenciasIf.isNotEmpty()) {

            val sentenciasIf = TreeItem("Sentencias en if")
            raiz.children.add(sentenciasIf)
            for (sentencia in listaSentenciasIf) {
                sentenciasIf.children.add(sentencia.getArbolVisual())
            }
        }

        if(listaSentenciasElse.isNotEmpty()) {

            val sentenciasElse = TreeItem("Sentencias en else")
            raiz.children.add(sentenciasElse)
            for (sentencia in listaSentenciasElse) {
                sentenciasElse.children.add(sentencia.getArbolVisual())
            }
        }

        return  raiz
    }

    override fun llenarTablaSimbolos(listaErroresSemanticos: java.util.ArrayList<Error>, tablaSimbolos: TablaSimbolos, ambito: String ) {
        if(listaSentenciasIf.isNotEmpty()){
            for(i in listaSentenciasIf){
                i.llenarTablaSimbolos(listaErroresSemanticos, tablaSimbolos, "$ambito/if")
            }
        }
        if(listaSentenciasElse.isNotEmpty()){
            for(e in listaSentenciasElse){
                e.llenarTablaSimbolos(listaErroresSemanticos, tablaSimbolos, "$ambito/else")
            }
        }
    }

    override fun analizarSemantica(
        listaErroresSemanticos: java.util.ArrayList<Error>,
        tablaSimbolos: TablaSimbolos,
        ambito: String
    ) {
        if(listaSentenciasIf.isNotEmpty()){
            for(i in listaSentenciasIf){
                i.analizarSemantica(listaErroresSemanticos, tablaSimbolos, "$ambito/if")
            }
        }
        if(listaSentenciasElse.isNotEmpty()){
            for(e in listaSentenciasElse){
                e.analizarSemantica(listaErroresSemanticos, tablaSimbolos, "$ambito/else")
            }
        }
    }

    override fun getCodeJava(): String {
        var codigo = "if ( " + exprLogica.getCodeJava() + " ) {\n\n"

        if(listaSentenciasIf.isNotEmpty()){
            for (s in listaSentenciasIf){
                codigo += s.getCodeJava()
            }
        }

        codigo += "} "

        if(listaSentenciasElse.isNotEmpty()){
            codigo += "} else { \n\n"
            for (s in listaSentenciasElse){
                codigo += s.getCodeJava()
            }
            codigo += "}\n\n"
        }

        return codigo
    }


}
