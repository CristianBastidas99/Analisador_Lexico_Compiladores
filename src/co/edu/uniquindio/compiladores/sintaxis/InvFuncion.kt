package co.edu.uniquindio.compiladores.sintaxis

import co.edu.uniquindio.compiladores.lexico.Error
import co.edu.uniquindio.compiladores.lexico.Token
import co.edu.uniquindio.compiladores.semantico.TablaSimbolos
import javafx.scene.control.TreeItem

class InvFuncion (var nombre:Token, val listaParametrosSinTipo: ArrayList<ParametroSinTipo>) : Sentencia() {

    /** Tipo de la variable que recibe el resultado (si la invocacion es el valor de una asignacion); lo fija Asignacion */
    var tipoDestino: String? = null

    override fun getArbolVisual(): TreeItem<String> {
        val raiz = TreeItem("Invocación Función")

        raiz.children.add( TreeItem("${nombre.lexema}"))

        if(listaParametrosSinTipo.isNotEmpty()) {

            val parametrosSinTipo = TreeItem("Parámetros sin tipo")
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

        val listaTipoParametro = ArrayList<String>()
        var parametrosValidos = true
        for (p in listaParametrosSinTipo) {
            val simbolo = tablaSimbolos.buscarSimboloValorEnAmbitos(p.nombre.lexema, ambito)
            if (simbolo != null) {
                listaTipoParametro.add(requireNotNull(simbolo.tipo))
            } else {
                parametrosValidos = false
                listaErroresSemanticos.add(Error("La variable (${p.nombre.lexema}) que intenta dar como argumento no se ha declarado", p.nombre.fila, p.nombre.columna))
            }
        }

        // Si algun argumento no existe ya se reporto; buscar la funcion solo produciria un error derivado
        if (!parametrosValidos) {
            return
        }

        val funcion = tablaSimbolos.buscarSimboloFuncion(nombre.lexema, listaTipoParametro)

        if (funcion == null) {
            listaErroresSemanticos.add(Error("La función invocada no existe", nombre.fila, nombre.columna))
        } else if (tipoDestino != null && funcion.tipo != tipoDestino) {
            listaErroresSemanticos.add(Error("El tipo de la invocación de función (${funcion.tipo}) es diferente al de la variable ($tipoDestino)", nombre.fila, nombre.columna))
        }
    }

    override fun getCodeJava(): String {
        var codigo = CodigoJava.identificador(nombre.lexema) + "( "

        if(listaParametrosSinTipo.isNotEmpty()){
            for (p in listaParametrosSinTipo){
                codigo += p.getCodeJava() + ", "
            }
            codigo = codigo.substring(0, codigo.length - 2)
        }
        codigo += " );\n"
        return codigo
    }

}
