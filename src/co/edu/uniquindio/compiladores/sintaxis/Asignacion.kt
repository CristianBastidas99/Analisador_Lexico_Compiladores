package co.edu.uniquindio.compiladores.sintaxis

import co.edu.uniquindio.compiladores.lexico.Error
import co.edu.uniquindio.compiladores.lexico.Token
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
        TODO("Not yet implemented")
    }

    override fun analizarSemantica(listaErroresSemanticos: ArrayList<Error>, tablaSimbolos: TablaSimbolos, ambito: String) {

        var simbolo = tablaSimbolos.buscarSimboloValor(identificador.lexema, ambito)

        if(simbolo != null) {
            if (expresion != null) {
                var tipoExpresion = expresion!!.obtenerTipo()
                if(simbolo.tipo != tipoExpresion){
                    listaErroresSemanticos.add(Error("El tipo de dato de la expresion ($tipoExpresion) no coincide con el tipo de la variable (${simbolo.tipo})", identificador.fila, identificador.columna))
                }
            }

            if (identificador2 != null) {
                var tipoIdentificador2 = tablaSimbolos.buscarSimboloValor(identificador2!!.lexema, ambito)
                if (tipoIdentificador2 != null) {
                    if(simbolo.tipo != tipoIdentificador2.tipo){
                        listaErroresSemanticos.add(Error("El tipo de dato del identificador (${tipoIdentificador2.tipo}) no coincide con el tipo de la variable (${simbolo.tipo})", identificador.fila, identificador.columna))
                    }
                }else{
                    listaErroresSemanticos.add(Error("La variable (${identificador2!!.lexema}) que intenta dar como asignacion no se ha declarado", identificador.fila, identificador.columna))
                }
            }

            if (sentencia != null) {
                if(sentencia!!.javaClass.simpleName.equals("InvFuncion")){
                    sentencia!!.analizarSemantica(listaErroresSemanticos, tablaSimbolos, ambito + ".${simbolo.tipo}")
                }else if(sentencia!!.javaClass.simpleName.equals("Lectura")){
                    sentencia!!.analizarSemantica(listaErroresSemanticos, tablaSimbolos, simbolo.tipo!!)
                }
            }
        }else{
            listaErroresSemanticos.add(Error("La variable que intenta dar asignacion no se ha declarado", identificador.fila, identificador.columna))
        }
    }


}
