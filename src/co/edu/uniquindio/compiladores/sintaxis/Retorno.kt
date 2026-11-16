package co.edu.uniquindio.compiladores.sintaxis

import co.edu.uniquindio.compiladores.lexico.Error
import co.edu.uniquindio.compiladores.lexico.Token
import co.edu.uniquindio.compiladores.semantico.TablaSimbolos
import javafx.scene.control.TreeItem
import java.util.ArrayList

class Retorno(var identificador:Token?, var expresion: Expresion?, var fila:Int, var columna:Int) : Sentencia() {

    override fun getArbolVisual(): TreeItem<String> {
        val raiz = TreeItem("Retorno")

        identificador?.let {
            raiz.children.add(TreeItem("${it.lexema}"))
        }

        expresion?.let {
            raiz.children.add( it.getArbolVisual() )
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
        val retornoFuncion = ambito.substring(ambito.indexOf(':') + 1, derecha )


            if (retornoFuncion != "vd") {
                expresion?.let { expr ->
                    val tipoExpresion = expr.obtenerTipo()
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

                identificador?.let { id ->
                    val tipoIdentificador = tablaSimbolos.buscarSimboloValorEnAmbitos(id.lexema, ambito)

                    if (tipoIdentificador != null) {
                        if (retornoFuncion != tipoIdentificador.tipo) {
                            listaErroresSemanticos.add(
                                Error(
                                    "El tipo de dato del identificador (${tipoIdentificador.tipo}) no coincide con el valor de retorno de la función (${retornoFuncion})",
                                    id.fila,
                                    id.columna
                                )
                            )
                        }
                    } else {
                        listaErroresSemanticos.add(
                            Error(
                                "La variable (${id.lexema}) que intenta retornar no se ha declarado",
                                id.fila,
                                id.columna
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

        identificador?.let {
            codigo += CodigoJava.usar(it.lexema)
        }

        expresion?.let {
            codigo += it.getCodeJava()
        }

        codigo += ";\n"
        return codigo
    }

}
