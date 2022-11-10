package co.edu.uniquindio.compiladores.sintaxis

import co.edu.uniquindio.compiladores.lexico.Categoria
import co.edu.uniquindio.compiladores.lexico.Error
import co.edu.uniquindio.compiladores.lexico.Token


class AnalizadorSintactico(var listaTokens:ArrayList<Token>) {

    var posicionActual = -1 //Se inicia en -1 para poder leer la posicion 0
    var tokenActual = Token("Inicio",Categoria.PUNTO, 0, 0) //Se inicializa como un token sin importancia
    var listaErrores = ArrayList<Error>()

    fun obtenerSiguienteToken(){

        posicionActual++

        if(posicionActual < listaTokens.size){
            tokenActual = listaTokens[posicionActual]

            if(tokenActual.categoria == Categoria.COMENTARIO_LINEA || tokenActual.categoria == Categoria.COMENTARIO_BLOQUE){
                obtenerSiguienteToken()
            }
        }
    }

    /**
     * Metodo que resetea la posicion y el token que se esta utilizando
     */
    fun restablecerToken(posicion:Int){

        if(posicion < listaTokens.size){
            posicionActual = posicion
            tokenActual = listaTokens[posicionActual]
        }
    }

    fun reportarError(mensaje:String){
        listaErrores.add(Error(mensaje,tokenActual.fila, tokenActual.columna))
    }

    /**
     * <Unidad de Compilación> ::= <Visibilidad> Class Identificador “{” [ <Lista de Variables Globales>] [ <Lista de Funciones> ] “}”
     */
    fun esUnidadDeCompilacion(): UnidadDeCompilacion?{

        obtenerSiguienteToken() //Como comienza desde -1 se pide el siguiente token, esto para evitar que los comentarios sean problema
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

    /**
     * <Visibilidad> ::= pub | pvt
     */
    fun esVisibilidad():Token?{

        if(tokenActual.categoria == Categoria.PALABRA_RESERVADA ){
            if(tokenActual.lexema == "pub" || tokenActual.lexema == "pvt"){
                return tokenActual
            }
        }

        return null
    }

    /**
     * <Lista de Variables Globales> ::= <Variable Global> [<Lista de Variables Globales>]
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

    /**
     * <Variable Global> ::= <Visibilidad> <Tipo> Identificador “|
     */
    fun esVariableGlobal(): VariableGlobal? {

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
                    }else{
                        reportarError("Debe colocar el fin de sentencia de la variable global")
                    }
                }else{
                    reportarError("Debe definir el identificador de la variable global")
                }
            }else{
                reportarError("Debe definir el tipo de la variable global")
            }
            restablecerToken(posicionInicial)
        }

        return null
    }

    /**
     * <Tipo> ::= ent | dbe| str| booleano | Caracter
     */
    fun esTipo(): Token? {

        if(tokenActual.categoria == Categoria.PALABRA_RESERVADA ){
            if(tokenActual.lexema == "ent" || tokenActual.lexema == "dbe" || tokenActual.lexema == "str"
                || tokenActual.lexema == "bln" || tokenActual.lexema == "crt"){
                return tokenActual
            }
        }

        return null
    }

    /**
     * <Lista de Funciones> ::= <Función> [<Lista de Funciones>]
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

    /**
     * <Función> ::= <Visibilidad> <Valor Retornado> Identificador “(” [ <Lista de Parámetros con Tipo> ] “)” “{” <Lista de Sentencias> “}”
     */
    fun esFuncion(): Funcion? {

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
                        val listaParametroConTipo:ArrayList<ParametroConTipo> = esListaParametrosConTipo()

                        if(tokenActual.categoria == Categoria.PARENTESIS_DERECHO) {
                            obtenerSiguienteToken()

                            if(tokenActual.categoria == Categoria.LLAVE_IZQUIERDO) {
                                obtenerSiguienteToken()
                                val listaSentencias:ArrayList<Sentencia> = esListaSentencias()

                                if(tokenActual.categoria == Categoria.LLAVE_DERECHO) {
                                    obtenerSiguienteToken()
                                    return Funcion(visibilidad, valorRetornado, nombre, listaParametroConTipo, listaSentencias)
                                }else{
                                    reportarError("Debe definir la llave derecha de la funcion")
                                }
                            }else{
                                reportarError("Debe definir la llave izquierda de la funcion")
                            }
                        }else{
                            reportarError("Debe definir el parentesis derecho de la funcion")
                        }
                    }else{
                        reportarError("Debe definir el parentesis izquierdo de la funcion")
                    }
                }else{
                    reportarError("Debe definir el identificador de la funcion")
                }
            }else{
                reportarError("Debe definir el valor retornado de la funcion")
            }
            restablecerToken(posicionInicial)
        }

        return null
    }

    /**
     * <Valor Retornado> ::= Void | <Tipo>
     */
    fun esValorRetornado(): Token? {

        if(tokenActual.categoria == Categoria.PALABRA_RESERVADA ){
            if(tokenActual.lexema == "vd" || esTipo() != null){
                return tokenActual
            }
        }

        return null
    }

    /**
     * <Lista de Parámetros con Tipo> ::= <Parámetro Con Tipo> [“:” <Lista de Parámetros>]
     */
    fun esListaParametrosConTipo(): ArrayList<ParametroConTipo> {

        var lista:ArrayList<ParametroConTipo> =  ArrayList<ParametroConTipo>()
        var p: ParametroConTipo? = esParametroConTipo()

        while (p != null){
            lista.add(p)
            if(tokenActual.categoria == Categoria.SEPARADOR ){
                obtenerSiguienteToken()
                p = esParametroConTipo()
            }else{
                //reportarError("Debe definir el separador del parametro")
                p = null
            }
        }

        return lista

    }

    /**
    *<Parámetro Con Tipo> ::= <Tipo> Identificador
    */
    fun esParametroConTipo(): ParametroConTipo? {

        var posicionInicial = posicionActual
        var tipo = esTipo()

        if(tipo != null) {
            obtenerSiguienteToken()

            if (tokenActual.categoria == Categoria.IDENTIFICADOR) {
                var nombre = tokenActual
                obtenerSiguienteToken()
                return ParametroConTipo(tipo, nombre)
            }else{
                reportarError("Debe definir el identificador del parametro")
            }
        }

        restablecerToken(posicionInicial)
        return null
    }

    /**
     * <Lista de Parámetros sin Tipo> ::= <Parámetro Sin Tipo> [“:” <Lista de Parámetros sin Tipo>]
     */
    fun esListaParametrosSinTipo(): ArrayList<ParametroSinTipo> {

        var lista:ArrayList<ParametroSinTipo> =  ArrayList<ParametroSinTipo>()
        var p: ParametroSinTipo? = esParametroSinTipo()

        while (p != null){
            lista.add(p)
            if(tokenActual.categoria == Categoria.SEPARADOR ){
                obtenerSiguienteToken()
                p = esParametroSinTipo()
            }else{
                //reportarError("Debe definir el separador del parametro")
                p = null
            }
        }

        return lista

    }

    /**
     * <Parámetro Sin Tipo> ::= Identificador
     */
    fun esParametroSinTipo(): ParametroSinTipo? {

        var posicionInicial = posicionActual
        var tipo = esTipo()

        if(tipo == null) {

            if (tokenActual.categoria == Categoria.IDENTIFICADOR) {
                var nombre = tokenActual
                obtenerSiguienteToken()
                return ParametroSinTipo(nombre)
            }else{
                reportarError("Debe definir el identificador del parametro")
            }
        }else{
            reportarError("Este parametro no lleva tipo")
        }

        restablecerToken(posicionInicial)
        return null
    }

    /**
    *<Lista de Sentencias> ::= <Sentencia> [<Lista de Sentencias>]
    */
    fun esListaSentencias(): ArrayList<Sentencia> {

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
    fun esSentencia(): Sentencia? {

        var desicion:Desicion? = esDesicionSentencia()
        if(desicion != null){
            return desicion
        }

        var declaracionVariableInm:DeclaracionVariableInm? = esDeclaracionVariableInmSentencia()
        if(declaracionVariableInm != null){
            return declaracionVariableInm
        }

        var declaracionVariableMut:DeclaracionVariableMut? = esDeclaracionVariableMutSentencia()
        if(declaracionVariableMut != null){
            return declaracionVariableMut
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

    /**
     * <Decisión> ::= <Decisión if> [<Decisión else>]
     * <Decisión if> ::= if “(” <Expresión Lógica> “)” “{” <Lista de Sentencias> “}”
     * <Decisión else> ::= else “{” <Lista de Sentencias> “}”
     */
    fun esDesicionSentencia(): Desicion? {

        var posicionInicial = posicionActual

        if(tokenActual.categoria == Categoria.PALABRA_RESERVADA && tokenActual.lexema == "if"){
            obtenerSiguienteToken()

            if(tokenActual.categoria == Categoria.PARENTESIS_IZQUIERDO){
                obtenerSiguienteToken()
                var exprLogica:ExpresionLogica? = esExpresionLogica()

                if(exprLogica != null){

                    if(tokenActual.categoria == Categoria.PARENTESIS_DERECHO){
                        obtenerSiguienteToken()

                        if(tokenActual.categoria == Categoria.LLAVE_IZQUIERDO){
                            obtenerSiguienteToken()
                            val listaSentenciasIf:ArrayList<Sentencia> = esListaSentencias()

                            if(tokenActual.categoria == Categoria.LLAVE_DERECHO){
                                obtenerSiguienteToken()

                                if(tokenActual.categoria == Categoria.PALABRA_RESERVADA && tokenActual.lexema == "else"){
                                    obtenerSiguienteToken()

                                    if(tokenActual.categoria == Categoria.LLAVE_IZQUIERDO) {
                                        obtenerSiguienteToken()
                                        val listaSentenciasElse: ArrayList<Sentencia> = esListaSentencias()

                                        if (tokenActual.categoria == Categoria.LLAVE_DERECHO) {
                                            obtenerSiguienteToken()
                                            return Desicion(exprLogica, listaSentenciasIf, listaSentenciasElse)
                                        }else{
                                            reportarError("Debe definir la llave derecha de la desicion ELSE")
                                        }
                                    }else{
                                        reportarError("Debe definir la llave izquierda de la desicion ELSE")
                                    }
                                }else{
                                    reportarError("Debe indicar la palabra reservada ELSE de la desicion")
                                }

                                return Desicion(exprLogica, listaSentenciasIf, ArrayList<Sentencia>())
                            }else{
                                reportarError("Debe definir la llave derecha de la desicion IF")
                            }
                        }else{
                            reportarError("Debe definir la llave izquierda de la desicion IF")
                        }
                    }else{
                        reportarError("Debe definir el parentesis derecho de la desicion")
                    }
                }else{
                    reportarError("Debe definir una expresion logica valida en la desicion")
                }
            }else{
                reportarError("Debe definir el parentesis izquierdo de la desicion")
            }
        }

        restablecerToken(posicionInicial)
        return null
    }

    /**
     * <ExpLogica> ::= [not] "("<ExpRelacional>")" [<OpeLogico> <ExpLogica>]
     */
    fun esExpresionLogica(): ExpresionLogica? {

        var posicionInicial = posicionActual
        var negacion:Token? = null

        if(tokenActual.categoria == Categoria.OPERADOR_LOGICO && tokenActual.lexema == "not"){
            negacion = tokenActual
            obtenerSiguienteToken()
        }

        if(tokenActual.categoria == Categoria.PARENTESIS_IZQUIERDO){
            obtenerSiguienteToken()
            var expRelacional:ExpresionRelacional? = esExpresionRelacional()

            if(expRelacional != null){

                if(tokenActual.categoria == Categoria.PARENTESIS_DERECHO){
                    obtenerSiguienteToken()
                    var opeLogico:Token? = esOperadorLogico()

                    if(opeLogico != null){
                        obtenerSiguienteToken()
                        var expLogica:ExpresionLogica?  = esExpresionLogica()

                        if(expLogica != null){
                            return ExpresionLogica(negacion, expRelacional, opeLogico, expLogica)
                        }else{
                            reportarError("Expresion logica no valida")
                            restablecerToken(posicionActual - 1)
                        }
                    }

                    return ExpresionLogica(negacion, expRelacional, opeLogico, null)
                }else{
                    reportarError("Debe cerrar con parentesis derecho la expresion logica")
                }
            }else{
                reportarError("Expresion relacional no valida")
            }
        }else{
            reportarError("Debe iniciar con un parentecis izquierdo la expresion logica")
        }

        restablecerToken(posicionInicial)
        return null
    }

    /**
     * <OpeLogico> ::= and | or | xor
     */
    fun esOperadorLogico(): Token? {

        if(tokenActual.categoria == Categoria.OPERADOR_LOGICO){

            if(tokenActual.lexema == "and" || tokenActual.lexema == "or" || tokenActual.lexema == "xor"){
                return tokenActual
            }
        }
        return null
    }


    /**
     * <Exp Relacional> ::= <ExpAritmetica> OpeRacional “(“ <ExpAritmetica> “)”
     */
    fun esExpresionRelacional(): ExpresionRelacional? {

        var posicionInicial = posicionActual


        if(tokenActual.categoria == Categoria.PARENTESIS_IZQUIERDO) {
            obtenerSiguienteToken()
            var exprAritmetica:ExpresionAritmetica? = esExpresionAritmetica()

            if (exprAritmetica != null) {

                if(tokenActual.categoria == Categoria.PARENTESIS_DERECHO) {
                    obtenerSiguienteToken()

                    if (tokenActual.categoria == Categoria.OPERADOR_RELACIONAL) {
                        var opeRelacional = tokenActual
                        obtenerSiguienteToken()

                        if(tokenActual.categoria == Categoria.PARENTESIS_IZQUIERDO) {
                            obtenerSiguienteToken()
                            var exprAritmetica2: ExpresionAritmetica? = esExpresionAritmetica()

                            if (exprAritmetica2 != null) {

                                if(tokenActual.categoria == Categoria.PARENTESIS_DERECHO) {
                                    obtenerSiguienteToken()
                                    return ExpresionRelacional(exprAritmetica, opeRelacional, exprAritmetica2)
                                }else{
                                    reportarError("Debe encerrar en parentesis la expresion aritmetica")
                                }
                            }else{
                                reportarError("Expresion aritmetica no es valida")
                            }
                        }else{
                            reportarError("Debe encerrar en parentesis la expresion aritmetica")
                        }
                    }else{
                        reportarError("Falta el operador relacional en la expresion Relacional")
                    }
                }else{
                    reportarError("Debe encerrar en parentesis la expresion aritmetica")
                }
            }else{
                reportarError("Expresion aritmetica no es valida")
            }
        }else{
            reportarError("Debe encerrar en parentesis la expresion aritmetica")
        }

        restablecerToken(posicionInicial)
        return null
    }

    /**
     * <ExpAritmetica> ::= Numero [OperAritmetico “(” <ExpAritmetica> “)”]
     */
    fun esExpresionAritmetica(): ExpresionAritmetica? {

        var posicionInicial = posicionActual

        if(tokenActual.categoria == Categoria.ENTERO || tokenActual.categoria == Categoria.DECIMAL){
            var numero = tokenActual
            obtenerSiguienteToken()
            posicionInicial = posicionActual

            if(tokenActual.categoria == Categoria.OPERADOR_ARITMETICO){
                var operAritmetico = tokenActual
                obtenerSiguienteToken()

                if(tokenActual.categoria == Categoria.PARENTESIS_IZQUIERDO) {
                    obtenerSiguienteToken()
                    var exprAritmetica:ExpresionAritmetica? = esExpresionAritmetica()

                    if(exprAritmetica != null){

                        if(tokenActual.categoria == Categoria.PARENTESIS_DERECHO) {
                            obtenerSiguienteToken()
                            return ExpresionAritmetica(numero, operAritmetico, exprAritmetica)
                        }else{
                            reportarError("Debe encerrar en parentesis la expresion aritmetica")
                        }
                    }else{
                        reportarError("Expresion aritmetica no valida")
                    }
                }else{
                    reportarError("Debe encerrar en parentesis la expresion aritmetica")
                }
                restablecerToken(posicionInicial)
            }

            return ExpresionAritmetica(numero, null, null)
        }else{
            reportarError("Una expresion aritmetica debe tener minimo un numero")
        }

        restablecerToken(posicionInicial)
        return null
    }

    /**
     * <DeclaracionVariableMut> ::= <TipoVariable> identificador “|”
     */
    fun esDeclaracionVariableMutSentencia(): DeclaracionVariableMut? {
        var posicionInicial = posicionActual
        var tipo:Token? = esTipo()

        if(tipo != null){
            obtenerSiguienteToken()

            if (tokenActual.categoria == Categoria.IDENTIFICADOR) {
                var nombre: Token = tokenActual
                obtenerSiguienteToken()

                if (tokenActual.categoria == Categoria.FIN_SENTENCIA) {
                    obtenerSiguienteToken()
                    return DeclaracionVariableMut(tipo, nombre)
                }else{
                    reportarError("Falta el fin de sentencia")
                }
            }else{
                reportarError("Se espera un identificador")
            }
        }

        restablecerToken(posicionInicial)
        return null
    }

    /**
     * <DeclaracionVariableInm> ::= <TipoVariable> <Asignacion>
     */
    fun esDeclaracionVariableInmSentencia(): DeclaracionVariableInm? {
        var posicionInicial = posicionActual
        var tipo:Token? = esTipo()

        if(tipo != null){
            obtenerSiguienteToken()
            var asignacion:Asignacion? = esAsignacionSentencia();

            if(asignacion != null) {
                return DeclaracionVariableInm(tipo, asignacion)
            }else{
                reportarError("La asignacion no es valida")
            }
        }

        restablecerToken(posicionInicial)
        return null
    }

    /**
     * <Asignacion> ::= identificador <OpeAsignacion> <TipoAsig> “|”
     * <TipoAsig> ::= <Expresión> | identificador | <Lectura> | <InvFunción>
     */
    fun esAsignacionSentencia(): Asignacion? {
        var posicionInicial = posicionActual

        if(tokenActual.categoria == Categoria.IDENTIFICADOR){
            var identificador = tokenActual
            obtenerSiguienteToken()

            if(tokenActual.categoria == Categoria.OPERADOR_ASIGNACION){
                var opeAsignacion = tokenActual
                obtenerSiguienteToken()
                var expresion:Expresion? = esExpresion()

                if(expresion != null){

                    if(tokenActual.categoria == Categoria.FIN_SENTENCIA) {
                        obtenerSiguienteToken()
                        return Asignacion(identificador, opeAsignacion, expresion, null, null)
                    }else{
                        reportarError("Falta el fin de sentencia")
                    }
                }

                var sentencia:Sentencia? = esLecturaSentencia()

                if(sentencia != null){
                    return Asignacion(identificador, opeAsignacion, null, null, sentencia)
                }

                sentencia = esInvFuncionSentencia()

                if(sentencia != null){
                    return Asignacion(identificador, opeAsignacion, null, null, sentencia)
                }

                if(tokenActual.categoria == Categoria.IDENTIFICADOR){
                    var identificador2 = tokenActual
                    obtenerSiguienteToken()

                    if(tokenActual.categoria == Categoria.FIN_SENTENCIA) {
                        obtenerSiguienteToken()
                        return Asignacion(identificador, opeAsignacion, null, identificador2, null)
                    }else{
                        reportarError("Falta el fin de sentencia")
                    }
                }

                reportarError("Por favor especifique el tipo de asignacion")

            }else{
                reportarError("Falta el operador de asignacion")
            }
        }

        restablecerToken(posicionInicial)
        return null
    }

    /**
     * <Expresión> ::= <ExpAritmetica> | <Exp Relacional> | <ExpLogica> | <ExpCadena>
     */
    fun esExpresion(): Expresion? {
        var posicionInicial = posicionActual
        var expresion:Expresion? = esExpresionAritmetica()

        if(expresion != null){
            return expresion
        }

        expresion = esExpresionRelacional()

        if(expresion != null){
            return expresion
        }

        expresion = esExpresionLogica()

        if(expresion != null){
            return expresion
        }

        expresion = esExpresionCadena()

        if(expresion != null){
            return expresion
        }

        restablecerToken(posicionInicial)
        return null
    }

    /**
     * <ExpCadena> ::= <TipoExpCadena> [“+” <ExpCadena>]
     */
    fun esExpresionCadena(): ExpresionCadena? {
        var posicionInicial = posicionActual
        var tipoExpresionCadena:Token? = esTipoExpresionCadena()

        if(tipoExpresionCadena != null){
            obtenerSiguienteToken()

            if(tokenActual.categoria == Categoria.OPERADOR_ARITMETICO && tokenActual.lexema == "+"){
                var operAgrupacion = tokenActual
                obtenerSiguienteToken()
                var expresionCadena:ExpresionCadena? = esExpresionCadena()

                if(expresionCadena != null){
                    return ExpresionCadena(tipoExpresionCadena, operAgrupacion, expresionCadena)
                }else{
                    reportarError("Expresion cadena no valida")
                    restablecerToken(posicionActual - 1)
                }
            }
            return ExpresionCadena(tipoExpresionCadena, null, null)
        }else{
            reportarError("La expresion cadena debe tener minimo una cadena")
        }

        restablecerToken(posicionInicial)
        return null
    }

    /**
     * <TipoExpCadena> ::= ent |dbe | str| crt
     */
    fun esTipoExpresionCadena(): Token? {

        if(tokenActual.categoria == Categoria.ENTERO || tokenActual.categoria == Categoria.DECIMAL ||
            tokenActual.categoria == Categoria.CADENA_CARCTERES || tokenActual.categoria == Categoria.CARACTER){
            return tokenActual
        }
        return null
    }

    /**
     * <Impresión> ::= print “(” <TipoImpresion> “)” “|”
     * <TipoImpresion> ::= <Expresion> | identificador
     */
    fun esImpresionSentencia(): Impresion? {
        var posicionInicial = posicionActual

        if(tokenActual.categoria == Categoria.PALABRA_RESERVADA && tokenActual.lexema == "imp"){
            obtenerSiguienteToken()

            if(tokenActual.categoria == Categoria.PARENTESIS_IZQUIERDO){
                obtenerSiguienteToken()
                var expresion:Expresion? = esExpresion()

                if(expresion != null){

                    if(tokenActual.categoria == Categoria.PARENTESIS_DERECHO){
                        obtenerSiguienteToken()

                        if(tokenActual.categoria == Categoria.FIN_SENTENCIA){
                            obtenerSiguienteToken()
                            return Impresion(expresion, null)
                        }else{
                            reportarError("Falta el fin de sentencia")
                        }
                    }else{
                        reportarError("El tipo de impresion debe ir encerrado en parentesis")
                    }
                }

                if(tokenActual.categoria == Categoria.IDENTIFICADOR){
                    var identificador = tokenActual
                    obtenerSiguienteToken()

                    if(tokenActual.categoria == Categoria.PARENTESIS_DERECHO){
                        obtenerSiguienteToken()

                        if(tokenActual.categoria == Categoria.FIN_SENTENCIA){
                            obtenerSiguienteToken()
                            return Impresion(null, identificador)
                        }else{
                            reportarError("Falta el fin de sentencia")
                        }
                    }else{
                        reportarError("El tipo de impresion debe ir encerrado en parentesis")
                    }
                }

                reportarError("El elemento que desea imprimir no es valido")

            }else{
                reportarError("El tipo de impresion debe ir encerrado en parentesis")
            }
        }

        restablecerToken(posicionInicial)
        return null
    }

    /**
     * <While> ::= whl “(” <Expresión Lógica> “)” “{” <Lista de Sentencias> “}”
     */
    fun esCicloSentencia(): Ciclo? {
        var posicionInicial = posicionActual

        if(tokenActual.categoria == Categoria.PALABRA_RESERVADA && tokenActual.lexema == "whl"){
            obtenerSiguienteToken()

            if(tokenActual.categoria == Categoria.PARENTESIS_IZQUIERDO){
                obtenerSiguienteToken()
                var expresionLogica:ExpresionLogica? = esExpresionLogica()

                if(expresionLogica != null){

                    if(tokenActual.categoria == Categoria.PARENTESIS_DERECHO){
                        obtenerSiguienteToken()

                        if(tokenActual.categoria == Categoria.LLAVE_IZQUIERDO){
                            obtenerSiguienteToken()
                            val listaSentencias: ArrayList<Sentencia> = esListaSentencias()

                            if(tokenActual.categoria == Categoria.LLAVE_DERECHO){
                                obtenerSiguienteToken()
                                return Ciclo(expresionLogica, listaSentencias)
                            }else{
                                reportarError("Las sentencias deben ir encerradas en llaves")
                            }
                        }else{
                            reportarError("Las sentencias deben ir encerradas en llaves")
                        }
                    }else{
                        reportarError("La expresion logica debe ir encerrado en parentesis")
                    }
                }else{
                    reportarError("Expresion logica no valida")
                }
            }else{
                reportarError("La expresion logica debe ir encerrado en parentesis")
            }
        }

        restablecerToken(posicionInicial)
        return null
    }

    /**
     * <Retorno> ::= ret <TipoRetorno> “|”
     * <TipoRetorno> ::= identificador | <Expresión>
     */
    fun esRetornoSentencia(): Retorno? {
        var posicionInicial = posicionActual

        if(tokenActual.categoria == Categoria.PALABRA_RESERVADA && tokenActual.lexema == "ret"){
            obtenerSiguienteToken()
            var expresion:Expresion? = esExpresion()

            if(expresion != null){

                if(tokenActual.categoria == Categoria.FIN_SENTENCIA){
                    obtenerSiguienteToken()
                    return Retorno(null, expresion)
                }else{
                    reportarError("Falta el fin de sentencia")
                }
            }

            if(tokenActual.categoria == Categoria.IDENTIFICADOR){
                var identificador = tokenActual
                obtenerSiguienteToken()

                if(tokenActual.categoria == Categoria.FIN_SENTENCIA){
                    obtenerSiguienteToken()
                    return Retorno(identificador, null)
                }else{
                    reportarError("Falta el fin de sentencia")
                }
            }

            reportarError("El elemento que intenta retornar no es valido")

        }

        restablecerToken(posicionInicial)
        return null
    }

    /**
     * <Lectura> ::= read “(” cadena “)” “|
     */
    fun esLecturaSentencia(): Lectura? {
        var posicionInicial = posicionActual

        if(tokenActual.categoria == Categoria.PALABRA_RESERVADA && tokenActual.lexema == "rad"){
            obtenerSiguienteToken()

            if(tokenActual.categoria == Categoria.PARENTESIS_IZQUIERDO){
                obtenerSiguienteToken()

                if(tokenActual.categoria == Categoria.CADENA_CARCTERES){
                    var cadena = tokenActual
                    obtenerSiguienteToken()

                    if(tokenActual.categoria == Categoria.PARENTESIS_DERECHO){
                        obtenerSiguienteToken()

                        if(tokenActual.categoria == Categoria.FIN_SENTENCIA){
                            obtenerSiguienteToken()
                            return Lectura(cadena)
                        }else{
                            reportarError("Falta el fin de sentencia")
                        }
                    }else{
                        reportarError("La cadena debe ir encerrada en parentesis")
                    }
                }else{
                    reportarError("Se espera una cadena")
                }
            }else{
                reportarError("La cadena debe ir encerrada en parentesis")
            }
        }

        restablecerToken(posicionInicial)
        return null
    }

    /**
     * <InvFunción> ::= identificador “(” [<Lista de Parámetros sin Tipo>] “)” “|
     */
    fun esInvFuncionSentencia(): InvFuncion? {
        var posicionInicial = posicionActual

        if(tokenActual.categoria == Categoria.IDENTIFICADOR){
            var nombre = tokenActual
            obtenerSiguienteToken()

            if(tokenActual.categoria == Categoria.PARENTESIS_IZQUIERDO){
                obtenerSiguienteToken()
                val listaParametroSinTipo:ArrayList<ParametroSinTipo> = esListaParametrosSinTipo()

                if(tokenActual.categoria == Categoria.PARENTESIS_DERECHO){
                    obtenerSiguienteToken()

                    if(tokenActual.categoria == Categoria.FIN_SENTENCIA){
                        obtenerSiguienteToken()
                        return InvFuncion(nombre, listaParametroSinTipo)
                    }else{
                        reportarError("Falta el fin de sentencia")
                    }
                }else{
                    reportarError("los parametros deber estar encerrados en parentesis")
                }
            }
        }

        restablecerToken(posicionInicial)
        return null
    }

    /**
     * <IncrDecr> ::= identificador <TipoIncrDecr> “|”
     * <TipoIncrDecr> ::= OperIncremento | OperDecremento
     */
    fun esIncrDecrSentencia(): IncrDecr? {
        var posicionInicial = posicionActual

        if(tokenActual.categoria == Categoria.IDENTIFICADOR){
            var nombre = tokenActual
            obtenerSiguienteToken()

            if(tokenActual.categoria == Categoria.OPERADOR_INCREMETO || tokenActual.categoria == Categoria.OPERADOR_DECREMENTO) {
                var tipo = tokenActual
                obtenerSiguienteToken()

                if (tokenActual.categoria == Categoria.FIN_SENTENCIA) {
                    obtenerSiguienteToken()
                    return IncrDecr(nombre, tipo)
                }else{
                    reportarError("Falta el fin de sentencia")
                }
            }
        }

        restablecerToken(posicionInicial)
        return null
    }
}
