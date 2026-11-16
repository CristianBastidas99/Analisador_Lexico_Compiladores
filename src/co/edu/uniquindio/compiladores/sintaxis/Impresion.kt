package co.edu.uniquindio.compiladores.sintaxis

import co.edu.uniquindio.compiladores.lexico.Error
import co.edu.uniquindio.compiladores.lexico.Token
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
            val simbolo = tablaSimbolos.buscarSimboloValorEnAmbitos(identificador!!.lexema, ambito)

            if (simbolo == null) {
                listaErroresSemanticos.add(
                    Error(
                        "La variable (${identificador!!.lexema}) que intenta imprimir no se ha declarado",
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
            codigo += CodigoJava.usar(identificador!!.lexema)
        }

        codigo += ");\n"
        return codigo
    }

}
