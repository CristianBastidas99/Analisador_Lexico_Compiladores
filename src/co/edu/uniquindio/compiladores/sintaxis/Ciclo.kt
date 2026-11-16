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

    /** Ambito unico y determinista por ciclo: se basa en la posicion del operador relacional de la condicion */
    private fun ambitoWhile(ambito: String): String {
        val t = expresionLogica.exprRelacional.opeRelacional
        return "$ambito/while@${t.fila}:${t.columna}"
    }

    override fun llenarTablaSimbolos(listaErroresSemanticos: java.util.ArrayList<Error>, tablaSimbolos: TablaSimbolos, ambito: String ) {
        if(listaSentencias.isNotEmpty()){
            for(s in listaSentencias){
                s.llenarTablaSimbolos(listaErroresSemanticos, tablaSimbolos, ambitoWhile(ambito))
            }
        }
    }

    override fun analizarSemantica(listaErroresSemanticos: java.util.ArrayList<Error>, tablaSimbolos: TablaSimbolos, ambito: String) {
        if(listaSentencias.isNotEmpty()){
            for(s in listaSentencias){
                s.analizarSemantica(listaErroresSemanticos, tablaSimbolos, ambitoWhile(ambito))
            }
        }
    }

    override fun getCodeJava(): String {
        // Las condiciones del lenguaje solo tienen literales (expresiones constantes); Boolean.valueOf evita que Java
        // las trate como constantes y rechace el cuerpo (while(false)) o el codigo posterior (while(true)) como inalcanzable
        var codigo = "while ( Boolean.valueOf(" + expresionLogica.getCodeJava() + ") ) {\n\n"
        CodigoJava.abrirAmbito()
        codigo += CodigoJava.sentencias(listaSentencias)
        CodigoJava.cerrarAmbito()
        codigo += "}\n\n"
        return codigo
    }

}
