package co.edu.uniquindio.compiladores.lexico

class AnalizadorLexico(var codigoFuente:String) {

    var posicionActual = 0
    var caracterActual = if (codigoFuente.isEmpty()) 0.toChar() else codigoFuente[0]
    var listaTokens = ArrayList<Token>()
    var listaErrores = ArrayList<Error>()
    var finCodigo = 0.toChar()
    var filaActual = 1
    var columnaActual = 1

    fun almacenarToken(lexema:String, categoria: Categoria, fila:Int, columna:Int) = listaTokens.add(Token(lexema, categoria, fila, columna))

    fun analizar(){
        while(caracterActual != finCodigo){
            if(caracterActual == ' ' || caracterActual == '\t' || caracterActual == '\n' || caracterActual == '\r'){
                obtenerSiguienteCaracter()
                continue
            }

            if(esPalabraReservada()) continue

            if(esEntero()) continue

            if(esDecimal()) continue

            if(esIdentificador()) continue

            if(esOperadorLogico()) continue

            if(esOperadorIncremento()) continue

            if(esOperadorDecremento()) continue

            if(esOperadorRelacional()) continue

            if(esOperadorAsignacion()) continue

            if(esOperadorAritmetico()) continue

            if(esParentesisIzquierdo()) continue

            if(esParentesisDerecho()) continue

            if(esLlaveDerecha()) continue

            if(esLlaveIzquierda()) continue

            if(esCorcheteIzquierdo()) continue

            if(esCorcheteDerecho()) continue

            if(esFinSentencia()) continue

            if(esPunto()) continue

            if(esDosPuntos()) continue

            if(esSeparador()) continue

            if(esComentarioLinea()) continue

            if(esComentarioBloque()) continue

            if(esCaracter()) continue

            if(esCadenaCaracter()) continue

            manejarDesconocido()
        }
    }

    fun reportarError(mensaje:String, fila:Int, columna:Int){
        listaErrores.add(Error(mensaje, fila, columna))
    }

    fun mostrarCaracter(c:Char):String = when {
        c == '\n' -> "\\n"
        c == '\r' -> "\\r"
        c == '\t' -> "\\t"
        c.isISOControl() -> "\\u%04x".format(c.code)
        else -> c.toString()
    }

    /** Consume el resto del codigo fuente (hasta el fin) y lo almacena como un unico token DESCONOCIDO */
    private fun consumirRestoComoDesconocido(fila:Int, columna:Int){
        var lexema = ""
        while (caracterActual != finCodigo) {
            lexema += caracterActual
            obtenerSiguienteCaracter()
        }
        almacenarToken(lexema, Categoria.DESCONOCIDO, fila, columna)
    }

    /**
     * Ningun automata reconocio el caracter actual: se genera un token DESCONOCIDO y se reporta el error
     * lexico con la fila y columna donde empieza el problema.
     */
    fun manejarDesconocido(){
        val fila = filaActual
        val columna = columnaActual
        val posicion = posicionActual

        when (caracterActual) {
            '"' -> {
                if (posicion + 2 < codigoFuente.length && codigoFuente[posicion + 1] == '"' && codigoFuente[posicion + 2] == '"') {
                    reportarError("Comentario de bloque sin cerrar: falta el cierre \"\"\"", fila, columna)
                    consumirRestoComoDesconocido(fila, columna)
                    return
                }
                reportarError("Carácter no reconocido: '\"'", fila, columna)
            }
            '#' -> {
                reportarError("Entero mal formado: después de '#' se esperaba al menos un dígito", fila, columna)
            }
            '@' -> {
                var lexema = "@"
                obtenerSiguienteCaracter()
                while (caracterActual.isDigit()) { lexema += caracterActual; obtenerSiguienteCaracter() }
                // Si hubiera un punto, esDecimal() ya habria reconocido el token: aqui falta el punto
                val mensaje = if (lexema.length == 1) "Decimal mal formado: después de '@' se esperaba '.' con dígitos opcionales"
                              else "Decimal mal formado: falta el punto ('$lexema')"
                reportarError(mensaje, fila, columna)
                almacenarToken(lexema, Categoria.DESCONOCIDO, fila, columna)
                return
            }
            '^' -> {
                var lexema = "^"
                obtenerSiguienteCaracter()
                while (caracterActual.isLetterOrDigit()) { lexema += caracterActual; obtenerSiguienteCaracter() }
                reportarError("Identificador mal formado: después de '^' se esperaba una letra ('$lexema')", fila, columna)
                almacenarToken(lexema, Categoria.DESCONOCIDO, fila, columna)
                return
            }
            '\'' -> {
                reportarError(diagnosticarCadena(posicion), fila, columna)
                // La cadena mal formada se consume hasta el fin de la linea como un unico token DESCONOCIDO
                var lexema = ""
                while (caracterActual != finCodigo && caracterActual != '\n') {
                    lexema += caracterActual
                    obtenerSiguienteCaracter()
                }
                almacenarToken(lexema, Categoria.DESCONOCIDO, fila, columna)
                return
            }
            '°' -> {
                reportarError(diagnosticarCaracter(posicion), fila, columna)
                // El caracter mal formado se consume completo (hasta el siguiente ° o el fin de la linea) como un unico DESCONOCIDO
                var lexema = "°"
                obtenerSiguienteCaracter()
                var cerrado = false
                while (!cerrado && caracterActual != finCodigo && caracterActual != '\n') {
                    if (caracterActual == '\\') {
                        lexema += caracterActual
                        obtenerSiguienteCaracter()
                        if (caracterActual == finCodigo || caracterActual == '\n') break
                    } else if (caracterActual == '°') {
                        cerrado = true
                    }
                    lexema += caracterActual
                    obtenerSiguienteCaracter()
                }
                almacenarToken(lexema, Categoria.DESCONOCIDO, fila, columna)
                return
            }
            else -> {
                reportarError("Carácter no reconocido: '${mostrarCaracter(caracterActual)}'", fila, columna)
            }
        }
        almacenarToken(caracterActual + "", Categoria.DESCONOCIDO, fila, columna)
        obtenerSiguienteCaracter()
    }

    /** Letras (o caracteres) validos despues de la barra invertida en cadenas y caracteres: \n \t \" \' \\ */
    fun esLetraDeEscape(c:Char):Boolean = c == 'n' || c == 't' || c == '"' || c == '\'' || c == '\\'

    /** Explica por qué la cadena que empieza en la posicion dada (comilla simple) no es válida */
    private fun diagnosticarCadena(inicio:Int):String{
        var i = inicio + 1
        val n = codigoFuente.length
        while (i < n && codigoFuente[i] != '\'') {
            val c = codigoFuente[i]
            if (c == '\\') {
                if (i + 1 >= n) break
                val e = codigoFuente[i + 1]
                if (esLetraDeEscape(e)) i += 2
                else return "Cadena mal formada: secuencia de escape no válida '\\${mostrarCaracter(e)}'"
            } else i++
        }
        return "Cadena sin cerrar: falta la comilla simple de cierre"
    }

    /** Explica por qué el caracter que empieza en la posicion dada (simbolo °) no es válido */
    private fun diagnosticarCaracter(inicio:Int):String{
        val n = codigoFuente.length
        val i = inicio + 1
        if (i >= n) return "Carácter sin cerrar: falta el símbolo ° de cierre"
        val c = codigoFuente[i]
        if (c == '°') return "Carácter mal formado: no puede estar vacío"
        if (c == '\\') {
            if (i + 1 >= n) return "Carácter sin cerrar: falta el símbolo ° de cierre"
            val e = codigoFuente[i + 1]
            if (!esLetraDeEscape(e))
                return "Carácter mal formado: secuencia de escape no válida '\\${mostrarCaracter(e)}'"
            return "Carácter sin cerrar: falta el símbolo ° de cierre"
        }
        return "Carácter sin cerrar o mal formado: se esperaba un solo símbolo seguido de °"
    }

    private val palabrasReservadas = listOf(
        "ent", "dbe", "str", "pub", "pvt", "abs", "cth", "brk", "cas", "ptt", "cls", "bln", "crt", "mut", "inm",
        "whl", "ret", "rad", "tre", "fls", "if", "else", "vd", "imp", "cat", "array"
    )

    private val operadoresLogicos = listOf("not", "or", "xor", "and")

    fun esPalabraReservada():Boolean = esAlgunaPalabra(palabrasReservadas, Categoria.PALABRA_RESERVADA)

    fun esOperadorLogico():Boolean = esAlgunaPalabra(operadoresLogicos, Categoria.OPERADOR_LOGICO)

    private fun esAlgunaPalabra(palabras:List<String>, categoria:Categoria):Boolean = palabras.any { esPalabra(it, categoria) }

    /**
     * Reconoce la palabra exacta en la posicion actual. Si coincide completa, almacena el token; si no,
     * deja el analizador en la posicion inicial (lo que sigue lo trata el resto de automatas).
     */
    private fun esPalabra(palabra:String, categoria:Categoria):Boolean{
        if(caracterActual != palabra[0]) return false
        val filaInicial = filaActual
        val columnaInicial = columnaActual
        val posicionInicial = posicionActual
        var lexema = ""
        for (c in palabra) {
            if (caracterActual != c) {
                restablecerCaracterActual(posicionInicial, filaInicial, columnaInicial)
                return false
            }
            lexema += caracterActual
            obtenerSiguienteCaracter()
        }
        almacenarToken(lexema, categoria, filaInicial, columnaInicial)
        return true
    }

    /** Reconoce un unico caracter perteneciente a [simbolos] como un token de la categoria dada */
    private fun esSimbolo(simbolos:String, categoria:Categoria):Boolean{
        if(simbolos.indexOf(caracterActual) < 0) return false
        val filaInicial = filaActual
        val columnaInicial = columnaActual
        val lexema = caracterActual.toString()
        obtenerSiguienteCaracter()
        almacenarToken(lexema, categoria, filaInicial, columnaInicial)
        return true
    }

    /** Reconoce el caracter [inicio] seguido de [segundo] (p. ej. "++", "==", "+=") */
    private fun esParDeCaracteres(inicio:String, segundo:Char, categoria:Categoria):Boolean{
        if(inicio.indexOf(caracterActual) < 0) return false
        val filaInicial = filaActual
        val columnaInicial = columnaActual
        val posicionInicial = posicionActual
        var lexema = caracterActual.toString()
        obtenerSiguienteCaracter()
        if(caracterActual == segundo) {
            lexema += caracterActual
            obtenerSiguienteCaracter()
            almacenarToken(lexema, categoria, filaInicial, columnaInicial)
            return true
        }
        restablecerCaracterActual(posicionInicial, filaInicial, columnaInicial)
        return false
    }

    fun esEntero():Boolean{
        if(caracterActual == '#'){
            var lexema = ""
            val filaInicial = filaActual
            val columnaInicial = columnaActual
            val posicionInicial = posicionActual

            lexema += caracterActual
            obtenerSiguienteCaracter()

            if(caracterActual.isDigit()) {
                lexema += caracterActual
                obtenerSiguienteCaracter()

                while (caracterActual.isDigit()) {
                    lexema += caracterActual
                    obtenerSiguienteCaracter()
                }
                almacenarToken(lexema, Categoria.ENTERO, filaInicial, columnaInicial)
                return true
            }
            restablecerCaracterActual(posicionInicial,filaInicial,columnaInicial)
        }
        return false
    }

    fun esDecimal():Boolean{

        if(caracterActual == '@'){
            var lexema = ""
            val filaInicial = filaActual
            val columnaInicial = columnaActual
            val posicionInicial = posicionActual

            lexema += caracterActual
            obtenerSiguienteCaracter()

            while (caracterActual.isDigit()) {
                lexema += caracterActual
                obtenerSiguienteCaracter()
            }

            if (caracterActual == '.') {
                lexema += caracterActual
                obtenerSiguienteCaracter()

                while (caracterActual.isDigit()) {
                    lexema += caracterActual
                    obtenerSiguienteCaracter()
                }

                almacenarToken(lexema, Categoria.DECIMAL, filaInicial, columnaInicial)
                return true
            }
            restablecerCaracterActual(posicionInicial,filaInicial,columnaInicial)
        }
        return false
    }

    fun esIdentificador():Boolean{
        if(caracterActual == '^'){
            var lexema = ""
            val filaInicial = filaActual
            val columnaInicial = columnaActual
            val posicionInicial = posicionActual

            lexema += caracterActual
            obtenerSiguienteCaracter()

            if(caracterActual.isLetter()) {
                lexema += caracterActual
                obtenerSiguienteCaracter()
                var i = 1

                while (caracterActual.isLetterOrDigit() && i <= 8) {
                    lexema += caracterActual
                    obtenerSiguienteCaracter()
                    i++
                }
                almacenarToken(lexema, Categoria.IDENTIFICADOR, filaInicial, columnaInicial)
                if (caracterActual.isLetterOrDigit()) {
                    reportarError("Identificador demasiado largo: máximo 10 caracteres incluyendo '^' ('$lexema')", filaInicial, columnaInicial)
                    val filaExceso = filaActual
                    val columnaExceso = columnaActual
                    var exceso = ""
                    while (caracterActual.isLetterOrDigit()) {
                        exceso += caracterActual
                        obtenerSiguienteCaracter()
                    }
                    almacenarToken(exceso, Categoria.DESCONOCIDO, filaExceso, columnaExceso)
                }
                return true
            }
            restablecerCaracterActual(posicionInicial,filaInicial,columnaInicial)
        }
        return false
    }

    fun esOperadorIncremento():Boolean = esParDeCaracteres("+", '+', Categoria.OPERADOR_INCREMENTO)

    fun esOperadorDecremento():Boolean = esParDeCaracteres("-", '-', Categoria.OPERADOR_DECREMENTO)

    fun esOperadorAritmetico():Boolean = esSimbolo("-+*/%", Categoria.OPERADOR_ARITMETICO)

    fun esOperadorRelacional():Boolean{

        if(esOperadorRelacionalIgualQue()) return true

        if(esOperadorRelacionalDistintoQue()) return true

        if(esOperadorRelacionalMenorMayorQue()) return true

        return false
    }

    fun esOperadorRelacionalIgualQue():Boolean = esParDeCaracteres("=", '=', Categoria.OPERADOR_RELACIONAL)

    fun esOperadorRelacionalDistintoQue():Boolean = esParDeCaracteres("!", '=', Categoria.OPERADOR_RELACIONAL)

    fun esOperadorRelacionalMenorMayorQue():Boolean{
        if(caracterActual == '<' || caracterActual == '>'){
            val filaInicial = filaActual
            val columnaInicial = columnaActual
            var lexema = caracterActual.toString()
            obtenerSiguienteCaracter()

            if(caracterActual == '=') {
                lexema += caracterActual
                obtenerSiguienteCaracter()
            }
            almacenarToken(lexema, Categoria.OPERADOR_RELACIONAL, filaInicial, columnaInicial)
            return true
        }
        return false
    }

    fun esOperadorAsignacion():Boolean{
        if(esParDeCaracteres("-+*/%", '=', Categoria.OPERADOR_ASIGNACION)) return true
        return esSimbolo("=", Categoria.OPERADOR_ASIGNACION)
    }

    fun esParentesisIzquierdo():Boolean = esSimbolo("(", Categoria.PARENTESIS_IZQUIERDO)

    fun esParentesisDerecho():Boolean = esSimbolo(")", Categoria.PARENTESIS_DERECHO)

    fun esLlaveIzquierda():Boolean = esSimbolo("{", Categoria.LLAVE_IZQUIERDO)

    fun esLlaveDerecha():Boolean = esSimbolo("}", Categoria.LLAVE_DERECHO)

    fun esCorcheteDerecho():Boolean = esSimbolo("]", Categoria.CORCHETE_DERECHO)

    fun esCorcheteIzquierdo():Boolean = esSimbolo("[", Categoria.CORCHETE_IZQUIERDO)

    fun esFinSentencia():Boolean = esSimbolo("|", Categoria.FIN_SENTENCIA)

    fun esPunto():Boolean = esSimbolo("_", Categoria.PUNTO)

    fun esDosPuntos():Boolean = esSimbolo(";", Categoria.DOS_PUNTOS)

    fun esSeparador():Boolean = esSimbolo(":", Categoria.SEPARADOR)

    fun esComentarioLinea():Boolean{
        if(caracterActual == '&'){
            var lexema = ""
            val filaInicial = filaActual
            val columnaInicial = columnaActual

            lexema += caracterActual
            obtenerSiguienteCaracter()

            while (caracterActual != '\n' && caracterActual != finCodigo) {
                lexema += caracterActual
                obtenerSiguienteCaracter()
            }

            // Segun el automata, el comentario debe terminar en un salto de línea
            if(caracterActual == '\n') {
                lexema += caracterActual
                obtenerSiguienteCaracter()
                almacenarToken(lexema, Categoria.COMENTARIO_LINEA, filaInicial, columnaInicial)
            } else {
                reportarError("Comentario de línea sin salto de línea final", filaInicial, columnaInicial)
                almacenarToken(lexema, Categoria.DESCONOCIDO, filaInicial, columnaInicial)
            }
            return true
        }
        return false
    }

    fun esComentarioBloque():Boolean{
        if(caracterActual == '"'){
            var lexema = ""
            val filaInicial = filaActual
            val columnaInicial = columnaActual
            val posicionInicial = posicionActual

            lexema += caracterActual
            obtenerSiguienteCaracter()

            if(caracterActual == '"') {
                lexema += caracterActual
                obtenerSiguienteCaracter()

                if(caracterActual == '"') {
                    lexema += caracterActual
                    obtenerSiguienteCaracter()
                    val lista = ArrayList<Char>()
                    var bandera = true

                    while (bandera && caracterActual != finCodigo) {
                        lexema += caracterActual

                        lista.add(0,caracterActual)
                        obtenerSiguienteCaracter()

                        if(lista.size >= 3) {
                            if (lista.get(0) == '"' && lista.get(1) == '"' && lista.get(2) == '"') {
                                bandera = false
                            }
                        }

                    }

                    if(!bandera) {
                        almacenarToken(lexema, Categoria.COMENTARIO_BLOQUE, filaInicial, columnaInicial)
                        return true
                    }
                }
            }
            restablecerCaracterActual(posicionInicial,filaInicial,columnaInicial)
        }
        return false
    }

    fun esCaracter():Boolean{
        if(caracterActual == '°'){
            var lexema = ""
            val filaInicial = filaActual
            val columnaInicial = columnaActual
            val posicionInicial = posicionActual

            lexema += caracterActual
            obtenerSiguienteCaracter()

            if(caracterActual == '\\'){
                lexema += caracterActual
                obtenerSiguienteCaracter()

                if(esLetraDeEscape(caracterActual)){
                    lexema += caracterActual
                    obtenerSiguienteCaracter()

                    if(caracterActual == '°') {
                        lexema += caracterActual
                        obtenerSiguienteCaracter()

                        almacenarToken(lexema, Categoria.CARACTER, filaInicial, columnaInicial)
                        return true
                    }
                }
                restablecerCaracterActual(posicionInicial,filaInicial,columnaInicial)
            }else if(caracterActual != finCodigo && caracterActual != '°'){
                lexema += caracterActual
                obtenerSiguienteCaracter()

                if(caracterActual == '°') {
                    lexema += caracterActual
                    obtenerSiguienteCaracter()

                    almacenarToken(lexema, Categoria.CARACTER, filaInicial, columnaInicial)
                    return true
                }
            }
            restablecerCaracterActual(posicionInicial,filaInicial,columnaInicial)
        }
        return false
    }

    fun esCadenaCaracter():Boolean{
        if(caracterActual == '\''){
            var lexema = ""
            val filaInicial = filaActual
            val columnaInicial = columnaActual
            val posicionInicial = posicionActual

            lexema += caracterActual
            obtenerSiguienteCaracter()

            while (caracterActual != '\'' && caracterActual != finCodigo) {

                if(caracterActual=='\\'){

                    lexema += caracterActual
                    obtenerSiguienteCaracter()

                    if(esLetraDeEscape(caracterActual)){

                        lexema += caracterActual
                        obtenerSiguienteCaracter()
                    }else{
                        restablecerCaracterActual(posicionInicial,filaInicial,columnaInicial)
                        return false
                    }

                }else{
                    // C = cualquier caracter excepto '\' y ''' (incluye saltos de linea reales, tabuladores y comillas dobles)
                    lexema += caracterActual
                    obtenerSiguienteCaracter()
                }
            }

            if(caracterActual == '\'' && caracterActual != finCodigo){
                lexema += caracterActual
                obtenerSiguienteCaracter()
                almacenarToken(lexema, Categoria.CADENA_CARACTERES, filaInicial, columnaInicial)
                return true
            }

            restablecerCaracterActual(posicionInicial,filaInicial,columnaInicial)
        }
        return false
    }

    fun obtenerSiguienteCaracter(){
        if(posicionActual == codigoFuente.length - 1){
            caracterActual = finCodigo
        }else{
            if(caracterActual == '\n'){
                filaActual++
                columnaActual = 1
            }else{
                columnaActual++
            }

            posicionActual++
            caracterActual = codigoFuente[posicionActual]
        }
    }

    fun restablecerCaracterActual(posicionInicial:Int, filaInicial:Int, columnaInicial:Int){
        filaActual = filaInicial
        columnaActual = columnaInicial
        posicionActual = posicionInicial
        caracterActual = codigoFuente[posicionActual]
    }

}