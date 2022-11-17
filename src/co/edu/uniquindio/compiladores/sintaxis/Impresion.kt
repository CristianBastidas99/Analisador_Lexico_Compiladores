package co.edu.uniquindio.compiladores.sintaxis

import co.edu.uniquindio.compiladores.lexico.Error
import co.edu.uniquindio.compiladores.lexico.Token
import co.edu.uniquindio.compiladores.semantico.Simbolo
import co.edu.uniquindio.compiladores.semantico.TablaSimbolos
import javafx.scene.control.TreeItem
import java.util.ArrayList

class Impresion(var expresion: Expresion?, var identificador: Token?) : Sentencia() {
    override fun getArbolVisual(): TreeItem<String> {
        val raiz = TreeItem("Impresion")

        if(expresion != null){
            raiz.children.add( expresion!!.getArbolVisual() )
        }

        if(identificador != null){
            raiz.children.add( TreeItem("${identificador!!.lexema}"))
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

        if (identificador != null) {
            var simbolo: Simbolo? = null
            var posicionS = ambito.length
            var ambitoActS = ambito

            do {

                ambitoActS = ambitoActS.substring(0, posicionS)
                simbolo = tablaSimbolos.buscarSimboloValor(identificador!!.lexema, ambitoActS)

                if (simbolo != null) {
                    posicionS = -1
                } else {
                    posicionS = ambitoActS.lastIndexOf('/')
                }

            } while (posicionS != -1)

            if (simbolo == null) {
                listaErroresSemanticos.add(
                    Error(
                        "La variable (${identificador!!.lexema}) que intenta imprimir no se a declarado",
                        identificador!!.fila,
                        identificador!!.columna
                    )
                )
            }
        }
    }

    override fun getCodeJava(): String {
        var codigo = "System.out.print("

        if(expresion != null){
            codigo += expresion!!.getCodeJava()
        }

        if(identificador != null){
            codigo += identificador!!.lexema.substring(1)
        }

        codigo += ");\n"
        return codigo
    }

}
