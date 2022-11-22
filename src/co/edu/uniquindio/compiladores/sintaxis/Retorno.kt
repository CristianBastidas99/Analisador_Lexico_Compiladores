package co.edu.uniquindio.compiladores.sintaxis

import co.edu.uniquindio.compiladores.lexico.Error
import co.edu.uniquindio.compiladores.lexico.Token
import co.edu.uniquindio.compiladores.semantico.Simbolo
import co.edu.uniquindio.compiladores.semantico.TablaSimbolos
import javafx.scene.control.TreeItem
import java.util.ArrayList

class Retorno(var identificador:Token?, var expresion: Expresion?, var fila:Int, var columna:Int) : Sentencia() {

    override fun getArbolVisual(): TreeItem<String> {
        val raiz = TreeItem("Retorno")

        if(identificador != null) {
            raiz.children.add(TreeItem("${identificador!!.lexema}"))
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

        var derecha = ambito.indexOf('/', ambito.indexOf(':'))
        if(derecha == -1){
            derecha = ambito.length
        }
        var retornoFuncion = ambito.substring(ambito.indexOf(':') + 1, derecha )


            if (retornoFuncion != "vd") {
                if (expresion != null) {
                    var tipoExpresion = expresion!!.obtenerTipo()
                    if (retornoFuncion != tipoExpresion) {
                        listaErroresSemanticos.add(
                            Error(
                                "El tipo de dato de la expresion ($tipoExpresion) que desa retornar no es correcto en la funcion (${retornoFuncion})",
                                fila,
                                columna
                            )
                        )
                    }
                }

                if (identificador != null) {
                    var tipoIdentificador: Simbolo? = null
                    var posicion = ambito.length
                    var ambitoAct = ambito

                    do {
                        ambitoAct = ambitoAct.substring(0, posicion)
                        tipoIdentificador = tablaSimbolos.buscarSimboloValor(identificador!!.lexema, ambitoAct)

                        if (tipoIdentificador != null) {
                            if (!retornoFuncion.equals(tipoIdentificador.tipo)) {
                                listaErroresSemanticos.add(
                                    Error(
                                        "El tipo de dato del identificador (${tipoIdentificador.tipo}) no coincide con el valor de retorno de la variable (${retornoFuncion})",
                                        identificador!!.fila,
                                        identificador!!.columna
                                    )
                                )
                            }
                            posicion = -1
                        } else {
                            posicion = ambitoAct.lastIndexOf('/')
                        }
                    }while (posicion != -1)

                    if(tipoIdentificador == null){
                        listaErroresSemanticos.add(
                            Error(
                                "La variable (${identificador!!.lexema}) que intenta retornar no se ha declarado",
                                identificador!!.fila,
                                identificador!!.columna
                            )
                        )
                    }
                }
            } else if ((retornoFuncion == "vd" && expresion != null) || (retornoFuncion == "vd" && identificador != null)) {
                Error(
                    "La funcion ($ambito) no debe retorna ningun valor",
                    fila,
                    columna
                )
            }
    }

    override fun getCodeJava(): String {
        var codigo = "return "

        if(identificador != null) {
            codigo += identificador!!.lexema.substring(1)
        }

        if(expresion != null) {
            codigo += expresion!!.getCodeJava()
        }

        codigo += ";\n"
        return codigo
    }

}
