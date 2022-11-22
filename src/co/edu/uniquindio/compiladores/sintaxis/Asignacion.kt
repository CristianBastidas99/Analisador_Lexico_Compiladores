package co.edu.uniquindio.compiladores.sintaxis

import co.edu.uniquindio.compiladores.lexico.Error
import co.edu.uniquindio.compiladores.lexico.Token
import co.edu.uniquindio.compiladores.semantico.Simbolo
import co.edu.uniquindio.compiladores.semantico.TablaSimbolos
import javafx.scene.control.TreeItem
import java.util.ArrayList

class Asignacion(var identificador:Token, var operAsignacion:Token, var expresion:Expresion?, var identificador2:Token?, var sentencia: Sentencia?) :Sentencia() {

    override fun getArbolVisual(): TreeItem<String> {

        val raiz = TreeItem("Asignacion")

        raiz.children.add( TreeItem("${identificador.lexema}") )

        raiz.children.add( TreeItem("${operAsignacion.lexema}") )

        if(expresion != null){
            raiz.children.add( expresion!!.getArbolVisual() )
        }

        if(identificador2 != null){
            raiz.children.add( TreeItem("${identificador2!!.lexema}") )
        }

        if(sentencia != null){
            raiz.children.add( sentencia!!.getArbolVisual() )
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

        var simbolo: Simbolo? = null
        var posicionS = ambito.length
        var ambitoActS = ambito

        do {
            ambitoActS = ambitoActS.substring(0, posicionS)
            simbolo = tablaSimbolos.buscarSimboloValor(identificador.lexema, ambitoActS)

            if (simbolo != null) {
                if (expresion != null) {
                    var tipoExpresion = expresion!!.obtenerTipo()
                    if (simbolo.tipo != tipoExpresion) {
                        listaErroresSemanticos.add(
                            Error(
                                "El tipo de dato de la expresion ($tipoExpresion) no coincide con el tipo de la variable (${simbolo.tipo})",
                                identificador.fila,
                                identificador.columna
                            )
                        )
                    }
                }

                if (identificador2 != null) {
                    var tipoIdentificador2: Simbolo? = null
                    var posicion = ambito.length
                    var ambitoAct = ambito

                    do {
                        ambitoAct = ambitoAct.substring(0, posicion)
                        tipoIdentificador2 = tablaSimbolos.buscarSimboloValor(identificador2!!.lexema, ambitoAct)

                        if (tipoIdentificador2 != null) {
                            if (!simbolo.tipo.equals(tipoIdentificador2.tipo)) {
                                listaErroresSemanticos.add(
                                    Error(
                                        "El tipo de dato del identificador (${tipoIdentificador2.tipo}) no coincide con el tipo de la variable (${simbolo.tipo})",
                                        identificador.fila,
                                        identificador.columna
                                    )
                                )
                            }
                            posicion = -1
                        } else {
                            posicion = ambitoAct.lastIndexOf('/')
                        }

                    } while (posicion != -1)

                    if (tipoIdentificador2 == null) {
                        listaErroresSemanticos.add(
                            Error(
                                "La variable (${identificador2!!.lexema}) que intenta dar como asignacion no se ha declarado",
                                identificador.fila,
                                identificador.columna
                            )
                        )
                    }
                }

                if (sentencia != null) {
                    if (sentencia!!.javaClass.simpleName.equals("InvFuncion")) {
                        sentencia!!.analizarSemantica(
                            listaErroresSemanticos,
                            tablaSimbolos,
                            ambito + ".${simbolo.tipo}"
                        )
                    } else if (sentencia!!.javaClass.simpleName.equals("Lectura")) {
                        sentencia!!.analizarSemantica(listaErroresSemanticos, tablaSimbolos, simbolo.tipo!!)
                    }
                }
                posicionS = -1
            } else {
                posicionS = ambitoActS.lastIndexOf('/')
            }
        }while (posicionS != -1)

        if(simbolo == null){
            listaErroresSemanticos.add(
                Error(
                    "La variable (${identificador.lexema}) que intenta dar asignacion no se ha declarado en el ambito $ambito",
                    identificador.fila,
                    identificador.columna
                )
            )
        }
    }

    override fun getCodeJava(): String {
        var codigo = identificador.lexema.substring(1) + " "
        codigo += operAsignacion.lexema + " "

        if(expresion != null){
            codigo += expresion!!.getCodeJava() + ";\n"
        }
        if(identificador2 != null){
            codigo += identificador2!!.lexema.substring(1) + ";\n"
        }
        if(sentencia != null){
            codigo += sentencia!!.getCodeJava()
        }

        return codigo
    }


}
