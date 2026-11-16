package co.edu.uniquindio.compiladores

import co.edu.uniquindio.compiladores.lexico.AnalizadorLexico
import co.edu.uniquindio.compiladores.lexico.Error
import co.edu.uniquindio.compiladores.semantico.AnalizadorSemantico
import co.edu.uniquindio.compiladores.sintaxis.AnalizadorSintactico
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Errores semanticos concretos: mensaje, fila y columna (numeracion desde 1).
 * Los programas usan solo \n como salto de linea.
 */
class SemanticoTest {

    private fun analizar(fuente: String): AnalizadorSemantico {
        val lexico = AnalizadorLexico(fuente)
        lexico.analizar()
        val sintactico = AnalizadorSintactico(lexico.listaTokens)
        val unidad = sintactico.esUnidadDeCompilacion()
        assertNotNull(unidad, "No se construyo la unidad. Errores: ${sintactico.listaErrores}")
        assertTrue(sintactico.listaErrores.isEmpty(), "Errores sintacticos: ${sintactico.listaErrores}")
        val semantico = AnalizadorSemantico(unidad!!)
        semantico.llenarTablaSimbolos()
        semantico.analizarSemantica()
        return semantico
    }

    private fun errores(fuente: String): List<Error> = analizar(fuente).listaErroresSemanticos

    private fun assertSinErrores(fuente: String) {
        val e = errores(fuente)
        assertTrue(e.isEmpty(), "No debia haber errores semanticos: $e")
    }

    private fun assertUnError(fuente: String, mensaje: String, fila: Int, columna: Int) {
        val e = errores(fuente)
        assertEquals(1, e.size, "Se esperaba un unico error: $e")
        assertEquals(mensaje, e[0].error)
        assertEquals(fila, e[0].fila, "fila de $e")
        assertEquals(columna, e[0].columna, "columna de $e")
    }

    /** Clase ^a con una funcion ^main vd cuyo cuerpo son las sentencias dadas (una por linea, desde la linea 3) */
    private fun enMain(vararg sentencias: String): String =
        "pub cls ^a {\n" + "pub vd ^main ( ) {\n" + sentencias.joinToString("") { it + "\n" } + "}\n" + "}"

    private val fuenteCalculo = "pub cls ^calc {\n" +
            "pvt ent ^total |\n" +
            "pub ent ^sumar ( ent ^a : ent ^b ) {\n" +
            "ret ^a |\n" +
            "}\n" +
            "pub vd ^main ( ) {\n" +
            "ent ^x = #3 |\n" +
            "ent ^y = #4 |\n" +
            "^sumar ( ^x : ^y ) |\n" +
            "^total = ^sumar ( ^x : ^y ) |\n" +
            "}\n" +
            "}"

    @Test
    fun argumentoGlobalEnInvocacion() {
        assertSinErrores("pub cls ^a {\n" +
                "pvt ent ^k |\n" +
                "pub ent ^f ( ent ^p ) {\n" +
                "ret ^p |\n" +
                "}\n" +
                "pub vd ^main ( ) {\n" +
                "^f ( ^k ) |\n" +
                "}\n" +
                "}")
    }

    @Test
    fun invocacionComoValor() {
        assertSinErrores(fuenteCalculo)
    }

    @Test
    fun invocacionComoValorDeTipoDistinto() {
        // ^sumar devuelve ent y se asigna a un str (linea 10, el nombre de la funcion empieza en la columna 6)
        val fuente = fuenteCalculo.replace("^total = ^sumar", "^t = ^sumar").replace("pvt ent ^total |", "pvt str ^t |")
        assertUnError(fuente, "El tipo de la invocación de función (ent) es diferente al de la variable (str)", 10, 6)
    }

    @Test
    fun funcionInexistente() {
        assertUnError(enMain("^zzz ( ) |"), "La función invocada no existe", 3, 1)
    }

    @Test
    fun invocacionConArgumentoDeTipoDistinto() {
        // ^sumar espera (ent, ent) y se invoca con (ent, str): no existe esa firma
        val fuente = fuenteCalculo.replace("ent ^y = #4 |", "str ^y = 'a' |").replace("^total = ^sumar ( ^x : ^y ) |\n", "")
        assertUnError(fuente, "La función invocada no existe", 9, 1)
    }

    @Test
    fun redeclaracion() {
        assertUnError(enMain("ent ^x |", "ent ^x |"),
            "El campo con el nombre ^x ya existe dentro del ámbito ^a/^main:vd", 4, 5)
    }

    @Test
    fun variableNoDeclarada() {
        assertUnError(enMain("^y = #1 |"),
            "La variable (^y) que intenta dar asignación no se ha declarado en el ámbito ^a/^main:vd", 3, 1)
    }

    @Test
    fun argumentoNoDeclaradoEnInvocacion() {
        // Solo se reporta la variable: no se agrega un "la funcion no existe" derivado
        assertUnError(fuenteCalculo.replace("^sumar ( ^x : ^y ) |\n^total", "^sumar ( ^x : ^q ) |\n^total"),
            "La variable (^q) que intenta dar como argumento no se ha declarado", 9, 15)
    }

    @Test
    fun dosIfHermanosConLaMismaVariable() {
        assertSinErrores(enMain("if ( ( ( #1 ) > ( #2 ) ) ) {", "ent ^x = #1 |", "}", "if ( ( ( #3 ) > ( #2 ) ) ) {", "ent ^x = #2 |", "}"))
    }

    @Test
    fun usoCruzadoEntreIfHermanos() {
        assertUnError(enMain("if ( ( ( #1 ) > ( #2 ) ) ) {", "ent ^x = #1 |", "}", "if ( ( ( #3 ) > ( #2 ) ) ) {", "imp ( ^x ) |", "}"),
            "La variable (^x) que intenta imprimir no se ha declarado", 7, 7)
    }

    @Test
    fun dosCiclosHermanosConLaMismaVariable() {
        assertSinErrores(enMain("whl ( ( ( #1 ) > ( #2 ) ) ) {", "ent ^x = #1 |", "}", "whl ( ( ( #3 ) > ( #2 ) ) ) {", "ent ^x = #2 |", "}"))
    }

    @Test
    fun lecturaSobreVariableNoDeclarada() {
        assertUnError(enMain("^z = rad ( 'a' ) |"),
            "La variable (^z) que intenta dar asignación no se ha declarado en el ámbito ^a/^main:vd", 3, 1)
    }

    @Test
    fun incrementoSobreVariableNoDeclarada() {
        assertUnError(enMain("^z ++ |"),
            "La variable (^z) que intenta incrementar o decrementar no se ha declarado", 3, 1)
    }

    @Test
    fun retornoConValorEnFuncionVoid() {
        val e = errores("pub cls ^a {\npub vd ^f ( ) {\nret #1 |\n}\n}")
        assertEquals(1, e.size, "$e")
        assertEquals("La función (^a/^f:vd) no debe retornar ningún valor", e[0].error)
    }

    @Test
    fun tiposIncompatibles() {
        assertUnError(enMain("ent ^x |", "^x = 'hola' |"),
            "El tipo de dato de la expresión (str) no coincide con el tipo de la variable (ent)", 4, 1)
    }

    @Test
    fun retornoIncorrecto() {
        // El retorno se reporta en la posicion del token 'ret' (linea 3, columna 1)
        assertUnError("pub cls ^a {\n" +
                "pub ent ^f ( ) {\n" +
                "ret 'hola' |\n" +
                "}\n" +
                "}",
            "El tipo de dato de la expresión (str) que desea retornar no es correcto en la función (ent)", 3, 1)
    }

    @Test
    fun lecturaAStrEntYDbeSinErrores() {
        assertSinErrores(enMain("str ^s |", "ent ^e |", "dbe ^d |",
            "^s = rad ( 'a' ) |", "^e = rad ( 'b' ) |", "^d = rad ( 'c' ) |"))
    }

    @Test
    fun lecturaABlnEsError() {
        assertUnError(enMain("bln ^b |", "^b = rad ( 'x' ) |"),
            "La variable (^b) es de tipo bln: la lectura (rad) solo se puede asignar a variables str, ent o dbe", 4, 1)
    }

    @Test
    fun lecturaACrtEsError() {
        assertUnError(enMain("crt ^c |", "^c = rad ( 'x' ) |"),
            "La variable (^c) es de tipo crt: la lectura (rad) solo se puede asignar a variables str, ent o dbe", 4, 1)
    }

    @Test
    fun inmutableReasignada() {
        assertUnError(enMain("ent ^x = #1 |", "^x = #2 |"),
            "La variable (^x) es inmutable y no se puede modificar", 4, 1)
    }

    @Test
    fun inmutableIncrementada() {
        assertUnError(enMain("ent ^x = #1 |", "^x ++ |"),
            "La variable (^x) es inmutable y no se puede modificar", 4, 1)
    }

    @Test
    fun inmutableLeida() {
        assertUnError(enMain("str ^x = 'a' |", "^x = rad ( 'b' ) |"),
            "La variable (^x) es inmutable y no se puede modificar", 4, 1)
    }

    @Test
    fun inmutableInicializadaYUsadaSinErrores() {
        assertSinErrores(enMain("ent ^x = #1 |", "ent ^y |", "^y = ^x |", "imp ( ^x ) |"))
    }

    @Test
    fun mainVoidConParametros() {
        assertUnError("pub cls ^a {\n" +
                "pub vd ^main ( ent ^p ) {\n" +
                "imp ( ^p ) |\n" +
                "}\n" +
                "}",
            "La función ^main no puede tener parámetros", 2, 8)
    }

    @Test
    fun mainNoVoidConParametrosEsFuncionNormal() {
        assertSinErrores("pub cls ^a {\n" +
                "pub ent ^main ( ent ^p ) {\n" +
                "ret ^p |\n" +
                "}\n" +
                "}")
    }

    @Test
    fun funcionDuplicadaSenalaElNombre() {
        val fuente = "pub cls ^a {\n" +
                "pub vd ^f ( ) {\n" +
                "imp ( 'a' ) |\n" +
                "}\n" +
                "pub vd ^f ( ) {\n" +
                "imp ( 'b' ) |\n" +
                "}\n" +
                "}"
        assertUnError(fuente, "La función con el nombre ^f ya existe dentro del ámbito ^a", 5, 8)
    }

    @Test
    fun simbolosGuardanLaPosicionDelNombre() {
        val s = analizar("pub cls ^a {\n" +
                "pvt ent ^g |\n" +
                "pub ent ^f ( ent ^p ) {\n" +
                "ent ^v |\n" +
                "ret ^p |\n" +
                "}\n" +
                "}").tablaSimbolos
        fun posicion(nombre: String) = s.listaSimbolos.first { it.nombre == nombre }.let { Pair(it.fila, it.columna) }
        assertEquals(Pair(2, 9), posicion("^g"))
        assertEquals(Pair(3, 9), posicion("^f"))
        assertEquals(Pair(3, 18), posicion("^p"))
        assertEquals(Pair(4, 5), posicion("^v"))
    }

    @Test
    fun caracterSueltoEsCrt() {
        assertSinErrores(enMain("crt ^c |", "^c = °x° |", "crt ^d = °y° |"))
    }

    @Test
    fun concatenacionDeCaracteresEsStr() {
        assertUnError(enMain("crt ^c = °x° + °y° |"),
            "El tipo de dato de la expresión (str) no coincide con el tipo de la variable (crt)", 3, 5)
    }

    /**
     * Programa de ejemplo del antiguo prueba/app.kt (ya eliminado). Los seis errores son los mismos que producia antes de la Fase 3
     * (solo cambian la numeracion desde 1 y la ortografia); ninguno era un falso positivo.
     */
    @Test
    fun programaDeEjemploDeApp() {
        val e = errores(fuenteApp)
        val esperados = listOf(
            Error("El campo con el nombre ^result ya existe dentro del ámbito ^alumno/^mult:ent/if@13:31", 17, 7),
            Error("El tipo de dato de la expresión (bln) no coincide con el tipo de la variable (ent)", 23, 3),
            Error("La variable (^result) que intenta imprimir no se ha declarado", 31, 8),
            Error("La variable (^result) que intenta retornar no se ha declarado", 36, 7),
            Error("El tipo de dato de la expresión (bln) que desea retornar no es correcto en la función (ent)", 38, 3),
            Error("La variable (^result) que intenta dar asignación no se ha declarado en el ámbito ^alumno/^mult:ent", 42, 2)
        )
        assertEquals(esperados.map { Triple(it.error, it.fila, it.columna) }, e.map { Triple(it.error, it.fila, it.columna) })
    }

    private val fuenteApp = "&Unidad de Compilacion\n" +
            "pub cls ^alumno {\n" +
            "\n" +
            "\t&Parametros Globales\n" +
            "\tpvt str ^nombre |\n" +
            "\tpvt ent ^edad |\n" +
            "\tpvt ent ^matric |\n" +
            "\n" +
            "\t&Funcion\n" +
            "\tpub ent ^mult (ent ^a : ent ^b) {\n" +
            "\n" +
            "\t&Desicion\n" +
            "        if ( ( (#3 + ( #7 ) ) > ( #2 ) ) and ( (#2 ) > ( #3 ) ) ) {\n" +
            "\n" +
            "\t\tent ^result | &Declaracion de Variable Mutable\n" +
            "\n" +
            "\t\tent ^result =  ^a | &Declaracion de Variable Inmutable\n" +
            "\n" +
            "\t} else {\n" +
            "\n" +
            "\t    ent ^result | &Declaracion de Variable Mutable\n" +
            "\n" +
            "\t\t^result =  ( (#3 + ( #7 ) ) > ( #2 ) ) and ( (#2 ) > ( #3 ) ) | &Asignacion Expresion\n" +
            "\n" +
            "\t\t^result =  ^a | &Asignacion Identificador\n" +
            "\n" +
            "\t}\n" +
            "\n" +
            "\timp ( (#3 + ( #7 ) ) > ( #2 ) ) | &Impresion Expresion\n" +
            "\n" +
            "\timp ( ^result ) | &Impresion Identificador\n" +
            "\n" +
            "\t&Ciclo\n" +
            "\twhl ( not ( (#2 ) > ( #3 ) )  ) {\n" +
            "\n" +
            "\t\tret ^result | &Retorno Identificador\n" +
            "\n" +
            "\t\tret   not ( (#2 ) > ( #3 ) ) | &Retorno Expresion\n" +
            "\n" +
            "\t}\n" +
            "\n" +
            "\t^result =  rad ( 'Muy buenos dias' ) | &Asignacion Lectura\n" +
            "\n" +
            "\t^mult ( ^a : ^b ) | &Invocacion Funcion\n" +
            "\n" +
            "\t^a ++ | &Incremento\n" +
            "\n" +
            "    }\n" +
            "}"
}
