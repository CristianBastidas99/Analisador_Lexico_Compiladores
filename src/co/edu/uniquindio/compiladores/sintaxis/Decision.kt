package co.edu.uniquindio.compiladores.sintaxis

import co.edu.uniquindio.compiladores.lexico.Error
import co.edu.uniquindio.compiladores.semantico.TablaSimbolos
import javafx.scene.control.TreeItem

class Decision(var exprLogica:ExpresionLogica, val listaSentenciasIf:ArrayList<Sentencia>, val listaSentenciasElse:ArrayList<Sentencia> ) :Sentencia() {
    override fun getArbolVisual(): TreeItem<String> {
        val raiz = TreeItem("Decisión")

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

    /** Ambitos unicos y deterministas por bloque: se basan en la posicion del operador relacional de la condicion */
    private fun posicion(): String {
        val t = exprLogica.exprRelacional.opeRelacional
        return "@${t.fila}:${t.columna}"
    }

    private fun ambitoIf(ambito: String) = "$ambito/if" + posicion()

    private fun ambitoElse(ambito: String) = "$ambito/else" + posicion()

    override fun llenarTablaSimbolos(listaErroresSemanticos: java.util.ArrayList<Error>, tablaSimbolos: TablaSimbolos, ambito: String ) {
        if(listaSentenciasIf.isNotEmpty()){
            for(i in listaSentenciasIf){
                i.llenarTablaSimbolos(listaErroresSemanticos, tablaSimbolos, ambitoIf(ambito))
            }
        }
        if(listaSentenciasElse.isNotEmpty()){
            for(e in listaSentenciasElse){
                e.llenarTablaSimbolos(listaErroresSemanticos, tablaSimbolos, ambitoElse(ambito))
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
                i.analizarSemantica(listaErroresSemanticos, tablaSimbolos, ambitoIf(ambito))
            }
        }
        if(listaSentenciasElse.isNotEmpty()){
            for(e in listaSentenciasElse){
                e.analizarSemantica(listaErroresSemanticos, tablaSimbolos, ambitoElse(ambito))
            }
        }
    }

    override fun completaNormalmente(): Boolean {
        if (listaSentenciasElse.isEmpty()) return true
        return CodigoJava.completaNormalmente(listaSentenciasIf) || CodigoJava.completaNormalmente(listaSentenciasElse)
    }

    override fun getCodeJava(): String {
        var codigo = "if ( " + exprLogica.getCodeJava() + " ) {\n\n"
        CodigoJava.abrirAmbito()
        codigo += CodigoJava.sentencias(listaSentenciasIf)
        CodigoJava.cerrarAmbito()
        codigo += "}\n"
        if (listaSentenciasElse.isNotEmpty()) {
            codigo += "else {\n\n"
            CodigoJava.abrirAmbito()
            codigo += CodigoJava.sentencias(listaSentenciasElse)
            CodigoJava.cerrarAmbito()
            codigo += "}\n"
        }
        return codigo + "\n"
    }


}
