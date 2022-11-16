package co.edu.uniquindio.compiladores.sintaxis

import co.edu.uniquindio.compiladores.lexico.Error
import co.edu.uniquindio.compiladores.lexico.Token
import co.edu.uniquindio.compiladores.semantico.TablaSimbolos
import javafx.scene.control.TreeItem
import java.util.ArrayList

class Retorno(var identificado:Token?, var expresion: Expresion?) : Sentencia() {

    override fun getArbolVisual(): TreeItem<String> {
        val raiz = TreeItem("Retorno")

        if(identificado != null) {
            raiz.children.add(TreeItem("${identificado!!.lexema}"))
        }

        if(expresion != null) {
            raiz.children.add( expresion!!.getArbolVisual() )
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

        var retornoFuncion = ambito.substring(ambito.indexOf(':') + 1,  ambito.indexOf('/', ambito.indexOf(':')))


        if(retornoFuncion != "vd") {
            if (expresion != null) {
                var tipoExpresion = expresion!!.obtenerTipo()
                if (retornoFuncion != tipoExpresion) {
                    listaErroresSemanticos.add(
                        Error(
                            "El tipo de dato de la expresion ($tipoExpresion) que desa retornar no es correcto en la funcion (${retornoFuncion})",
                            0,
                            0
                        )
                    )
                }
            }

            if (identificado != null) {
                var tipoIdentificador = tablaSimbolos.buscarSimboloValor(identificado!!.lexema, ambito)
                if (tipoIdentificador != null) {
                    if (retornoFuncion != tipoIdentificador.tipo) {
                        listaErroresSemanticos.add(
                            Error(
                                "El tipo de dato del identificador (${tipoIdentificador.tipo}) no coincide con el valor de retorno de la variable (${retornoFuncion})",
                                identificado!!.fila,
                                identificado!!.columna
                            )
                        )
                    }
                } else {
                    listaErroresSemanticos.add(
                        Error(
                            "La variable (${identificado!!.lexema}) que intenta retornar no se ha declarado",
                            identificado!!.fila,
                            identificado!!.columna
                        )
                    )
                }
            }
        }else if((retornoFuncion == "vd" && expresion != null) || (retornoFuncion == "vd" && identificado != null)){
            Error(
                "La funcion ($ambito) no debe retorna ningun valor",
                0,
                0
            )
        }

    }

}
