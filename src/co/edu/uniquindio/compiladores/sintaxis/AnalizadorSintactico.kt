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

    fun restablecerToken(posicion:Int){

        if(posicion < listaTokens.size){
            posicionActual = posicion
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

            if(tokenActual.categoria == Categoria.PALABRA_RESERVADA && tokenActual.lexema == "cls"){
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

        var posicionInicial = posicionActual

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
                        obtenerSiguienteToken()
                        return VariableGlobal(visibilidad, tipo, nombre)
                    }
                }
            }
            restablecerToken(posicionInicial)
        }
        return null
    }

    /*
    <Tipo> ::= ent | dbe| str| booleano | Caracter
     */
    private fun esTipo(): Token? {

        if(tokenActual.categoria == Categoria.PALABRA_RESERVADA ){
            if(tokenActual.lexema == "ent" || tokenActual.lexema == "dbe" || tokenActual.lexema == "str"
                || tokenActual.lexema == "bln" || tokenActual.lexema == "crt"){
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

        var posicionInicial = posicionActual

        var visibilidad = esVisibilidad()

        if(visibilidad != null){
            obtenerSiguienteToken()
            var valorRetornado = esValorRetornado()

            if(valorRetornado != null){
                obtenerSiguienteToken()

                if(tokenActual.categoria == Categoria.IDENTIFICADOR) {
                    var nombre = tokenActual
                    obtenerSiguienteToken()

                    if(tokenActual.categoria == Categoria.PARENTESIS_IZQUIERDO) {
                        obtenerSiguienteToken()
                        val listaParametros:ArrayList<Parametro> = esListaParametros()

                        if(tokenActual.categoria == Categoria.PARENTESIS_DERECHO) {
                            obtenerSiguienteToken()

                            if(tokenActual.categoria == Categoria.LLAVE_IZQUIERDO) {
                                obtenerSiguienteToken()
                                val listaSentencias:ArrayList<Sentencia> = esListaSentencias()

                                if(tokenActual.categoria == Categoria.LLAVE_DERECHO) {
                                    obtenerSiguienteToken()
                                    return Funcion(visibilidad, valorRetornado, nombre, listaParametros, listaSentencias)
                                }
                            }
                        }
                    }
                }
            }
            restablecerToken(posicionInicial)
        }
        return null
    }

    /*
    <Valor Retornado> ::= Void | <Tipo>
     */
    private fun esValorRetornado(): Token? {

        if(tokenActual.categoria == Categoria.PALABRA_RESERVADA ){
            if(tokenActual.lexema == "vd" || esTipo() != null){
                return tokenActual
            }
        }

        return null
    }

    /**
     * <Lista de Parámetros> ::= <Parámetro> [“:” <Lista de Parámetros>]
     */
    private fun esListaParametros(): ArrayList<Parametro> {

        var lista:ArrayList<Parametro> =  ArrayList<Parametro>()
        var p: Parametro? = esParametro()

        while (p != null){
            lista.add(p)
            if(tokenActual.categoria == Categoria.SEPARADOR ){
                obtenerSiguienteToken()
                p = esParametro()
            }else{
                p = null
            }
        }

        return lista

    }

    /**
    *<Parámetro> ::= [<Tipo>] Identificador
    */
    private fun esParametro(): Parametro? {

        var posicionInicial = posicionActual
        var tipo = esTipo()

        if(tipo != null){
            obtenerSiguienteToken()
        }

        if(tokenActual.categoria == Categoria.IDENTIFICADOR) {
            var nombre = tokenActual
            obtenerSiguienteToken()
            return Parametro(tipo, nombre)
        }

        restablecerToken(posicionInicial)
        return null
    }

    /**
    *<Lista de Sentencias> ::= <Sentencia> [<Lista de Sentencias>]
    */
    private fun esListaSentencias(): ArrayList<Sentencia> {

        var lista:ArrayList<Sentencia> =  ArrayList<Sentencia>()
        var s: Sentencia? = esSentencia()

        while (s != null){
            lista.add(s)
            s = esSentencia()
        }

        return lista
    }

    /**
     * <Sentencia> ::= <Decisión> | <DeclaracionVariable> | <Asignacion> | <Impresión> |
     *                  <Ciclo> |<Retorno> | <Lectura> | <InvFunción> | <IncrDecr>
     */
    private fun esSentencia(): Sentencia? {

        var desicion:Desicion? = esDesicionSentencia()
        if(desicion != null){
            return desicion
        }

        var declaracionVariable:DeclaracionVariable? = esDeclaracionVariableSentencia()
        if(declaracionVariable != null){
            return declaracionVariable
        }

        var asignacion:Asignacion? = esAsignacionSentencia()
        if(asignacion != null){
            return asignacion
        }

        var impresion:Impresion? = esImpresionSentencia()
        if(impresion != null){
            return impresion
        }

        var ciclo:Ciclo? = esCicloSentencia()
        if(ciclo != null){
            return ciclo
        }

        var retorno:Retorno? = esRetornoSentencia()
        if(retorno != null){
            return retorno
        }

        var lectura:Lectura? = esLecturaSentencia()
        if(lectura != null){
            return lectura
        }

        var invFuncion:InvFuncion? = esInvFuncionSentencia()
        if(invFuncion != null){
            return invFuncion
        }

        var incrDecr:IncrDecr? = esIncrDecrSentencia()
        if(incrDecr != null){
            return incrDecr
        }

        return null
    }

    private fun esDesicionSentencia(): Desicion? {

        var posicionInicial = posicionActual

        if(tokenActual.categoria == Categoria.PALABRA_RESERVADA && tokenActual.lexema == "if"){
            obtenerSiguienteToken()

            if(tokenActual.categoria == Categoria.PARENTESIS_IZQUIERDO){
                obtenerSiguienteToken()
                var esprLogica:String? = esExpresionLogica()

                if(esprLogica != null){
                    obtenerSiguienteToken()

                    if(tokenActual.categoria == Categoria.PARENTESIS_DERECHO){
                        obtenerSiguienteToken()

                        if(tokenActual.categoria == Categoria.LLAVE_IZQUIERDO){
                            obtenerSiguienteToken()
                            val listaSentenciasIf:ArrayList<Sentencia> = esListaSentencias()

                            if(listaSentenciasIf.isNotEmpty()){
                                obtenerSiguienteToken()

                                if(tokenActual.categoria == Categoria.LLAVE_DERECHO){
                                    obtenerSiguienteToken()

                                    if(tokenActual.categoria == Categoria.PALABRA_RESERVADA && tokenActual.lexema == "else"){
                                        obtenerSiguienteToken()

                                        if(tokenActual.categoria == Categoria.LLAVE_IZQUIERDO) {
                                            obtenerSiguienteToken()
                                            val listaSentenciasElse: ArrayList<Sentencia> = esListaSentencias()

                                            if (listaSentenciasElse.isNotEmpty()) {
                                                obtenerSiguienteToken()

                                                if (tokenActual.categoria == Categoria.LLAVE_DERECHO) {
                                                    obtenerSiguienteToken()
                                                    return Desicion(esprLogica, listaSentenciasIf, listaSentenciasElse)
                                                }
                                            }
                                        }
                                    }

                                    return Desicion(esprLogica, listaSentenciasIf, ArrayList<Sentencia>())
                                }
                            }
                        }
                    }
                }
            }
        }

        restablecerToken(posicionInicial)
        return null
    }

    private fun esExpresionLogica(): String? {

        var posicionInicial = posicionActual

        if(tokenActual.categoria == Categoria.OPERADOR_LOGICO && tokenActual.lexema == "not"){
            obtenerSiguienteToken()
        }

        if(tokenActual.categoria == Categoria.PARENTESIS_IZQUIERDO){
            obtenerSiguienteToken()
            var expRelacional:String? = esExpresionRelacional()

            if(expRelacional != null){
                obtenerSiguienteToken()
                expRelacional = "(${expRelacional})"

                if(tokenActual.categoria == Categoria.PARENTESIS_DERECHO){
                    obtenerSiguienteToken()
                    var opeLogico:String? = esOperadorLogico()

                    if(opeLogico != null){
                        obtenerSiguienteToken()
                        var expLogica:String?  = esExpresionLogica()

                        if(expRelacional2 != null){
                            obtenerSiguienteToken()
                            expRelacional += opeLogico
                            expRelacional += expLogica
                        }
                    }

                    return expRelacional
                }
            }
        }
        restablecerToken(posicionInicial)
        return null
    }

    private fun esExpresionRelacional(): String? {

        if(tokenActual.categoria == Categoria.PARENTESIS_IZQUIERDO){

        }
    }


}
