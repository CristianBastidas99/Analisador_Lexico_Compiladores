package co.edu.uniquindio.compiladores.sintaxis

import co.edu.uniquindio.compiladores.lexico.Error
import co.edu.uniquindio.compiladores.lexico.Token
import co.edu.uniquindio.compiladores.semantico.TablaSimbolos
import javafx.scene.control.TreeItem
import java.util.ArrayList

class IncrDecr (var identificador:Token, var tipo:Token) : Sentencia() {

    override fun getArbolVisual(): TreeItem<String> {
        val raiz = TreeItem("Incremento/Decremento")

        raiz.children.add( TreeItem("${identificador.lexema}"))

        raiz.children.add( TreeItem("${tipo.lexema}"))

        return raiz
    }

    override fun llenarTablaSimbolos(
        listaErroresSemanticos: ArrayList<Error>,
        tablaSimbolos: TablaSimbolos,
        ambito: String
    ) {

    }

    override fun analizarSemantica(listaErroresSemanticos: ArrayList<Error>, tablaSimbolos: TablaSimbolos, ambito: String) {

        var simbolo = tablaSimbolos.buscarSimboloValor(identificador.lexema, ambito)

        if (simbolo != null) {
            if(simbolo.tipo != "ent"){
                listaErroresSemanticos.add(Error("El tipo de dato del identificador (${identificador.lexema}) no coincide con el tipo admitido (ent)", identificador.fila, identificador.columna))
            }
        }else{
            listaErroresSemanticos.add(
                Error(
                    "La variable que intenta imprimir no se ha declarado",
                    identificador.fila,
                    identificador.columna
                )
            )
        }
    }

}
