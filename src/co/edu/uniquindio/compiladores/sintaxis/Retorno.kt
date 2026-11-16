package co.edu.uniquindio.compiladores.sintaxis

import co.edu.uniquindio.compiladores.lexico.Error
import co.edu.uniquindio.compiladores.lexico.Token
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
                                "El tipo de dato de la expresión ($tipoExpresion) que desea retornar no es correcto en la función (${retornoFuncion})",
                                fila,
                                columna
                            )
                        )
                    }
                }

                if (identificador != null) {
                    val tipoIdentificador = tablaSimbolos.buscarSimboloValorEnAmbitos(identificador!!.lexema, ambito)

                    if (tipoIdentificador != null) {
                        if (retornoFuncion != tipoIdentificador.tipo) {
                            listaErroresSemanticos.add(
                                Error(
                                    "El tipo de dato del identificador (${tipoIdentificador.tipo}) no coincide con el valor de retorno de la función (${retornoFuncion})",
                                    identificador!!.fila,
                                    identificador!!.columna
                                )
                            )
                        }
                    } else {
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
                listaErroresSemanticos.add(
                    Error(
                        "La función ($ambito) no debe retornar ningún valor",
                        fila,
                        columna
                    )
                )
            }
    }

    override fun completaNormalmente(): Boolean = false

    override fun getCodeJava(): String {
        var codigo = "return "

        if(identificador != null) {
            codigo += CodigoJava.usar(identificador!!.lexema)
        }

        if(expresion != null) {
            codigo += expresion!!.getCodeJava()
        }

        codigo += ";\n"
        return codigo
    }

}
