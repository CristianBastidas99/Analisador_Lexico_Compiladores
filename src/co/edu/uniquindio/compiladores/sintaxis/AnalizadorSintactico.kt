package co.edu.uniquindio.compiladores.sintaxis

import co.edu.uniquindio.compiladores.lexico.Categoria
import co.edu.uniquindio.compiladores.lexico.Token

class AnalizadorSintactico(var listaTokens:ArrayList<Token>) {

    var posicionActual = 0
    var tokenActual = listaTokens[posicionActual]
    var listaErrores = ArrayList<Error>()

    fun obtenerSiguienteToken(){

        posicionActual++

        if(posicionActual < listaTokens.size){
            tokenActual = listaTokens[posicionActual]
        }
    }

    fun reportarError(mensaje:String){

    }

    /*
    <Unidad de Compilación> ::= <Visibilidad> Class Identificador “{” [ <Lista de Variables Globales>] [ <Lista de Funciones> ] “}”
     */
    fun esUnidadDeCompilacion(): UnidadDeCompilacion?{

        var visibilidad = esVisibilidad()

        if(visibilidad != null){
            obtenerSiguienteToken()

            if(tokenActual.categoria == Categoria.PALABRA_RESERVADA && tokenActual.lexema == "clss"){
                obtenerSiguienteToken()

                if(tokenActual.categoria == Categoria.IDENTIFICADOR){
                    var nombre = tokenActual
                    obtenerSiguienteToken()

                    if(tokenActual.categoria == Categoria.LLAVE_IZQUIERDO){
                        obtenerSiguienteToken()
                        val listaVariablesGlobales:ArrayList<VariableGlobal> = esListaVariablesGlobales()
                        val listaFunciones:ArrayList<Funcion> = esListaFunciones()

                        if(tokenActual.categoria == Categoria.LLAVE_DERECHO){
                            return UnidadDeCompilacion(visibilidad, nombre, listaVariablesGlobales, listaFunciones)
                        }else{
                            reportarError("Falta la llave derecha")
                        }
                    }else{
                        reportarError("Falta la llave izquierda")
                    }
                }else{
                    reportarError("Falta el identificador de la funcion")
                }
            }else{
                reportarError("Falta palabra reservada cls")
            }
        }else{
            reportarError("Debe definir la visibilidad de la clase")
        }
        return null
    }

    /*
    <Visibilidad> ::= pub | pvt
     */
    fun esVisibilidad():Token?{

        if(tokenActual.categoria == Categoria.PALABRA_RESERVADA ){
            if(tokenActual.lexema == "pub" || tokenActual.lexema == "pvt"){
                return tokenActual
            }
        }

        return null
    }

    /*
    <Lista de Variables Globales> ::= <Variable Global> [<Lista de Variables Globales>]
     */
    fun esListaVariablesGlobales():ArrayList<VariableGlobal>{
        var lista:ArrayList<VariableGlobal> =  ArrayList<VariableGlobal>()
        var vg: VariableGlobal? = esVariableGlobal()

        while (vg != null){
            lista.add(vg)
            vg = esVariableGlobal()
        }

        return lista
    }

    /*
    <Variable Global> ::= <Visibilidad> <Tipo> Identificador “|
     */
    private fun esVariableGlobal(): VariableGlobal? {
        var visibilidad = esVisibilidad()

        if(visibilidad != null) {
            obtenerSiguienteToken()
            var tipo = esTipo()

            if(tipo != null){
                obtenerSiguienteToken()

                if(tokenActual.categoria == Categoria.IDENTIFICADOR){
                    var nombre = tokenActual
                    obtenerSiguienteToken()

                    if(tokenActual.categoria == Categoria.FIN_SENTENCIA){

                        return VariableGlobal(visibilidad, tipo, nombre)
                    }
                }
            }
        }
        return null
    }

    /*
    <Tipo> ::= ent | dbe| str| booleano | Caracter
     */
    private fun esTipo(): Token? {

        if(tokenActual.categoria == Categoria.PALABRA_RESERVADA ){
            if(tokenActual.lexema == "ent" || tokenActual.lexema == "dbe" || tokenActual.lexema == "srt"){
                return tokenActual
            }
        }

        return null
    }

    /*
    <Lista de Funciones> ::= <Función> [<Lista de Funciones>]
     */
    fun esListaFunciones():ArrayList<Funcion>{

        var lista:ArrayList<Funcion> =  ArrayList<Funcion>()
        var f: Funcion? = esFuncion()

        while (f != null){
            lista.add(f)
            f = esFuncion()
        }

        return lista
    }

    /*
    <Función> ::= <Visibilidad> <Valor Retornado> Identificador “(” [ <Lista de Parámetros> ] “)” “{” <Lista de Sentencias> “}”
     */
    private fun esFuncion(): Funcion? {
        return null
    }

}
