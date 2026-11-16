package co.edu.uniquindio.compiladores.sintaxis

import co.edu.uniquindio.compiladores.lexico.Categoria
import co.edu.uniquindio.compiladores.lexico.Error
import co.edu.uniquindio.compiladores.lexico.Token


class AnalizadorSintactico(var listaTokens:ArrayList<Token>) {

    var posicionActual = -1 //Se inicia en -1 para poder leer la posicion 0
    var tokenActual = Token("Inicio",Categoria.PUNTO, 0, 0) //Se inicializa como un token sin importancia
    var listaErrores = ArrayList<Error>()

    /** Error mas lejano visto durante el analisis (incluidos los de alternativas descartadas) y la posicion de token donde ocurrio */
    private var falloMasLejano: Error? = null
    private var posicionFalloMasLejano = -1

    /** Token que representa el fin del archivo (se usa cuando se agotan los tokens) */
    private fun tokenFinDeArchivo(): Token {
        val ultimo = listaTokens.lastOrNull()
        if (ultimo == null) return Token("", Categoria.DESCONOCIDO, 1, 1)
        // Posicion justo despues del ultimo token (que puede contener saltos de linea)
        val saltos = ultimo.lexema.count { it == '\n' }
        val fila = ultimo.fila + saltos
        val columna = if (saltos == 0) ultimo.columna + ultimo.lexema.length else ultimo.lexema.length - ultimo.lexema.lastIndexOf('\n')
        return Token("", Categoria.DESCONOCIDO, fila, columna)
    }

    fun obtenerSiguienteToken(){

        posicionActual++

        if(posicionActual < listaTokens.size){
            tokenActual = listaTokens[posicionActual]

            if(tokenActual.categoria == Categoria.COMENTARIO_LINEA || tokenActual.categoria == Categoria.COMENTARIO_BLOQUE){
                obtenerSiguienteToken()
            }
        }else{
            tokenActual = tokenFinDeArchivo()
        }
    }

    /**
     * Metodo que resetea la posicion y el token que se esta utilizando
     */
    fun restablecerToken(posicion:Int){

        posicionActual = posicion

        if(posicion >= 0 && posicion < listaTokens.size){
            tokenActual = listaTokens[posicionActual]
        }else if(posicion >= listaTokens.size){
            tokenActual = tokenFinDeArchivo()
        }
    }

    /**
     * Retrocede a una posicion guardada y descarta los errores reportados desde ese punto: eran especulativos,
     * producidos al probar una alternativa que se abandona. (El error mas lejano se conserva aparte para
     * explicar el fallo si ninguna alternativa tiene exito.)
     */
    fun restablecerToken(posicion:Int, cantidadErrores:Int){
        restablecerToken(posicion)

        if(listaErrores.size > cantidadErrores){
            listaErrores.subList(cantidadErrores, listaErrores.size).clear()
        }
    }

    /**
     * @param candidato si es false el error se lista pero no compite como "error mas lejano". Se usa para
     * alternativas debiles/ambiguas: en un empate de posicion el primer error reportado gana, y asi un mensaje
     * generico de una alternativa no tapa al de la alternativa especifica que se prueba despues.
     */
    fun reportarError(mensaje:String, candidato:Boolean = true){
        val error = Error(mensaje, tokenActual.fila, tokenActual.columna)

        if(candidato && posicionActual > posicionFalloMasLejano){
            falloMasLejano = error
            posicionFalloMasLejano = posicionActual
        }

        agregarError(error)
    }

    private fun agregarError(error: Error){
        if(listaErrores.none { it.error == error.error && it.fila == error.fila && it.columna == error.columna }){
            listaErrores.add(error)
        }
    }

    /**
     * Reporta un error de nivel de unidad de compilacion. Si durante el analisis se llego mas lejos que la posicion
     * actual (una alternativa fallo mas adelante y se descarto), se reporta ese error, que es el que realmente
     * explica el problema, en lugar del mensaje generico.
     */
    private fun reportarErrorFinal(mensaje:String){
        val lejano = falloMasLejano

        if(lejano != null && posicionFalloMasLejano > posicionActual){
            agregarError(lejano)
        }else{
            reportarError(mensaje)
        }
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
                            reportarTokensSobrantes()
                            return UnidadDeCompilacion(visibilidad, nombre, listaVariablesGlobales, listaFunciones)
                        }else{
                            reportarErrorFinal("Falta la llave derecha")
                        }
                    }else{
                        reportarErrorFinal("Falta la llave izquierda")
                    }
                }else{
                    reportarErrorFinal("Falta el identificador de la clase")
                }
            }else{
                reportarErrorFinal("Falta palabra reservada cls")
            }
        }else{
            reportarErrorFinal("Debe definir la visibilidad de la clase")
        }
        return null
    }

    /**
     * Despues de la llave que cierra la clase no debe quedar nada (salvo comentarios)
     */
    private fun reportarTokensSobrantes(){
        var i = posicionActual + 1

        while(i < listaTokens.size && (listaTokens[i].categoria == Categoria.COMENTARIO_LINEA || listaTokens[i].categoria == Categoria.COMENTARIO_BLOQUE)){
            i++
        }

        if(i < listaTokens.size){
            val sobrante = listaTokens[i]
            agregarError(Error("Hay codigo despues de la llave que cierra la clase", sobrante.fila, sobrante.columna))
        }
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
        val erroresIniciales = listaErrores.size

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
                //No compite como error mas lejano: "pub X" puede ser tambien una funcion y esa alternativa se prueba despues
                reportarError("Debe definir el tipo de la variable global", candidato = false)
            }
            restablecerToken(posicionInicial, erroresIniciales)
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
        val erroresIniciales = listaErrores.size

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
            restablecerToken(posicionInicial, erroresIniciales)
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
        val erroresIniciales = listaErrores.size
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

        restablecerToken(posicionInicial, erroresIniciales)
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
        val erroresIniciales = listaErrores.size
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

        restablecerToken(posicionInicial, erroresIniciales)
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

        var decision:Decision? = esDecisionSentencia()
        if(decision != null){
            return decision
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
    fun esDecisionSentencia(): Decision? {

        var posicionInicial = posicionActual
        val erroresIniciales = listaErrores.size

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
                                            return Decision(exprLogica, listaSentenciasIf, listaSentenciasElse)
                                        }else{
                                            reportarError("Debe definir la llave derecha de la desicion ELSE")
                                        }
                                    }else{
                                        reportarError("Debe definir la llave izquierda de la desicion ELSE")
                                    }
                                }else{
                                    return Decision(exprLogica, listaSentenciasIf, ArrayList<Sentencia>())
                                }
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

        restablecerToken(posicionInicial, erroresIniciales)
        return null
    }

    /**
     * <ExpLogica> ::= [not] "("<ExpRelacional>")" [<OpeLogico> <ExpLogica>]
     */
    fun esExpresionLogica(): ExpresionLogica? {

        var posicionInicial = posicionActual
        val erroresIniciales = listaErrores.size
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
                        val posicionOperador = posicionActual
                        obtenerSiguienteToken()
                        var expLogica:ExpresionLogica?  = esExpresionLogica()

                        if(expLogica != null){
                            return ExpresionLogica(negacion, expRelacional, opeLogico, expLogica)
                        }else{
                            reportarError("Expresion logica no valida")
                            restablecerToken(posicionOperador)
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

        restablecerToken(posicionInicial, erroresIniciales)
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
        val erroresIniciales = listaErrores.size


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

        restablecerToken(posicionInicial, erroresIniciales)
        return null
    }

    /**
     * <ExpAritmetica> ::= Numero [OperAritmetico “(” <ExpAritmetica> “)”]
     */
    fun esExpresionAritmetica(): ExpresionAritmetica? {

        var posicionInicial = posicionActual
        val erroresIniciales = listaErrores.size

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
                restablecerToken(posicionInicial, erroresIniciales)
            }

            return ExpresionAritmetica(numero, null, null)
        }else{
            reportarError("Una expresion aritmetica debe tener minimo un numero")
        }

        restablecerToken(posicionInicial, erroresIniciales)
        return null
    }

    /**
     * <DeclaracionVariableMut> ::= <TipoVariable> identificador “|”
     */
    fun esDeclaracionVariableMutSentencia(): DeclaracionVariableMut? {
        var posicionInicial = posicionActual
        val erroresIniciales = listaErrores.size
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

        restablecerToken(posicionInicial, erroresIniciales)
        return null
    }

    /**
     * <DeclaracionVariableInm> ::= <TipoVariable> <Asignacion>
     */
    fun esDeclaracionVariableInmSentencia(): DeclaracionVariableInm? {
        var posicionInicial = posicionActual
        val erroresIniciales = listaErrores.size
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

        restablecerToken(posicionInicial, erroresIniciales)
        return null
    }

    /**
     * <Asignacion> ::= identificador <OpeAsignacion> <TipoAsig> “|”
     * <TipoAsig> ::= <Expresión> | identificador | <Lectura> | <InvFunción>
     */
    fun esAsignacionSentencia(): Asignacion? {
        var posicionInicial = posicionActual
        val erroresIniciales = listaErrores.size

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

        restablecerToken(posicionInicial, erroresIniciales)
        return null
    }

    /**
     * <Expresión> ::= <ExpAritmetica> | <Exp Relacional> | <ExpLogica> | <ExpCadena>
     */
    fun esExpresion(): Expresion? {
        var posicionInicial = posicionActual
        val erroresIniciales = listaErrores.size
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

        restablecerToken(posicionInicial, erroresIniciales)
        return null
    }

    /**
     * <ExpCadena> ::= <TipoExpCadena> [“+” <ExpCadena>]
     */
    fun esExpresionCadena(): ExpresionCadena? {
        var posicionInicial = posicionActual
        val erroresIniciales = listaErrores.size
        var tipoExpresionCadena:Token? = esTipoExpresionCadena()

        if(tipoExpresionCadena != null){
            obtenerSiguienteToken()

            if(tokenActual.categoria == Categoria.OPERADOR_ARITMETICO && tokenActual.lexema == "+"){
                var operAgrupacion = tokenActual
                val posicionOperador = posicionActual
                obtenerSiguienteToken()
                var expresionCadena:ExpresionCadena? = esExpresionCadena()

                if(expresionCadena != null){
                    return ExpresionCadena(tipoExpresionCadena, operAgrupacion, expresionCadena)
                }else{
                    reportarError("Expresion cadena no valida")
                    restablecerToken(posicionOperador)
                }
            }
            return ExpresionCadena(tipoExpresionCadena, null, null)
        }else{
            reportarError("La expresion cadena debe tener minimo una cadena")
        }

        restablecerToken(posicionInicial, erroresIniciales)
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
        val erroresIniciales = listaErrores.size

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

        restablecerToken(posicionInicial, erroresIniciales)
        return null
    }

    /**
     * <While> ::= whl “(” <Expresión Lógica> “)” “{” <Lista de Sentencias> “}”
     */
    fun esCicloSentencia(): Ciclo? {
        var posicionInicial = posicionActual
        val erroresIniciales = listaErrores.size

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

        restablecerToken(posicionInicial, erroresIniciales)
        return null
    }

    /**
     * <Retorno> ::= ret <TipoRetorno> “|”
     * <TipoRetorno> ::= identificador | <Expresión>
     */
    fun esRetornoSentencia(): Retorno? {
        var posicionInicial = posicionActual
        val erroresIniciales = listaErrores.size

        if(tokenActual.categoria == Categoria.PALABRA_RESERVADA && tokenActual.lexema == "ret"){
            val tokenRet = tokenActual
            obtenerSiguienteToken()
            var expresion:Expresion? = esExpresion()

            if(expresion != null){

                if(tokenActual.categoria == Categoria.FIN_SENTENCIA){
                    obtenerSiguienteToken()
                    return Retorno(null, expresion, tokenRet.fila, tokenRet.columna)
                }else{
                    reportarError("Falta el fin de sentencia")
                }
            }

            if(tokenActual.categoria == Categoria.IDENTIFICADOR){
                var identificador = tokenActual
                obtenerSiguienteToken()

                if(tokenActual.categoria == Categoria.FIN_SENTENCIA){
                    obtenerSiguienteToken()
                    return Retorno(identificador, null, tokenRet.fila, tokenRet.columna)
                }else{
                    reportarError("Falta el fin de sentencia")
                }
            }

            reportarError("El elemento que intenta retornar no es valido")

        }

        restablecerToken(posicionInicial, erroresIniciales)
        return null
    }

    /**
     * <Lectura> ::= read “(” cadena “)” “|
     */
    fun esLecturaSentencia(): Lectura? {
        var posicionInicial = posicionActual
        val erroresIniciales = listaErrores.size

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

        restablecerToken(posicionInicial, erroresIniciales)
        return null
    }

    /**
     * <InvFunción> ::= identificador “(” [<Lista de Parámetros sin Tipo>] “)” “|
     */
    fun esInvFuncionSentencia(): InvFuncion? {
        var posicionInicial = posicionActual
        val erroresIniciales = listaErrores.size

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

        restablecerToken(posicionInicial, erroresIniciales)
        return null
    }

    /**
     * <IncrDecr> ::= identificador <TipoIncrDecr> “|”
     * <TipoIncrDecr> ::= OperIncremento | OperDecremento
     */
    fun esIncrDecrSentencia(): IncrDecr? {
        var posicionInicial = posicionActual
        val erroresIniciales = listaErrores.size

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

        restablecerToken(posicionInicial, erroresIniciales)
        return null
    }
}
