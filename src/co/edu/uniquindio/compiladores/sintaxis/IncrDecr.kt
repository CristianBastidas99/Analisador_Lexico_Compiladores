package co.edu.uniquindio.compiladores.sintaxis

import co.edu.uniquindio.compiladores.lexico.Error
import co.edu.uniquindio.compiladores.lexico.Token
import co.edu.uniquindio.compiladores.semantico.Simbolo
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

        var simbolo: Simbolo? = null
        var posicionS = ambito.length
        var ambitoActS = ambito

        do {
            if (simbolo != null) {
                if (!simbolo.tipo.equals("ent")) {
                    listaErroresSemanticos.add(
                        Error(
                            "El tipo de dato del identificador (${identificador.lexema}) no coincide con el tipo admitido (ent)",
                            identificador.fila,
                            identificador.columna
                        )
                    )
                }
                posicionS = -1
            } else {
                posicionS = ambitoActS.lastIndexOf('/')
            }
        }while (posicionS != -1)

        if (simbolo == null) {
            listaErroresSemanticos.add(
                Error(
                    "La variable que intenta imprimir no se ha declarado",
                    identificador.fila,
                    identificador.columna
                )
            )
        }
    }

    override fun getCodeJava(): String {
        return identificador.lexema.substring(1) + tipo.lexema + ";\n"
    }

}
