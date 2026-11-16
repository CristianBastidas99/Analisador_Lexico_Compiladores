package co.edu.uniquindio.compiladores.sintaxis

import co.edu.uniquindio.compiladores.lexico.Error
import co.edu.uniquindio.compiladores.lexico.Token
import co.edu.uniquindio.compiladores.semantico.TablaSimbolos
import javafx.scene.control.TreeItem
import java.util.ArrayList

class Asignacion(var identificador:Token, var operAsignacion:Token, var expresion:Expresion?, var identificador2:Token?, var sentencia: Sentencia?) :Sentencia() {

    /** true cuando es la asignacion inicial de una declaracion de variable inmutable (no cuenta como modificacion) */
    var esInicializacion: Boolean = false

    override fun getArbolVisual(): TreeItem<String> {

        val raiz = TreeItem("Asignación")

        raiz.children.add( TreeItem("${identificador.lexema}") )

        raiz.children.add( TreeItem("${operAsignacion.lexema}") )

        expresion?.let {
            raiz.children.add( it.getArbolVisual() )
        }

        identificador2?.let {
            raiz.children.add( TreeItem("${it.lexema}") )
        }

        sentencia?.let {
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

        // Se limpian los destinos de la ejecucion anterior (el analisis puede repetirse)
        val s = sentencia
        if (s is InvFuncion) s.tipoDestino = null
        if (s is Lectura) { s.tipoDestino = null; s.nombreDestino = null }

        val simbolo = tablaSimbolos.buscarSimboloValorEnAmbitos(identificador.lexema, ambito)

        if (simbolo == null) {
            listaErroresSemanticos.add(
                Error(
                    "La variable (${identificador.lexema}) que intenta dar asignación no se ha declarado en el ámbito $ambito",
                    identificador.fila,
                    identificador.columna
                )
            )
            return
        }

        if (simbolo.modificable == false && !esInicializacion) {
            listaErroresSemanticos.add(
                Error(
                    "La variable (${identificador.lexema}) es inmutable y no se puede modificar",
                    identificador.fila,
                    identificador.columna
                )
            )
        }

        expresion?.let { expr ->
            val tipoExpresion = expr.obtenerTipo()
            if (simbolo.tipo != tipoExpresion) {
                listaErroresSemanticos.add(
                    Error(
                        "El tipo de dato de la expresión ($tipoExpresion) no coincide con el tipo de la variable (${simbolo.tipo})",
                        identificador.fila,
                        identificador.columna
                    )
                )
            }
        }

        identificador2?.let { id2 ->
            val simbolo2 = tablaSimbolos.buscarSimboloValorEnAmbitos(id2.lexema, ambito)

            if (simbolo2 != null) {
                if (simbolo.tipo != simbolo2.tipo) {
                    listaErroresSemanticos.add(
                        Error(
                            "El tipo de dato del identificador (${simbolo2.tipo}) no coincide con el tipo de la variable (${simbolo.tipo})",
                            identificador.fila,
                            identificador.columna
                        )
                    )
                }
            } else {
                listaErroresSemanticos.add(
                    Error(
                        "La variable (${id2.lexema}) que intenta dar como asignación no se ha declarado",
                        identificador.fila,
                        identificador.columna
                    )
                )
            }
        }

        if (s is InvFuncion) {
            s.tipoDestino = simbolo.tipo
            s.analizarSemantica(listaErroresSemanticos, tablaSimbolos, ambito)
        } else if (s is Lectura) {
            s.tipoDestino = simbolo.tipo
            s.nombreDestino = identificador.lexema
            s.posicionDestino = identificador
            s.analizarSemantica(listaErroresSemanticos, tablaSimbolos, ambito)
        }
    }

    override fun getCodeJava(): String {
        val izquierda = CodigoJava.usar(identificador.lexema)
        return izquierda + " " + operAsignacion.lexema + " " + getCodeJavaDerecha()
    }

    /** Lado derecho de la asignacion (con el ; final); permite generarlo antes de declarar la variable destino */
    fun getCodeJavaDerecha(): String {
        var codigo = ""

        expresion?.let {
            codigo += it.getCodeJava() + ";\n"
        }
        identificador2?.let {
            codigo += CodigoJava.usar(it.lexema) + ";\n"
        }
        sentencia?.let {
            codigo += it.getCodeJava()
        }

        return codigo
    }


}
