package co.edu.uniquindio.compiladores.sintaxis

import co.edu.uniquindio.compiladores.lexico.Error
import co.edu.uniquindio.compiladores.lexico.Token
import co.edu.uniquindio.compiladores.semantico.TablaSimbolos
import javafx.scene.control.TreeItem

class InvFuncion (var nombre:Token, val listaParametrosSinTipo: ArrayList<ParametroSinTipo>) : Sentencia() {

    override fun getArbolVisual(): TreeItem<String> {
        val raiz = TreeItem("Invocacion Funcion")

        raiz.children.add( TreeItem("${nombre.lexema}"))

        if(listaParametrosSinTipo.isNotEmpty()) {

            val parametrosSinTipo = TreeItem("Parametros sin tipo")
            raiz.children.add(parametrosSinTipo)
            for (parametrosinTipo in listaParametrosSinTipo) {
                parametrosSinTipo.children.add(parametrosinTipo.getArbolVisual())
            }
        }

        return raiz
    }

    override fun llenarTablaSimbolos(
        listaErroresSemanticos: java.util.ArrayList<Error>,
        tablaSimbolos: TablaSimbolos,
        ambito: String
    ) {

    }

    override fun analizarSemantica(listaErroresSemanticos: java.util.ArrayList<Error>, tablaSimbolos: TablaSimbolos, ambito: String) {

        var arreglo = ambito.split("\\.")
        var ambitoo  = ""
        var tipoAsignacion = ""

        if(arreglo.size > 1){
            ambitoo = arreglo.get(0)
            tipoAsignacion = arreglo.get(1)
        }

        if(tipoAsignacion.isNotEmpty()) {
            val listaTipoParametro: ArrayList<String> =
                obtenerTipoParametros(listaErroresSemanticos, tablaSimbolos, ambitoo)
            var funcion = tablaSimbolos.buscarSimboloFuncion(nombre.lexema, listaTipoParametro)

            if (funcion != null) {
                if(funcion.tipo != tipoAsignacion){
                    listaErroresSemanticos.add(Error("EL tipo de la invocacion de funcion (${funcion.tipo}) es diferente a la variable ($tipoAsignacion)", nombre.fila, nombre.columna))
                }
            }else{
                listaErroresSemanticos.add(Error("La funcion invocada no existe", nombre.fila, nombre.columna))
            }
        }else{
            val listaTipoParametro: ArrayList<String> =
                obtenerTipoParametros(listaErroresSemanticos, tablaSimbolos, ambito)
            var funcion = tablaSimbolos.buscarSimboloFuncion(nombre.lexema, listaTipoParametro)

            if (funcion == null) {
                listaErroresSemanticos.add(Error("La funcion invocada no existe", nombre.fila, nombre.columna))
            }
        }
    }

    override fun getCodeJava(): String {
        var codigo = nombre.lexema.substring(1) + "( "

        if(listaParametrosSinTipo.isNotEmpty()){
            for (p in listaParametrosSinTipo){
                codigo += p.getCodeJava() + ", "
            }
            codigo = codigo.substring(0, codigo.length - 2)
        }
        codigo += " );\n"
        return codigo
    }

    fun obtenerTipoParametros(listaErroresSemanticos: java.util.ArrayList<Error>, tablaSimbolos: TablaSimbolos, ambito: String):ArrayList<String> {
        val listaTipoParametro : ArrayList<String> = ArrayList()
        if(listaParametrosSinTipo.isNotEmpty()){
            for(p in listaParametrosSinTipo){
                var simbolo = tablaSimbolos.buscarSimboloValor(p.nombre.lexema, ambito)
                if(simbolo != null) {
                    listaTipoParametro.add(simbolo.tipo!!)
                }else{
                    listaErroresSemanticos.add(Error("La variable (${p.nombre!!.lexema}) que intenta dar como asignacion no se ha declarado", p.nombre.fila, p.nombre.columna))
                }
            }
        }
        return listaTipoParametro
    }

}
