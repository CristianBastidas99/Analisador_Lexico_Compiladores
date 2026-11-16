package co.edu.uniquindio.compiladores.sintaxis


import co.edu.uniquindio.compiladores.lexico.Error
import co.edu.uniquindio.compiladores.lexico.Token
import co.edu.uniquindio.compiladores.semantico.TablaSimbolos
import javafx.scene.control.TreeItem

class Funcion(var visibilidad:Token, var valorRetornado:Token, var nombre:Token, val listaParametrosConTipo:ArrayList<ParametroConTipo>, val listaSentencias:ArrayList<Sentencia>) {

    fun getArbolVisual(): TreeItem<String>? {

        val raiz = TreeItem("Funcion")

        raiz.children.add( TreeItem("${visibilidad.lexema}"))

        raiz.children.add( TreeItem("${valorRetornado.lexema}"))

        raiz.children.add( TreeItem("${nombre.lexema}"))

        if(listaParametrosConTipo.isNotEmpty()) {

            val parametrosConTipo = TreeItem("Parametros con tipo")
            raiz.children.add(parametrosConTipo)
            for (parametroConTipo in listaParametrosConTipo) {
                parametrosConTipo.children.add(parametroConTipo.getArbolVisual())
            }
        }

        if(listaSentencias.isNotEmpty()) {

            val sentencias = TreeItem("Sentencias")
            raiz.children.add(sentencias)
            for (sentencia in listaSentencias) {
                sentencias.children.add(sentencia.getArbolVisual())
            }
        }

        return raiz
    }

    override fun toString(): String {
        return "Funcion(visibilidad=${visibilidad.lexema}, valorRetornado=${valorRetornado.lexema}, nombre=${nombre.lexema}, listaParametros=$listaParametrosConTipo, listaSentencias=$listaSentencias)"
    }

    fun llenarTablaSimbolos(listaErroresSemanticos: java.util.ArrayList<Error>, tablaSimbolos: TablaSimbolos, ambito: String) {
        val listaTipoParametro : ArrayList<String> = obtenerTipoParametros()
        tablaSimbolos.guardasSimboloFuncion(nombre.lexema, valorRetornado.lexema, listaTipoParametro, ambito, visibilidad.lexema, visibilidad.fila, visibilidad.columna)

        if(listaParametrosConTipo.isNotEmpty()){
            for(p in listaParametrosConTipo){
                tablaSimbolos.guardasSimboloValor(p.nombre.lexema, p.tipo.lexema, true, ambito + "/" + nombre.lexema + ":" + valorRetornado.lexema, "pvt", p.nombre.fila, p.nombre.columna)
            }
        }

        if(listaSentencias.isNotEmpty()){
            for(s in listaSentencias){
                s.llenarTablaSimbolos(listaErroresSemanticos, tablaSimbolos, ambito + "/" + nombre.lexema + ":" + valorRetornado.lexema)
            }
        }

    }

    fun analizarSemantica(listaErroresSemanticos: java.util.ArrayList<Error>, tablaSimbolos: TablaSimbolos, ambito: String) {
        if(listaSentencias.isNotEmpty()){
            for(s in listaSentencias){
                s.analizarSemantica(listaErroresSemanticos, tablaSimbolos, ambito + "/" + nombre.lexema + ":" + valorRetornado.lexema)
            }
        }
    }

    fun obtenerTipoParametros():ArrayList<String> {
        val listaTipoParametro : ArrayList<String> = ArrayList()
        if(listaParametrosConTipo.isNotEmpty()){
            for(p in listaParametrosConTipo){
                listaTipoParametro.add(p.tipo.lexema)
            }
        }
        return listaTipoParametro
    }

    fun getCodeJava(): String {
        val esMain = nombre.lexema.substring(1) == "main" && obtenerValorRetornado() == "void"
        CodigoJava.reiniciarAmbitos()
        CodigoJava.abrirAmbito()

        // La funcion main debe ser public static void main(String[] args) para poder ejecutarse con java
        var codigo = (if (esMain) "public" else obtenerVisibilidad()) + " static "
        codigo += obtenerValorRetornado() + " "
        codigo += CodigoJava.identificador(nombre.lexema) + " ( "
        if (esMain) {
            codigo += "String[] args"
        } else {
            codigo += listaParametrosConTipo.joinToString(", ") { it.getCodeJava() }
        }
        codigo += " ){\n\n"
        codigo += CodigoJava.sentencias(listaSentencias)
        // Java exige que toda funcion con tipo retorne en todos los caminos
        if (obtenerValorRetornado() != "void" && CodigoJava.completaNormalmente(listaSentencias)) {
            codigo += "return " + CodigoJava.valorPorDefecto(valorRetornado.lexema) + ";\n"
        }
        codigo += "}\n\n"
        CodigoJava.cerrarAmbito()
        return codigo
    }

    fun obtenerVisibilidad(): String{
        var stringVisibilidad = "public"
        if(visibilidad.lexema.equals("pvt")){
            stringVisibilidad = "private"
        }
        return stringVisibilidad
    }

    fun obtenerStringTipo(): String{
        var stringTipo = "int"
        if(valorRetornado.lexema.equals("dbe")){
            stringTipo = "double"
        }else if(valorRetornado.lexema.equals("str")){
            stringTipo = "String"
        }else if(valorRetornado.lexema.equals("bln")){
            stringTipo = "boolean"
        }
        else if(valorRetornado.lexema.equals("crt")){
            stringTipo = "char"
        }
        return stringTipo
    }

    fun obtenerValorRetornado(): String{
        var stringValorRetornado = "void"
        if(!valorRetornado.lexema.equals("vd")){
            stringValorRetornado = obtenerStringTipo()
        }
        return stringValorRetornado
    }


}
