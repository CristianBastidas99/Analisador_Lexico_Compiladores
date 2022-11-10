package co.edu.uniquindio.compiladores.semantico

import co.edu.uniquindio.compiladores.lexico.Error
import co.edu.uniquindio.compiladores.sintaxis.UnidadDeCompilacion

class AnalizadorSemantico(var uc: UnidadDeCompilacion) {

    var listaErroresSemanticos: ArrayList<Error> = ArrayList()
    var tablaSimbolos: TablaSimbolos = TablaSimbolos(listaErroresSemanticos)

    fun llenarTablaSimbolos(){
        uc.llenarTablaSimbolos(listaErroresSemanticos, tablaSimbolos)
    }

    fun analizarSemantica(){
        uc.analizarSemantica(listaErroresSemanticos, tablaSimbolos)
    }

}