package co.edu.uniquindio.compiladores.lexico

import java.util.ArrayList

class AnalizadorLexico (var codigoFuente:String) {

    var posicionAcual = 0
    var caracterActual = if (codigoFuente.isEmpty()) 0.toChar() else codigoFuente[0]
    var listaTokens = ArrayList<Token>()
    var listaErrores = ArrayList<Error>()
    var finCodigo = 0.toChar()
    var filaActual = 1
    var columnaActual = 1

    fun  almacenarToken(lexema:String, categoria: Categoria, fila:Int, columna:Int) = listaTokens.add(Token(lexema, categoria, fila, columna))

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

            if(esParantesisDerecho()) continue

            if(esLlavesDerecha()) continue

            if(esLlavesIzquierda()) continue

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
        val posicion = posicionAcual

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

    fun esPalabraReservada():Boolean{

        if(esPalabraReservadaEnt()) return true

        if(esPalabraReservadaDbe()) return true

        if(esPalabraReservadaStr()) return true

        if(esPalabraReservadaPub()) return true

        if(esPalabraReservadaPvt()) return true

        if(esPalabraReservadaAbs()) return true

        if(esPalabraReservadaCth()) return true

        if(esPalabraReservadaBrk()) return true

        if(esPalabraReservadaCas()) return true

        if(esPalabraReservadaPtt()) return true

        if(esPalabraReservadaCls()) return true

        if(esPalabraReservadaBln()) return true

        if(esPalabraReservadaCrt()) return true

        if(esPalabraReservadaMut()) return true

        if(esPalabraReservadaInm()) return true

        if(esPalabraReservadaWhl()) return true

        if(esPalabraReservadaRet()) return true

        if(esPalabraReservadaRad()) return true

        if(esPalabraReservadaTre()) return true

        if(esPalabraReservadaFls()) return true

        if(esPalabraReservadaIf()) return true

        if(esPalabraReservadaElse()) return true

        if(esPalabraReservadaVd()) return true

        if(esPalabraReservadaImp()) return true

        if(esPalabraReservadaCat()) return true

        if(esPalabraReservadaArray()) return true

        return false

    }
    fun esPalabraReservadaBln():Boolean{
        if(caracterActual == 'b'){
            var lexema = ""
            var filaInicial = filaActual
            var columnaInicial = columnaActual
            var posicionInicial = posicionAcual;

            lexema += caracterActual
            obtenerSiguienteCaracter()

            if(caracterActual == 'l') {
                lexema += caracterActual
                obtenerSiguienteCaracter()

                if(caracterActual == 'n') {
                    lexema += caracterActual
                    obtenerSiguienteCaracter()
                    almacenarToken(lexema, Categoria.PALABRA_RESERVADA, filaInicial, columnaInicial)
                    return true
                }
            }
            restablecerCaracterActual(posicionInicial,filaInicial,columnaInicial)
        }
        return false
    }



    fun esPalabraReservadaCls():Boolean{
        if(caracterActual == 'c'){
            var lexema = ""
            var filaInicial = filaActual
            var columnaInicial = columnaActual
            var posicionInicial = posicionAcual;

            lexema += caracterActual
            obtenerSiguienteCaracter()

            if(caracterActual == 'l') {
                lexema += caracterActual
                obtenerSiguienteCaracter()

                if(caracterActual == 's') {
                    lexema += caracterActual
                    obtenerSiguienteCaracter()
                    almacenarToken(lexema, Categoria.PALABRA_RESERVADA, filaInicial, columnaInicial)
                    return true
                }
            }
            restablecerCaracterActual(posicionInicial,filaInicial,columnaInicial)
        }
        return false
    }

    fun esPalabraReservadaCrt():Boolean{
        if(caracterActual == 'c'){
            var lexema = ""
            var filaInicial = filaActual
            var columnaInicial = columnaActual
            var posicionInicial = posicionAcual;

            lexema += caracterActual
            obtenerSiguienteCaracter()

            if(caracterActual == 'r') {
                lexema += caracterActual
                obtenerSiguienteCaracter()

                if(caracterActual == 't') {
                    lexema += caracterActual
                    obtenerSiguienteCaracter()
                    almacenarToken(lexema, Categoria.PALABRA_RESERVADA, filaInicial, columnaInicial)
                    return true
                }
            }
            restablecerCaracterActual(posicionInicial,filaInicial,columnaInicial)
        }
        return false
    }

    fun esPalabraReservadaMut():Boolean{
        if(caracterActual == 'm'){
            var lexema = ""
            var filaInicial = filaActual
            var columnaInicial = columnaActual
            var posicionInicial = posicionAcual;

            lexema += caracterActual
            obtenerSiguienteCaracter()

            if(caracterActual == 'u') {
                lexema += caracterActual
                obtenerSiguienteCaracter()

                if(caracterActual == 't') {
                    lexema += caracterActual
                    obtenerSiguienteCaracter()
                    almacenarToken(lexema, Categoria.PALABRA_RESERVADA, filaInicial, columnaInicial)
                    return true
                }
            }
            restablecerCaracterActual(posicionInicial,filaInicial,columnaInicial)
        }
        return false
    }

    fun esPalabraReservadaInm():Boolean{
        if(caracterActual == 'i'){
            var lexema = ""
            var filaInicial = filaActual
            var columnaInicial = columnaActual
            var posicionInicial = posicionAcual;

            lexema += caracterActual
            obtenerSiguienteCaracter()

            if(caracterActual == 'n') {
                lexema += caracterActual
                obtenerSiguienteCaracter()

                if(caracterActual == 'm') {
                    lexema += caracterActual
                    obtenerSiguienteCaracter()
                    almacenarToken(lexema, Categoria.PALABRA_RESERVADA, filaInicial, columnaInicial)
                    return true
                }
            }
            restablecerCaracterActual(posicionInicial,filaInicial,columnaInicial)
        }
        return false
    }

    fun esPalabraReservadaWhl():Boolean{
        if(caracterActual == 'w'){
            var lexema = ""
            var filaInicial = filaActual
            var columnaInicial = columnaActual
            var posicionInicial = posicionAcual;

            lexema += caracterActual
            obtenerSiguienteCaracter()

            if(caracterActual == 'h') {
                lexema += caracterActual
                obtenerSiguienteCaracter()

                if(caracterActual == 'l') {
                    lexema += caracterActual
                    obtenerSiguienteCaracter()
                    almacenarToken(lexema, Categoria.PALABRA_RESERVADA, filaInicial, columnaInicial)
                    return true
                }
            }
            restablecerCaracterActual(posicionInicial,filaInicial,columnaInicial)
        }
        return false
    }
    fun esPalabraReservadaRet():Boolean{
        if(caracterActual == 'r'){
            var lexema = ""
            var filaInicial = filaActual
            var columnaInicial = columnaActual
            var posicionInicial = posicionAcual;

            lexema += caracterActual
            obtenerSiguienteCaracter()

            if(caracterActual == 'e') {
                lexema += caracterActual
                obtenerSiguienteCaracter()

                if(caracterActual == 't') {
                    lexema += caracterActual
                    obtenerSiguienteCaracter()
                    almacenarToken(lexema, Categoria.PALABRA_RESERVADA, filaInicial, columnaInicial)
                    return true
                }
            }
            restablecerCaracterActual(posicionInicial,filaInicial,columnaInicial)
        }
        return false
    }
    fun esPalabraReservadaRad():Boolean{
        if(caracterActual == 'r'){
            var lexema = ""
            var filaInicial = filaActual
            var columnaInicial = columnaActual
            var posicionInicial = posicionAcual;

            lexema += caracterActual
            obtenerSiguienteCaracter()

            if(caracterActual == 'a') {
                lexema += caracterActual
                obtenerSiguienteCaracter()

                if(caracterActual == 'd') {
                    lexema += caracterActual
                    obtenerSiguienteCaracter()
                    almacenarToken(lexema, Categoria.PALABRA_RESERVADA, filaInicial, columnaInicial)
                    return true
                }
            }
            restablecerCaracterActual(posicionInicial,filaInicial,columnaInicial)
        }
        return false
    }
    fun esPalabraReservadaTre():Boolean{
        if(caracterActual == 't'){
            var lexema = ""
            var filaInicial = filaActual
            var columnaInicial = columnaActual
            var posicionInicial = posicionAcual;

            lexema += caracterActual
            obtenerSiguienteCaracter()

            if(caracterActual == 'r') {
                lexema += caracterActual
                obtenerSiguienteCaracter()

                if(caracterActual == 'e') {
                    lexema += caracterActual
                    obtenerSiguienteCaracter()
                    almacenarToken(lexema, Categoria.PALABRA_RESERVADA, filaInicial, columnaInicial)
                    return true
                }
            }
            restablecerCaracterActual(posicionInicial,filaInicial,columnaInicial)
        }
        return false
    }
    fun esPalabraReservadaFls():Boolean{
        if(caracterActual == 'f'){
            var lexema = ""
            var filaInicial = filaActual
            var columnaInicial = columnaActual
            var posicionInicial = posicionAcual;

            lexema += caracterActual
            obtenerSiguienteCaracter()

            if(caracterActual == 'l') {
                lexema += caracterActual
                obtenerSiguienteCaracter()

                if(caracterActual == 's') {
                    lexema += caracterActual
                    obtenerSiguienteCaracter()
                    almacenarToken(lexema, Categoria.PALABRA_RESERVADA, filaInicial, columnaInicial)
                    return true
                }
            }
            restablecerCaracterActual(posicionInicial,filaInicial,columnaInicial)
        }
        return false
    }

    fun esPalabraReservadaIf():Boolean{
        if(caracterActual == 'i'){
            var lexema = ""
            var filaInicial = filaActual
            var columnaInicial = columnaActual
            var posicionInicial = posicionAcual;

            lexema += caracterActual
            obtenerSiguienteCaracter()

            if(caracterActual == 'f') {
                lexema += caracterActual
                obtenerSiguienteCaracter()
                almacenarToken(lexema, Categoria.PALABRA_RESERVADA, filaInicial, columnaInicial)
                 return true
                }

            restablecerCaracterActual(posicionInicial,filaInicial,columnaInicial)
        }
        return false
    }
    fun esPalabraReservadaElse():Boolean{
        if(caracterActual == 'e'){
            var lexema = ""
            var filaInicial = filaActual
            var columnaInicial = columnaActual
            var posicionInicial = posicionAcual;

            lexema += caracterActual
            obtenerSiguienteCaracter()

            if(caracterActual == 'l') {
                lexema += caracterActual
                obtenerSiguienteCaracter()

                if (caracterActual == 's') {
                    lexema += caracterActual
                    obtenerSiguienteCaracter()

                    if (caracterActual == 'e') {
                        lexema += caracterActual
                        obtenerSiguienteCaracter()
                        almacenarToken(lexema, Categoria.PALABRA_RESERVADA, filaInicial, columnaInicial)
                        return true
                    }
                }
            }
            restablecerCaracterActual(posicionInicial,filaInicial,columnaInicial)
        }
        return false
    }
    fun esPalabraReservadaVd():Boolean{
        if(caracterActual == 'v'){
            var lexema = ""
            var filaInicial = filaActual
            var columnaInicial = columnaActual
            var posicionInicial = posicionAcual;

            lexema += caracterActual
            obtenerSiguienteCaracter()

            if(caracterActual == 'd') {
                lexema += caracterActual
                obtenerSiguienteCaracter()
                almacenarToken(lexema, Categoria.PALABRA_RESERVADA, filaInicial, columnaInicial)
                return true
            }

            restablecerCaracterActual(posicionInicial,filaInicial,columnaInicial)
        }
        return false
    }

    fun esPalabraReservadaImp():Boolean{
        if(caracterActual == 'i'){
            var lexema = ""
            var filaInicial = filaActual
            var columnaInicial = columnaActual
            var posicionInicial = posicionAcual;

            lexema += caracterActual
            obtenerSiguienteCaracter()

            if(caracterActual == 'm') {
                lexema += caracterActual
                obtenerSiguienteCaracter()

                if(caracterActual == 'p') {
                    lexema += caracterActual
                    obtenerSiguienteCaracter()
                    almacenarToken(lexema, Categoria.PALABRA_RESERVADA, filaInicial, columnaInicial)
                    return true
                }
            }
            restablecerCaracterActual(posicionInicial,filaInicial,columnaInicial)
        }
        return false
    }

    fun esPalabraReservadaCat():Boolean{
        if(caracterActual == 'c'){
            var lexema = ""
            var filaInicial = filaActual
            var columnaInicial = columnaActual
            var posicionInicial = posicionAcual;

            lexema += caracterActual
            obtenerSiguienteCaracter()

            if(caracterActual == 'a') {
                lexema += caracterActual
                obtenerSiguienteCaracter()

                if(caracterActual == 't') {
                    lexema += caracterActual
                    obtenerSiguienteCaracter()
                    almacenarToken(lexema, Categoria.PALABRA_RESERVADA, filaInicial, columnaInicial)
                    return true
                }
            }
            restablecerCaracterActual(posicionInicial,filaInicial,columnaInicial)
        }
        return false
    }

    fun esPalabraReservadaArray():Boolean {
        if (caracterActual == 'a') {
            var lexema = ""
            var filaInicial = filaActual
            var columnaInicial = columnaActual
            var posicionInicial = posicionAcual;

            lexema += caracterActual
            obtenerSiguienteCaracter()

            if (caracterActual == 'r') {
                lexema += caracterActual
                obtenerSiguienteCaracter()

                if (caracterActual == 'r') {
                    lexema += caracterActual
                    obtenerSiguienteCaracter()

                    if (caracterActual == 'a') {
                        lexema += caracterActual
                        obtenerSiguienteCaracter()

                        if (caracterActual == 'y') {
                            lexema += caracterActual
                            obtenerSiguienteCaracter()
                            almacenarToken(lexema, Categoria.PALABRA_RESERVADA, filaInicial, columnaInicial)
                            return true

                        }
                    }
                }
            }

            restablecerCaracterActual(posicionInicial, filaInicial, columnaInicial)
        }

        return false
    }

        fun esPalabraReservadaEnt():Boolean{
        if(caracterActual == 'e'){
            var lexema = ""
            var filaInicial = filaActual
            var columnaInicial = columnaActual
            var posicionInicial = posicionAcual;

            lexema += caracterActual
            obtenerSiguienteCaracter()

            if(caracterActual == 'n') {
                lexema += caracterActual
                obtenerSiguienteCaracter()

                if(caracterActual == 't') {
                    lexema += caracterActual
                    obtenerSiguienteCaracter()
                    almacenarToken(lexema, Categoria.PALABRA_RESERVADA, filaInicial, columnaInicial)
                    return true
                }
            }
            restablecerCaracterActual(posicionInicial,filaInicial,columnaInicial)
        }
        return false
    }
    fun esPalabraReservadaDbe():Boolean{
        if(caracterActual == 'd'){
            var lexema = ""
            var filaInicial = filaActual
            var columnaInicial = columnaActual
            var posicionInicial = posicionAcual;

            lexema += caracterActual
            obtenerSiguienteCaracter()

            if(caracterActual == 'b') {
                lexema += caracterActual
                obtenerSiguienteCaracter()

                if(caracterActual == 'e') {
                    lexema += caracterActual
                    obtenerSiguienteCaracter()
                    almacenarToken(lexema, Categoria.PALABRA_RESERVADA, filaInicial, columnaInicial)
                    return true
                }
            }
            restablecerCaracterActual(posicionInicial,filaInicial,columnaInicial)
        }
        return false
    }

    fun esPalabraReservadaStr():Boolean{
        if(caracterActual == 's'){
            var lexema = ""
            var filaInicial = filaActual
            var columnaInicial = columnaActual
            var posicionInicial = posicionAcual;

            lexema += caracterActual
            obtenerSiguienteCaracter()

            if(caracterActual == 't') {
                lexema += caracterActual
                obtenerSiguienteCaracter()

                if(caracterActual == 'r') {
                    lexema += caracterActual
                    obtenerSiguienteCaracter()
                    almacenarToken(lexema, Categoria.PALABRA_RESERVADA, filaInicial, columnaInicial)
                    return true
                }
            }
            restablecerCaracterActual(posicionInicial,filaInicial,columnaInicial)
        }
        return false
    }

    fun esPalabraReservadaPub():Boolean{
        if(caracterActual == 'p'){
            var lexema = ""
            var filaInicial = filaActual
            var columnaInicial = columnaActual
            var posicionInicial = posicionAcual;

            lexema += caracterActual
            obtenerSiguienteCaracter()

            if(caracterActual == 'u') {
                lexema += caracterActual
                obtenerSiguienteCaracter()

                if(caracterActual == 'b') {
                    lexema += caracterActual
                    obtenerSiguienteCaracter()
                    almacenarToken(lexema, Categoria.PALABRA_RESERVADA, filaInicial, columnaInicial)
                    return true
                }
            }
            restablecerCaracterActual(posicionInicial,filaInicial,columnaInicial)
        }
        return false
    }

    fun esPalabraReservadaPvt():Boolean{
        if(caracterActual == 'p'){
            var lexema = ""
            var filaInicial = filaActual
            var columnaInicial = columnaActual
            var posicionInicial = posicionAcual;

            lexema += caracterActual
            obtenerSiguienteCaracter()

            if(caracterActual == 'v') {
                lexema += caracterActual
                obtenerSiguienteCaracter()

                if(caracterActual == 't') {
                    lexema += caracterActual
                    obtenerSiguienteCaracter()
                    almacenarToken(lexema, Categoria.PALABRA_RESERVADA, filaInicial, columnaInicial)
                    return true
                }
            }
            restablecerCaracterActual(posicionInicial,filaInicial,columnaInicial)
        }
        return false
    }

    fun esPalabraReservadaAbs():Boolean{
        if(caracterActual == 'a'){
            var lexema = ""
            var filaInicial = filaActual
            var columnaInicial = columnaActual
            var posicionInicial = posicionAcual;

            lexema += caracterActual
            obtenerSiguienteCaracter()

            if(caracterActual == 'b') {
                lexema += caracterActual
                obtenerSiguienteCaracter()

                if(caracterActual == 's') {
                    lexema += caracterActual
                    obtenerSiguienteCaracter()
                    almacenarToken(lexema, Categoria.PALABRA_RESERVADA, filaInicial, columnaInicial)
                    return true
                }
            }
            restablecerCaracterActual(posicionInicial,filaInicial,columnaInicial)
        }
        return false
    }


    fun esPalabraReservadaBrk():Boolean{
        if(caracterActual == 'b'){
            var lexema = ""
            var filaInicial = filaActual
            var columnaInicial = columnaActual
            var posicionInicial = posicionAcual;

            lexema += caracterActual
            obtenerSiguienteCaracter()

            if(caracterActual == 'r') {
                lexema += caracterActual
                obtenerSiguienteCaracter()

                if(caracterActual == 'k') {
                    lexema += caracterActual
                    obtenerSiguienteCaracter()
                    almacenarToken(lexema, Categoria.PALABRA_RESERVADA, filaInicial, columnaInicial)
                    return true
                }
            }
            restablecerCaracterActual(posicionInicial,filaInicial,columnaInicial)
        }
        return false
    }

    fun esPalabraReservadaCas():Boolean{
        if(caracterActual == 'c'){
            var lexema = ""
            var filaInicial = filaActual
            var columnaInicial = columnaActual
            var posicionInicial = posicionAcual;

            lexema += caracterActual
            obtenerSiguienteCaracter()

            if(caracterActual == 'a') {
                lexema += caracterActual
                obtenerSiguienteCaracter()

                if(caracterActual == 's') {
                    lexema += caracterActual
                    obtenerSiguienteCaracter()
                    almacenarToken(lexema, Categoria.PALABRA_RESERVADA, filaInicial, columnaInicial)
                    return true
                }
            }
            restablecerCaracterActual(posicionInicial,filaInicial,columnaInicial)
        }
        return false
    }

    fun esPalabraReservadaCth():Boolean{
        if(caracterActual == 'c'){
            var lexema = ""
            var filaInicial = filaActual
            var columnaInicial = columnaActual
            var posicionInicial = posicionAcual;

            lexema += caracterActual
            obtenerSiguienteCaracter()

            if(caracterActual == 't') {
                lexema += caracterActual
                obtenerSiguienteCaracter()

                if(caracterActual == 'h') {
                    lexema += caracterActual
                    obtenerSiguienteCaracter()
                    almacenarToken(lexema, Categoria.PALABRA_RESERVADA, filaInicial, columnaInicial)
                    return true
                }
            }
            restablecerCaracterActual(posicionInicial,filaInicial,columnaInicial)
        }
        return false
    }

    fun esPalabraReservadaPtt():Boolean{
        if(caracterActual == 'p'){
            var lexema = ""
            var filaInicial = filaActual
            var columnaInicial = columnaActual
            var posicionInicial = posicionAcual;

            lexema += caracterActual
            obtenerSiguienteCaracter()

            if(caracterActual == 't') {
                lexema += caracterActual
                obtenerSiguienteCaracter()

                if(caracterActual == 't') {
                    lexema += caracterActual
                    obtenerSiguienteCaracter()
                    almacenarToken(lexema, Categoria.PALABRA_RESERVADA, filaInicial, columnaInicial)
                    return true
                }
            }
            restablecerCaracterActual(posicionInicial,filaInicial,columnaInicial)
        }
        return false
    }

    fun esEntero():Boolean{
        if(caracterActual == '#'){
            var lexema = ""
            var filaInicial = filaActual
            var columnaInicial = columnaActual
            var posicionInicial = posicionAcual;

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

        var posicion = posicionAcual;

        if(caracterActual == '@'){
            var lexema = ""
            var filaInicial = filaActual
            var columnaInicial = columnaActual
            var posicionInicial = posicionAcual;

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
        var posicion = posicionAcual;
        if(caracterActual == '^'){
            var lexema = ""
            var filaInicial = filaActual
            var columnaInicial = columnaActual
            var posicionInicial = posicionAcual;

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

    fun esOperadorLogico():Boolean{

        if(esOperadorLogicoNegacion()) return true

        if(esOperadorLogicoSuma()) return true

        if(esOperadorLogicoSumaExclusiva()) return true

        if(esOperadorLogicoProducto()) return true

        return false
    }

    fun esOperadorLogicoNegacion():Boolean{
        if(caracterActual == 'n'){
            var lexema = ""
            var filaInicial = filaActual
            var columnaInicial = columnaActual
            var posicionInicial = posicionAcual;

            lexema += caracterActual
            obtenerSiguienteCaracter()

            if(caracterActual == 'o') {
                lexema += caracterActual
                obtenerSiguienteCaracter()

                if(caracterActual == 't') {
                    lexema += caracterActual
                    obtenerSiguienteCaracter()
                    almacenarToken(lexema, Categoria.OPERADOR_LOGICO, filaInicial, columnaInicial)
                    return true
                }
            }
            restablecerCaracterActual(posicionInicial,filaInicial,columnaInicial)
        }
        return false
    }

    fun esOperadorLogicoSuma():Boolean{
        if(caracterActual == 'o'){
            var lexema = ""
            var filaInicial = filaActual
            var columnaInicial = columnaActual
            var posicionInicial = posicionAcual;

            lexema += caracterActual
            obtenerSiguienteCaracter()

            if(caracterActual == 'r') {
                lexema += caracterActual
                obtenerSiguienteCaracter()
                almacenarToken(lexema, Categoria.OPERADOR_LOGICO, filaInicial, columnaInicial)
                return true
            }
            restablecerCaracterActual(posicionInicial,filaInicial,columnaInicial)
        }
        return false
    }

    fun esOperadorLogicoSumaExclusiva():Boolean{
        if(caracterActual == 'x'){
            var lexema = ""
            var filaInicial = filaActual
            var columnaInicial = columnaActual
            var posicionInicial = posicionAcual;

            lexema += caracterActual
            obtenerSiguienteCaracter()

            if(caracterActual == 'o') {
                lexema += caracterActual
                obtenerSiguienteCaracter()

                if(caracterActual == 'r') {
                    lexema += caracterActual
                    obtenerSiguienteCaracter()
                    almacenarToken(lexema, Categoria.OPERADOR_LOGICO, filaInicial, columnaInicial)
                    return true
                }
            }
            restablecerCaracterActual(posicionInicial,filaInicial,columnaInicial)
        }
        return false
    }

    fun esOperadorLogicoProducto():Boolean{
        if(caracterActual == 'a'){
            var lexema = ""
            var filaInicial = filaActual
            var columnaInicial = columnaActual
            var posicionInicial = posicionAcual;

            lexema += caracterActual
            obtenerSiguienteCaracter()

            if(caracterActual == 'n') {
                lexema += caracterActual
                obtenerSiguienteCaracter()

                if(caracterActual == 'd') {
                    lexema += caracterActual
                    obtenerSiguienteCaracter()
                    almacenarToken(lexema, Categoria.OPERADOR_LOGICO, filaInicial, columnaInicial)
                    return true
                }
            }
            restablecerCaracterActual(posicionInicial,filaInicial,columnaInicial)
        }
        return false
    }

    fun esOperadorIncremento():Boolean{
        if(caracterActual == '+'){
            var lexema = ""
            var filaInicial = filaActual
            var columnaInicial = columnaActual
            var posicionInicial = posicionAcual;

            lexema += caracterActual
            obtenerSiguienteCaracter()

            if(caracterActual == '+') {
                lexema += caracterActual
                obtenerSiguienteCaracter()
                almacenarToken(lexema, Categoria.OPERADOR_INCREMETO, filaInicial, columnaInicial)
                return true
            }
            restablecerCaracterActual(posicionInicial,filaInicial,columnaInicial)
        }
        return false
    }

    fun esOperadorDecremento():Boolean{
        if(caracterActual == '-'){
            var lexema = ""
            var filaInicial = filaActual
            var columnaInicial = columnaActual
            var posicionInicial = posicionAcual;

            lexema += caracterActual
            obtenerSiguienteCaracter()

            if(caracterActual == '-') {
                lexema += caracterActual
                obtenerSiguienteCaracter()
                almacenarToken(lexema, Categoria.OPERADOR_DECREMENTO, filaInicial, columnaInicial)
                return true
            }
            restablecerCaracterActual(posicionInicial,filaInicial,columnaInicial)
        }
        return false
    }

    fun esOperadorAritmetico():Boolean{
        if(caracterActual == '-' || caracterActual == '+' || caracterActual == '*'
            || caracterActual == '/' || caracterActual == '%'){
            var lexema = ""
            var filaInicial = filaActual
            var columnaInicial = columnaActual

            lexema += caracterActual
            obtenerSiguienteCaracter()
            almacenarToken(lexema, Categoria.OPERADOR_ARITMETICO, filaInicial, columnaInicial)
            return true
        }
        return false
    }

    fun esOperadorRelacional():Boolean{

        if(esOperadorRelacionalIgualQue()) return true

        if(esOperadorRelacionalDistintoQue()) return true

        if(esOperadorRelacionalMenorMayorQue()) return true

        return false
    }

    fun esOperadorRelacionalIgualQue():Boolean{
        if(caracterActual == '='){
            var lexema = ""
            var filaInicial = filaActual
            var columnaInicial = columnaActual
            var posicionInicial = posicionAcual;

            lexema += caracterActual
            obtenerSiguienteCaracter()

            if(caracterActual == '=') {
                lexema += caracterActual
                obtenerSiguienteCaracter()
                almacenarToken(lexema, Categoria.OPERADOR_RELACIONAL, filaInicial, columnaInicial)
                return true
            }
            restablecerCaracterActual(posicionInicial,filaInicial,columnaInicial)
        }
        return false
    }

    fun esOperadorRelacionalDistintoQue():Boolean{
        if(caracterActual == '!'){
            var lexema = ""
            var filaInicial = filaActual
            var columnaInicial = columnaActual
            var posicionInicial = posicionAcual;

            lexema += caracterActual
            obtenerSiguienteCaracter()

            if(caracterActual == '=') {
                lexema += caracterActual
                obtenerSiguienteCaracter()
                almacenarToken(lexema, Categoria.OPERADOR_RELACIONAL, filaInicial, columnaInicial)
                return true
            }
            restablecerCaracterActual(posicionInicial,filaInicial,columnaInicial)
        }
        return false
    }

    fun esOperadorRelacionalMenorMayorQue():Boolean{
        if(caracterActual == '<' || caracterActual == '>'){
            var lexema = ""
            var filaInicial = filaActual
            var columnaInicial = columnaActual

            lexema += caracterActual
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

        if(caracterActual == '-' || caracterActual == '+' || caracterActual == '*'
            || caracterActual == '/' || caracterActual == '%'){
            var lexema = ""
            var filaInicial = filaActual
            var columnaInicial = columnaActual
            var posicionInicial = posicionAcual;

            lexema += caracterActual
            obtenerSiguienteCaracter()

            if(caracterActual == '=') {
                lexema += caracterActual
                obtenerSiguienteCaracter()
                almacenarToken(lexema, Categoria.OPERADOR_ASIGNACION, filaInicial, columnaInicial)
                return true
            }
            restablecerCaracterActual(posicionInicial,filaInicial,columnaInicial)
        }else if(caracterActual == '='){
            var lexema = ""
            var filaInicial = filaActual
            var columnaInicial = columnaActual

            lexema += caracterActual
            obtenerSiguienteCaracter()
            almacenarToken(lexema, Categoria.OPERADOR_ASIGNACION, filaInicial, columnaInicial)
            return true
        }
        return false
    }
    fun esParentesisIzquierdo() : Boolean{

        if (caracterActual == '(') {
            var lexema = ""
            val filaInicial = filaActual
            val columnaInicial = columnaActual
            val posicionInicial = posicionAcual

            lexema += caracterActual
            obtenerSiguienteCaracter()

            almacenarToken(lexema, Categoria.PARENTESIS_IZQUIERDO, filaInicial, columnaInicial)
            return true
        }

        return false
    }
    fun esParantesisDerecho() : Boolean{

        if (caracterActual == ')') {
            var lexema = ""
            val filaInicial = filaActual
            val columnaInicial = columnaActual
            val posicionInicial = posicionAcual

            lexema += caracterActual
            obtenerSiguienteCaracter()

            almacenarToken(lexema, Categoria.PARENTESIS_DERECHO, filaInicial, columnaInicial)
            return true
        }

        return false
    }
    fun esLlavesIzquierda() : Boolean{

        if (caracterActual == '{') {
            var lexema = ""
            val filaInicial = filaActual
            val columnaInicial = columnaActual
            val posicionInicial = posicionAcual

            lexema += caracterActual
            obtenerSiguienteCaracter()

            almacenarToken(lexema, Categoria.LLAVE_IZQUIERDO, filaInicial, columnaInicial)
            return true
        }

        return false
    }

    fun esLlavesDerecha() : Boolean{

        if (caracterActual == '}') {
            var lexema = ""
            val filaInicial = filaActual
            val columnaInicial = columnaActual
            val posicionInicial = posicionAcual

            lexema += caracterActual
            obtenerSiguienteCaracter()

            almacenarToken(lexema, Categoria.LLAVE_DERECHO, filaInicial, columnaInicial)
            return true
        }

        return false
    }

    fun esCorcheteDerecho() : Boolean{

        if (caracterActual == ']') {
            var lexema = ""
            val filaInicial = filaActual
            val columnaInicial = columnaActual
            val posicionInicial = posicionAcual

            lexema += caracterActual
            obtenerSiguienteCaracter()

            almacenarToken(lexema, Categoria.CORCHETE_DERECHO, filaInicial, columnaInicial)
            return true
        }

        return false
    }

    fun esCorcheteIzquierdo() : Boolean{

        if (caracterActual == '[') {
            var lexema = ""
            val filaInicial = filaActual
            val columnaInicial = columnaActual
            val posicionInicial = posicionAcual

            lexema += caracterActual
            obtenerSiguienteCaracter()

            almacenarToken(lexema, Categoria.CORCHETE_IZQUIERDO, filaInicial, columnaInicial)
            return true
        }

        return false
    }

    fun esFinSentencia():Boolean{
        if(caracterActual == '|'){
            var lexema = ""
            var filaInicial = filaActual
            var columnaInicial = columnaActual

            lexema += caracterActual
            obtenerSiguienteCaracter()
            almacenarToken(lexema, Categoria.FIN_SENTENCIA, filaInicial, columnaInicial)
            return true
        }
        return false
    }

    fun esPunto():Boolean{
        if(caracterActual == '_'){
            var lexema = ""
            var filaInicial = filaActual
            var columnaInicial = columnaActual

            lexema += caracterActual
            obtenerSiguienteCaracter()
            almacenarToken(lexema, Categoria.PUNTO, filaInicial, columnaInicial)
            return true
        }
        return false
    }

    fun esDosPuntos():Boolean{
        if(caracterActual == ';'){
            var lexema = ""
            var filaInicial = filaActual
            var columnaInicial = columnaActual

            lexema += caracterActual
            obtenerSiguienteCaracter()
            almacenarToken(lexema, Categoria.DOS_PUNTOS, filaInicial, columnaInicial)
            return true
        }
        return false
    }

    fun esSeparador():Boolean{
        if(caracterActual == ':'){
            var lexema = ""
            var filaInicial = filaActual
            var columnaInicial = columnaActual

            lexema += caracterActual
            obtenerSiguienteCaracter()
            almacenarToken(lexema, Categoria.SEPARADOR, filaInicial, columnaInicial)
            return true
        }
        return false
    }

    fun esComentarioLinea():Boolean{
        if(caracterActual == '&'){
            var lexema = ""
            var filaInicial = filaActual
            var columnaInicial = columnaActual
            var posicionInicial = posicionAcual;

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
            var filaInicial = filaActual
            var columnaInicial = columnaActual
            var posicionInicial = posicionAcual;

            lexema += caracterActual
            obtenerSiguienteCaracter()

            if(caracterActual == '"') {
                lexema += caracterActual
                obtenerSiguienteCaracter()

                if(caracterActual == '"') {
                    lexema += caracterActual
                    obtenerSiguienteCaracter()
                    var lista = ArrayList<Char>();
                    var bandera = true;

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
        var posicion = posicionAcual;
        if(caracterActual == '°'){
            var lexema = ""
            var filaInicial = filaActual
            var columnaInicial = columnaActual
            var posicionInicial = posicionAcual;

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
                    restablecerCaracterActual(posicionInicial,filaInicial,columnaInicial)
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
        var posicion = posicionAcual;
        if(caracterActual == '\''){
            var lexema = ""
            var filaInicial = filaActual
            var columnaInicial = columnaActual
            var posicionInicial = posicionAcual;

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
                        return false;
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
                almacenarToken(lexema, Categoria.CADENA_CARCTERES, filaInicial, columnaInicial)
                return true
            }

            restablecerCaracterActual(posicionInicial,filaInicial,columnaInicial)
        }
        return false
    }

    fun obtenerSiguienteCaracter(){
        if(posicionAcual == codigoFuente.length - 1){
            caracterActual = finCodigo
        }else{
            if(caracterActual == '\n'){
                filaActual++
                columnaActual = 1
            }else{
                columnaActual++
            }

            posicionAcual++
            caracterActual = codigoFuente[posicionAcual]
        }
    }

    fun restablecerCaracterActual(posicionInicial:Int, filaInicial:Int, columnaInicial:Int){
        filaActual = filaInicial
        columnaActual = columnaInicial
        posicionAcual = posicionInicial
        caracterActual = codigoFuente[posicionAcual]
    }

}