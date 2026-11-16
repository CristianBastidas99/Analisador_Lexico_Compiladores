package co.edu.uniquindio.compiladores

import co.edu.uniquindio.compiladores.lexico.AnalizadorLexico
import co.edu.uniquindio.compiladores.sintaxis.AnalizadorSintactico
import co.edu.uniquindio.compiladores.sintaxis.UnidadDeCompilacion
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import co.edu.uniquindio.compiladores.sintaxis.*

/**
 * Los errores sintacticos no deben incluir ruido de alternativas descartadas (retroceso):
 * programa valido -> sin errores; programa invalido -> errores reales, con posicion y sin duplicados.
 */
class SintacticoErroresTest {

    private class Analisis(val unidad: UnidadDeCompilacion?, val sintactico: AnalizadorSintactico) {
        val errores get() = sintactico.listaErrores
    }

    private fun analizar(fuente: String): Analisis {
        val lexico = AnalizadorLexico(fuente)
        lexico.analizar()
        val sintactico = AnalizadorSintactico(lexico.listaTokens)
        return Analisis(sintactico.esUnidadDeCompilacion(), sintactico)
    }

    private fun assertValido(fuente: String) {
        val a = analizar(fuente)
        assertNotNull(a.unidad, "No se construyo la unidad. Errores: ${a.errores}")
        assertTrue(a.errores.isEmpty(), "Un programa valido no debe tener errores: ${a.errores}")
    }

    private fun assertSinDuplicados(a: Analisis) {
        val claves = a.errores.map { Triple(it.error, it.fila, it.columna) }
        assertEquals(claves.distinct(), claves, "Errores duplicados: ${a.errores}")
    }

    private val funcionConRetorno = "pub cls ^calc {\n" +
            "pvt ent ^total |\n" +
            "pub str ^titulo |\n" +
            "pub ent ^sumar ( ent ^a : ent ^b ) {\n" +
            "ent ^res |\n" +
            "^res = ^a |\n" +
            "^res += ^b |\n" +
            "imp ( ^res ) |\n" +
            "ret ^res |\n" +
            "}\n" +
            "pub vd ^main ( ) {\n" +
            "ent ^x = #3 |\n" +
            "ent ^y = #4 |\n" +
            "^sumar ( ^x : ^y ) |\n" +
            "^total = ^sumar ( ^x : ^y ) |\n" +
            "imp ( ^total ) |\n" +
            "}\n" +
            "}"

    private val flujo = "pvt cls ^flujo {\n" +
            "pvt vd ^main ( ) {\n" +
            "ent ^i |\n" +
            "^i = #5 |\n" +
            "^i ++ |\n" +
            "^i ++ |\n" +
            "^i -- |\n" +
            "if ( ( ( #3 ) > ( #2 ) ) and ( ( #1 ) <= ( #1 ) ) ) {\n" +
            "imp ( 'A' ) |\n" +
            "} else {\n" +
            "imp ( 'B' ) |\n" +
            "}\n" +
            "if ( not ( ( #1 ) > ( #2 ) ) ) {\n" +
            "imp ( 'C' ) |\n" +
            "}\n" +
            "if ( ( ( #1 ) >= ( #2 ) ) or ( ( #1 ) != ( #1 ) ) ) {\n" +
            "imp ( 'D' ) |\n" +
            "} else {\n" +
            "imp ( 'E' ) |\n" +
            "}\n" +
            "if ( ( ( #1 ) < ( #2 ) ) xor ( ( #1 ) < ( #2 ) ) ) {\n" +
            "imp ( 'F' ) |\n" +
            "} else {\n" +
            "imp ( 'G' ) |\n" +
            "}\n" +
            "whl ( ( ( #1 ) > ( #2 ) ) ) {\n" +
            "imp ( 'nunca' ) |\n" +
            "^i ++ |\n" +
            "}\n" +
            "imp ( ^i ) |\n" +
            "imp ( ( #2 + ( #3 * ( #4 ) ) ) > ( #13 ) ) |\n" +
            "}\n" +
            "}"

    private fun tipos(conCrt: Boolean): String {
        val crtMut = if (conCrt) "crt ^e |\n^e = °y° |\n" else ""
        val crtInm = if (conCrt) "crt ^j = °x° |\n" else ""
        val crtImp = if (conCrt) "imp ( ^e ) |\nimp ( ^j ) |\n" else ""
        return "pub cls ^tipos {\n" +
                "pub vd ^main ( ) {\n" +
                "ent ^a |\ndbe ^b |\nstr ^c |\nbln ^d |\n" +
                "^a = #7 |\n^b = @2.5 |\n^c = 'ab' |\n^d = ( #1 ) <= ( #1 ) |\n" +
                crtMut +
                "ent ^f = #5 |\ndbe ^g = @1.5 |\nstr ^h = 'hola' |\n" +
                "bln ^i = ( ( #1 ) < ( #2 ) ) and ( ( #2 ) < ( #3 ) ) |\n" +
                crtInm +
                "imp ( ^a ) |\nimp ( ^b ) |\nimp ( ^c ) |\nimp ( ^d ) |\n" +
                "imp ( ^f ) |\nimp ( ^g ) |\nimp ( ^h ) |\nimp ( ^i ) |\n" +
                crtImp +
                "}\n}"
    }

    private val lectura = "pub cls ^lee {\n" +
            "pub vd ^main ( ) {\n" +
            "str ^nombre |\n" +
            "^nombre = rad ( 'Escriba su nombre' ) |\n" +
            "str ^otro = rad ( 'Otro dato' ) |\n" +
            "imp ( ^nombre ) |\n" +
            "}\n" +
            "}"

    private val borde = "pub cls ^borde {\n" +
            "pub ent ^class ( ent ^int ) {\n" +
            "ent ^new = #1 |\n" +
            "if ( ( ( #1 ) <= ( #1 ) ) ) {\n" +
            "ent ^new = #2 |\n" +
            "imp ( ^new ) |\n" +
            "}\n" +
            "ent ^q = #7 |\n" +
            "if ( ( ( #1 ) <= ( #1 ) ) ) {\n" +
            "ent ^q = ^q |\n" +
            "imp ( ^q ) |\n" +
            "}\n" +
            "ret ^int |\n" +
            "}\n" +
            "pub ent ^buscar ( ) {\n" +
            "whl ( ( ( #2 ) > ( #1 ) ) ) {\n" +
            "ret #9 |\n" +
            "}\n" +
            "}\n" +
            "pub vd ^main ( ) {\n" +
            "ent ^v = #4 |\n" +
            "ent ^w |\n" +
            "^w = ^class ( ^v ) |\n" +
            "imp ( ^w ) |\n" +
            "imp ( 'a\\\\b' + #5 + @1.5 + °z° ) |\n" +
            "imp ( °a° + °b° ) |\n" +
            "}\n" +
            "}"

    @Test
    fun programasValidosNoTienenErrores() {
        assertValido("pub cls ^hola {\npub vd ^main ( ) {\nimp ( 'Hola mundo' ) |\n}\n}")
        assertValido(funcionConRetorno)
        assertValido(funcionConRetorno.replace("^total = ^sumar ( ^x : ^y ) |\n", "").replace("imp ( ^total ) |\n", ""))
        assertValido(flujo)
        assertValido(tipos(false))
        assertValido(tipos(true))
        assertValido(lectura)
        assertValido(borde)
    }

    @Test
    fun faltaClsDevuelveNuloConError() {
        val a = analizar("pub ^a { }")
        assertNull(a.unidad)
        assertEquals(1, a.errores.size, "${a.errores}")
        val e = a.errores[0]
        assertEquals("Falta palabra reservada cls", e.error)
        assertEquals(1, e.fila)
        assertEquals(5, e.columna)
    }

    @Test
    fun faltaFinDeSentencia() {
        val a = analizar("pub cls ^a {\npub vd ^main ( ) {\nimp ( 'x' )\n}\n}")
        assertNull(a.unidad, "${a.errores}")
        assertEquals(1, a.errores.size, "${a.errores}")
        assertEquals("Falta el fin de sentencia", a.errores[0].error)
        assertEquals(4, a.errores[0].fila)
        assertEquals(1, a.errores[0].columna)
        assertSinDuplicados(a)
    }

    @Test
    fun parentesisSinCerrarEnIf() {
        val a = analizar("pub cls ^a {\npub vd ^main ( ) {\nif ( ( ( #1 ) <= ( #1 ) ) {\nimp ( 'x' ) |\n}\n}\n}")
        assertNull(a.unidad, "${a.errores}")
        assertEquals(1, a.errores.size, "${a.errores}")
        assertEquals("Debe definir el paréntesis derecho de la decisión", a.errores[0].error)
        assertEquals(3, a.errores[0].fila)
        assertSinDuplicados(a)
    }

    @Test
    fun funcionSinTipoDeRetorno() {
        val a = analizar("pub cls ^a {\npub ^main ( ) {\nimp ( 'x' ) |\n}\n}")
        assertNull(a.unidad, "${a.errores}")
        assertEquals(1, a.errores.size, "${a.errores}")
        assertEquals("Debe definir el valor retornado de la función", a.errores[0].error)
        assertEquals(2, a.errores[0].fila)
        assertEquals(5, a.errores[0].columna)
        assertSinDuplicados(a)
    }

    @Test
    fun llaveSinCerrar() {
        val a = analizar("pub cls ^a {\npub vd ^main ( ) {\nimp ( 'x' ) |\n}")
        assertNull(a.unidad, "${a.errores}")
        assertEquals(1, a.errores.size, "${a.errores}")
        assertEquals("Falta la llave derecha", a.errores[0].error)
        assertEquals(2, a.errores[0].columna)
        assertEquals(4, a.errores[0].fila)
        assertSinDuplicados(a)
    }

    @Test
    fun codigoDespuesDeLaClase() {
        val a = analizar("pub cls ^a {\n}\nimp ( 'x' ) |")
        assertEquals(1, a.errores.size, "${a.errores}")
        assertEquals("Hay código después de la llave que cierra la clase", a.errores[0].error)
        assertEquals(3, a.errores[0].fila)
        assertEquals(1, a.errores[0].columna)
    }

    // ---------------------------------------------------------------------------------------------
    // Programas validos: uno por cada tipo de sentencia, comprobando el AST
    // ---------------------------------------------------------------------------------------------

    /** Envuelve un cuerpo en una clase con un main: el cuerpo empieza en la fila 3, columna 1 */
    private fun enMain(cuerpo: String) = "pub cls ^a {\npub vd ^main ( ) {\n$cuerpo\n}\n}"

    private val condicion = "( ( ( #1 ) > ( #2 ) ) )"

    /** Analiza el cuerpo (valido), y devuelve las sentencias del unico main */
    private fun sentencias(cuerpo: String): List<Sentencia> {
        val fuente = enMain(cuerpo)
        assertValido(fuente)
        val unidad = analizar(fuente).unidad!!
        assertEquals(1, unidad.listaFunciones.size)
        return unidad.listaFunciones[0].listaSentencias
    }

    @Test
    fun programaMinimoValido() {
        val a = analizar("pub cls ^a {\n}")
        assertNotNull(a.unidad, "${a.errores}")
        assertTrue(a.errores.isEmpty(), "${a.errores}")
        assertEquals("pub", a.unidad!!.visibilidad.lexema)
        assertEquals("^a", a.unidad!!.nombre.lexema)
        assertTrue(a.unidad!!.listaVariablesGlobales.isEmpty())
        assertTrue(a.unidad!!.listaFunciones.isEmpty())
    }

    @Test
    fun variablesGlobalesYFuncionesEnElAst() {
        val fuente = "pvt cls ^b {\npub ent ^x |\npvt str ^y |\npub ent ^f ( ent ^p : dbe ^q ) {\nret ^p |\n}\npub vd ^main ( ) {\n}\n}"
        assertValido(fuente)
        val unidad = analizar(fuente).unidad!!
        assertEquals("pvt", unidad.visibilidad.lexema)
        assertEquals(2, unidad.listaVariablesGlobales.size)
        assertEquals("^x", unidad.listaVariablesGlobales[0].nombre.lexema)
        assertEquals("str", unidad.listaVariablesGlobales[1].tipo.lexema)
        assertEquals(2, unidad.listaFunciones.size)
        val f = unidad.listaFunciones[0]
        assertEquals("ent", f.valorRetornado.lexema)
        assertEquals(2, f.listaParametrosConTipo.size)
        assertEquals(1, f.listaSentencias.size)
        assertTrue(f.listaSentencias[0] is Retorno)
        assertEquals("vd", unidad.listaFunciones[1].valorRetornado.lexema)
    }

    @Test
    fun decisionSinElse() {
        val s = sentencias("if $condicion {\nimp ( 'a' ) |\n}")
        assertEquals(1, s.size)
        val d = s[0] as Decision
        assertEquals(1, d.listaSentenciasIf.size)
        assertTrue(d.listaSentenciasIf[0] is Impresion)
        assertTrue(d.listaSentenciasElse.isEmpty())
    }

    @Test
    fun decisionConElse() {
        val s = sentencias("if $condicion {\nimp ( 'a' ) |\n} else {\nimp ( 'b' ) |\nimp ( 'c' ) |\n}")
        assertEquals(1, s.size)
        val d = s[0] as Decision
        assertEquals(1, d.listaSentenciasIf.size)
        assertEquals(2, d.listaSentenciasElse.size)
    }

    @Test
    fun cicloValido() {
        val s = sentencias("whl $condicion {\nimp ( 'a' ) |\n^i ++ |\n}")
        assertEquals(1, s.size)
        val c = s[0] as Ciclo
        assertEquals(2, c.listaSentencias.size)
        assertTrue(c.listaSentencias[0] is Impresion)
        assertTrue(c.listaSentencias[1] is IncrDecr)
    }

    @Test
    fun declaracionMutable() {
        val s = sentencias("ent ^x |")
        assertEquals(1, s.size)
        val d = s[0] as DeclaracionVariableMut
        assertEquals("ent", d.tipo.lexema)
        assertEquals("^x", d.identificador.lexema)
    }

    @Test
    fun declaracionInmutable() {
        val s = sentencias("ent ^x = #5 |")
        assertEquals(1, s.size)
        val d = s[0] as DeclaracionVariableInm
        assertEquals("ent", d.tipo.lexema)
        assertEquals("^x", d.asignacion.identificador.lexema)
        assertEquals("=", d.asignacion.operAsignacion.lexema)
        assertNotNull(d.asignacion.expresion)
    }

    @Test
    fun asignacionSimpleYCompuesta() {
        val s = sentencias("^x = #5 |\n^x += #2 |\n^y = ^x |")
        assertEquals(3, s.size)
        assertTrue(s.all { it is Asignacion })
        assertEquals("=", (s[0] as Asignacion).operAsignacion.lexema)
        assertNotNull((s[0] as Asignacion).expresion)
        assertEquals("+=", (s[1] as Asignacion).operAsignacion.lexema)
        assertEquals("^x", (s[2] as Asignacion).identificador2!!.lexema)
    }

    @Test
    fun impresionValida() {
        val s = sentencias("imp ( 'hola' ) |\nimp ( ^x ) |")
        assertEquals(2, s.size)
        assertTrue(s.all { it is Impresion })
        assertNotNull((s[0] as Impresion).expresion)
        assertEquals("^x", (s[1] as Impresion).identificador!!.lexema)
    }

    @Test
    fun retornoValido() {
        val s = sentencias("ret #5 |\nret ^x |")
        assertEquals(2, s.size)
        assertTrue(s.all { it is Retorno })
        assertNotNull((s[0] as Retorno).expresion)
        assertEquals("^x", (s[1] as Retorno).identificador!!.lexema)
    }

    @Test
    fun lecturaValidaDentroDeAsignacion() {
        val s = sentencias("^x = rad ( 'dato' ) |")
        assertEquals(1, s.size)
        val a = s[0] as Asignacion
        val lectura = a.sentencia as Lectura
        assertEquals("'dato'", lectura.cadena.lexema)
    }

    @Test
    fun invocacionDeFuncion() {
        val s = sentencias("^f ( ^a : ^b ) |\n^g ( ) |")
        assertEquals(2, s.size)
        assertTrue(s.all { it is InvFuncion })
        assertEquals("^f", (s[0] as InvFuncion).nombre.lexema)
        assertEquals(2, (s[0] as InvFuncion).listaParametrosSinTipo.size)
        assertEquals(0, (s[1] as InvFuncion).listaParametrosSinTipo.size)
    }

    @Test
    fun incrementoYDecremento() {
        val s = sentencias("^i ++ |\n^i -- |")
        assertEquals(2, s.size)
        assertTrue(s.all { it is IncrDecr })
        assertEquals("++", (s[0] as IncrDecr).tipo.lexema)
        assertEquals("--", (s[1] as IncrDecr).tipo.lexema)
    }

    // ---------------------------------------------------------------------------------------------
    // Un caso de error por cada mensaje alcanzable de reportarError
    // ---------------------------------------------------------------------------------------------

    /** Comprueba que el programa no se construye y que el unico error es exactamente (mensaje, fila, columna) */
    private fun assertUnicoError(fuente: String, mensaje: String, fila: Int, columna: Int) {
        val a = analizar(fuente)
        assertNull(a.unidad, "Se esperaba fallo para: $fuente")
        assertEquals(1, a.errores.size, "Errores para <$fuente>: ${a.errores}")
        val e = a.errores[0]
        assertEquals(mensaje, e.error, "Mensaje para <$fuente>")
        assertEquals(fila, e.fila, "Fila de '${e.error}' para <$fuente>")
        assertEquals(columna, e.columna, "Columna de '${e.error}' para <$fuente>")
        assertSinDuplicados(a)
    }

    private fun assertErrorEnMain(cuerpo: String, mensaje: String, fila: Int, columna: Int) =
        assertUnicoError(enMain(cuerpo), mensaje, fila, columna)

    @Test
    fun errorUnidadDeCompilacion() {
        assertUnicoError("cls ^a { }", "Debe definir la visibilidad de la clase", 1, 1)
        assertUnicoError("pub cls { }", "Falta el identificador de la clase", 1, 9)
        assertUnicoError("pub cls ^a }", "Falta la llave izquierda", 1, 12)
        assertUnicoError("pub cls ^a {", "Falta la llave derecha", 1, 13)
    }

    @Test
    fun errorVariableGlobal() {
        assertUnicoError("pub cls ^a {\npub ent ^x\n}", "Debe colocar el fin de sentencia de la variable global", 3, 1)
        assertUnicoError("pub cls ^a {\npub ent 5 |\n}", "Debe definir el identificador de la variable global", 2, 9)
    }

    @Test
    fun errorFuncion() {
        assertUnicoError("pub cls ^a {\npub ent ^x |\npub\n}", "Debe definir el valor retornado de la función", 4, 1)
        assertUnicoError("pub cls ^a {\npub vd f ( ) {\n}\n}", "Debe definir el identificador de la función", 2, 8)
        assertUnicoError("pub cls ^a {\npub vd ^f ) {\n}\n}", "Debe definir el paréntesis izquierdo de la función", 2, 11)
        assertUnicoError("pub cls ^a {\npub vd ^f ( ent ^x {\n}\n}", "Debe definir el paréntesis derecho de la función", 2, 20)
        assertUnicoError("pub cls ^a {\npub vd ^f ( ) \n}\n}", "Debe definir la llave izquierda de la función", 3, 1)
        // "rad" suelto no es una sentencia: la lista termina y se esperaba la llave
        assertErrorEnMain("rad ( 'a' ) |", "Debe definir la llave derecha de la función", 3, 1)
    }

    @Test
    fun errorParametros() {
        assertUnicoError("pub cls ^a {\npub vd ^f ( ent ) {\n}\n}", "Debe definir el identificador del parámetro", 2, 17)
        // Parametro sin tipo mal formado (invocacion de funcion)
        assertErrorEnMain("^f (", "Debe definir el identificador del parámetro", 4, 1)
        assertErrorEnMain("^f ( ent ^x ) |", "Este parámetro no lleva tipo", 3, 6)
    }

    @Test
    fun errorDecision() {
        assertErrorEnMain("if {\nimp ( 'a' ) |\n}", "Debe definir el paréntesis izquierdo de la decisión", 3, 4)
        assertErrorEnMain("if ( ( ( #1 ) > ( #2 ) ) {\nimp ( 'a' ) |\n}", "Debe definir el paréntesis derecho de la decisión", 3, 26)
        assertErrorEnMain("if $condicion \nimp ( 'a' ) |", "Debe definir la llave izquierda de la decisión IF", 4, 1)
        assertErrorEnMain("if $condicion {\nrad ( 'a' ) |\n}", "Debe definir la llave derecha de la decisión IF", 4, 1)
        assertErrorEnMain("if $condicion {\nimp ( 'a' ) |\n} else {\nrad ( 'a' ) |\n}", "Debe definir la llave derecha de la decisión ELSE", 6, 1)
    }

    @Test
    fun errorDecisionElseSinLlaveIzquierdaSeReporta() {
        // Con else sin llave izquierda la decision falla: la unidad es invalida
        val a = analizar(enMain("if $condicion {\nimp ( 'a' ) |\n} else imp ( 'b' ) |"))
        assertNull(a.unidad, "${a.errores}")
        assertEquals(1, a.errores.size, "${a.errores}")
        assertEquals("Debe definir la llave izquierda de la decisión ELSE", a.errores[0].error)
        assertEquals(5, a.errores[0].fila)
        assertEquals(8, a.errores[0].columna)
    }

    @Test
    fun elseSinLlaveIzquierdaDebeInvalidarLaUnidad() {
        val a = analizar(enMain("if $condicion {\nimp ( 'a' ) |\n} else imp ( 'b' ) |"))
        assertNull(a.unidad, "${a.errores}")
        assertEquals(1, a.errores.size, "${a.errores}")
        assertEquals("Debe definir la llave izquierda de la decisión ELSE", a.errores[0].error)
    }

    @Test
    fun elseAlFinalDelArchivoInvalidaLaUnidad() {
        val a = analizar("pub cls ^a {\npub vd ^main ( ) {\nif $condicion {\nimp ( 'a' ) |\n} else")
        assertNull(a.unidad, "${a.errores}")
        assertTrue(a.errores.isNotEmpty())
        assertSinDuplicados(a)
    }

    @Test
    fun errorExpresionLogicaYRelacional() {
        assertErrorEnMain("if ( ) {\nimp ( 'a' ) |\n}", "Debe iniciar con un paréntesis izquierdo la expresión lógica", 3, 6)
        assertErrorEnMain("if ( ( ( #1 ) > ( #2 ) { imp ( 'a' ) |\n}", "Debe cerrar con paréntesis derecho la expresión lógica", 3, 24)
        assertErrorEnMain("if ( ( ( #1 ) ) ) {\nimp ( 'a' ) |\n}", "Falta el operador relacional en la expresión relacional", 3, 15)
        assertErrorEnMain("if ( ( ( #1 ) > ) ) {\nimp ( 'a' ) |\n}", "Debe encerrar en paréntesis la expresión aritmética", 3, 17)
    }

    @Test
    fun errorExpresionAritmetica() {
        assertErrorEnMain("if ( ( ( #1 ) > ( ) ) ) {\nimp ( 'a' ) |\n}", "Una expresión aritmética debe tener mínimo un número", 3, 19)
        assertErrorEnMain("^x = #5 + |", "Debe encerrar en paréntesis la expresión aritmética", 3, 11)
        assertErrorEnMain("if ( ( ( #1 ) > ( #2 + #3 ) ) ) {\nimp ( 'a' ) |\n}", "Debe encerrar en paréntesis la expresión aritmética", 3, 24)
    }

    @Test
    fun errorCiclo() {
        assertErrorEnMain("whl $condicion \nimp ( 'a' ) |", "Las sentencias deben ir encerradas en llaves", 4, 1)
        assertErrorEnMain("whl ( ( ( #1 ) > ( #2 ) ) {\nimp ( 'a' ) |\n}", "La expresión lógica debe ir encerrada en paréntesis", 3, 27)
        assertErrorEnMain("whl {\nimp ( 'a' ) |\n}", "La expresión lógica debe ir encerrada en paréntesis", 3, 5)
    }

    @Test
    fun errorDeclaracionYAsignacion() {
        assertErrorEnMain("ent 5 |", "La asignación no es válida", 3, 5)
        assertErrorEnMain("ent ^x = #5", "Falta el fin de sentencia", 4, 1)
        assertErrorEnMain("ent ^x #5 |", "Falta el operador de asignación", 3, 8)
        assertErrorEnMain("^x = ^y", "Falta el fin de sentencia", 4, 1)
    }

    @Test
    fun errorExpresionCadena() {
        assertErrorEnMain("^x = 'a' + |", "La expresión cadena debe tener mínimo una cadena", 3, 12)
    }

    @Test
    fun errorImpresion() {
        assertErrorEnMain("imp ( #5", "El tipo de impresión debe ir encerrado en paréntesis", 4, 1)
        assertErrorEnMain("imp", "El tipo de impresión debe ir encerrado en paréntesis", 4, 1)
        assertErrorEnMain("imp ( #5 )", "Falta el fin de sentencia", 4, 1)
        assertErrorEnMain("imp ( ^x )", "Falta el fin de sentencia", 4, 1)
        assertErrorEnMain("imp ( ) |", "Una expresión aritmética debe tener mínimo un número", 3, 7)
    }

    @Test
    fun errorRetorno() {
        assertErrorEnMain("ret #5", "Falta el fin de sentencia", 4, 1)
        assertErrorEnMain("ret ^x ^y |", "Falta el fin de sentencia", 3, 8)
    }

    @Test
    fun errorLectura() {
        assertErrorEnMain("^x = rad ( #5 ) |", "Se espera una cadena", 3, 12)
        assertErrorEnMain("^x = rad 'a'", "La cadena debe ir encerrada en paréntesis", 3, 10)
        assertErrorEnMain("^x = rad ( 'a' )", "Falta el fin de sentencia", 4, 1)
    }

    @Test
    fun errorInvocacionDeFuncion() {
        assertErrorEnMain("^f ( ^x", "Los parámetros deben estar encerrados en paréntesis", 4, 1)
        assertErrorEnMain("^f ( ^x ^y ) |", "Los parámetros deben estar encerrados en paréntesis", 3, 9)
        assertErrorEnMain("^f ( ^x ) ", "Falta el fin de sentencia", 4, 1)
    }

    @Test
    fun errorIncrementoDecremento() {
        assertErrorEnMain("^x ++", "Falta el fin de sentencia", 4, 1)
        assertErrorEnMain("^x -- ", "Falta el fin de sentencia", 4, 1)
    }

    /**
     * Mensajes de reportarError que NO se alcanzan como error final visible (siempre los tapa otro):
     *  - "Debe definir el tipo de la variable global": candidato=false, y la alternativa de función reporta
     *    "Debe definir el valor retornado de la función" en la misma posicion.
     *  - Los de "... no valida/no es valida" (expresion logica, relacional, aritmetica, cadena, "elemento a imprimir",
     *    "elemento a retornar", "expresion logica de la desicion"/ciclo): la subregla que falla siempre reporta
     *    antes un error mas especifico en la misma posicion y, en un empate, gana el primero.
     *  - "Se espera un identificador" (declaracion mutable) y "Por favor especifique el tipo de asignación":
     *    los tapa "La asignación no es válida" / el error de la expresión en la misma posicion.
     * Esta prueba documenta ese comportamiento con un caso representativo.
     */
    @Test
    fun mensajesTapadosPorElErrorEspecifico() {
        val casos = listOf(
            Triple("if ( ( ( #1 ) > ( #2 ) ) and ) {\nimp ( 'a' ) |\n}", "Debe iniciar con un paréntesis izquierdo la expresión lógica", 30),
            Triple("whl ( ) {\nimp ( 'a' ) |\n}", "Debe iniciar con un paréntesis izquierdo la expresión lógica", 7),
            Triple("^x = |", "Una expresión aritmética debe tener mínimo un número", 6),
            Triple("ret |", "Una expresión aritmética debe tener mínimo un número", 5),
            Triple("imp ( + ) |", "Una expresión aritmética debe tener mínimo un número", 7)
        )
        for ((cuerpo, mensaje, columna) in casos) {
            assertErrorEnMain(cuerpo, mensaje, 3, columna)
        }
        assertUnicoError("pub cls ^a {\npub ^x |\n}", "Debe definir el valor retornado de la función", 2, 5)
    }
}
