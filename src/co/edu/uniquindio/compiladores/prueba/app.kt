package co.edu.uniquindio.compiladores.prueba

import co.edu.uniquindio.compiladores.lexico.AnalizadorLexico
import co.edu.uniquindio.compiladores.lexico.Categoria
import co.edu.uniquindio.compiladores.lexico.Token
import co.edu.uniquindio.compiladores.semantico.AnalizadorSemantico
import co.edu.uniquindio.compiladores.sintaxis.AnalizadorSintactico
import co.edu.uniquindio.compiladores.sintaxis.InvFuncion
import co.edu.uniquindio.compiladores.sintaxis.ParametroSinTipo
import co.edu.uniquindio.compiladores.sintaxis.Sentencia

/*
&Unidad de Compilacion
pub cls ^alumno {

	&Parametros Globales
	pvt str ^nombre |
	pvt ent ^edad |
	pvt ent ^matric |

	&Funcion
	pub ent ^mult (ent ^a : ent ^b) {

	&Desicion
        if ( ( (#3 + ( #7 ) ) > ( #2 ) ) and ( (#2 ) > ( #3 ) ) ) {

		ent ^result | &Declaracion de Variable Mutable

		ent ^result =  ^a | &Declaracion de Variable Inmutable

	} else {

	    ent ^result | &Declaracion de Variable Mutable

		^result =  ( (#3 + ( #7 ) ) > ( #2 ) ) and ( (#2 ) > ( #3 ) ) | &Asignacion Expresion

		^result =  ^a | &Asignacion Identificador

	}

	imp ( (#3 + ( #7 ) ) > ( #2 ) ) | &Impresion Expresion

	imp ( ^result ) | &Impresion Identificador

	&Ciclo
	whl ( not ( (#2 ) > ( #3 ) )  ) {

		ret ^result | &Retorno Identificador

		ret   not ( (#2 ) > ( #3 ) ) | &Retorno Expresion

	}

	^result =  rad ( 'Muy buenos dias' ) | &Asignacion Lectura

	^mult ( ^a : ^b ) | &Invocacion Funcion

	^a ++ | &Incremento

    }
}
    //autogenerate from DDL  JPA hibernate
    //generate model from tables JPA hibernate

 */

fun main() {

    /*var nombre = Token("^var", Categoria.IDENTIFICADOR, 0, 0)
    var sentencia: Sentencia = InvFuncion(nombre, ArrayList<ParametroSinTipo>())

    println(sentencia.javaClass.simpleName.equals("InvFuncion"))

    var cadena = "Hola"
    println(cadena.split("\\."))*/

    pruebaUnidadDeCompilacion()
    //pruebaDeclaracionVariable()

    /*var texto = "hol/tipo:ent/ciclo/ciclo/if"
    var posicion = texto.indexOf(':')

    println(texto.substring(posicion + 1, texto.indexOf('/', posicion)))

    println(texto.substring(0,0))*/

}

fun pruebaUnidadDeCompilacion(){
    var codigo_fuente = "&Unidad de Compilacion\n" +
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
    //println(codigo_fuente)
    var analizadorLexico = AnalizadorLexico(codigo_fuente)
    analizadorLexico.analizar()
    //println(analizadorLexico.listaTokens)

    var analizadorSintactico = AnalizadorSintactico(analizadorLexico.listaTokens)
    var uc = analizadorSintactico.esUnidadDeCompilacion()
    if (uc != null) {
        //println(uc)
        val semantica = AnalizadorSemantico(uc)
        semantica.llenarTablaSimbolos()
        semantica.analizarSemantica()
        println(semantica.tablaSimbolos)
    }
}

fun pruebaDeclaracionVariable(){

    var codigo_fuente = "ent ^result |"
    println(codigo_fuente)
    var analizadorLexico = AnalizadorLexico(codigo_fuente)
    analizadorLexico.analizar()
    println(analizadorLexico.listaTokens)

    var analizadorSintactico = AnalizadorSintactico(analizadorLexico.listaTokens)
    var dvm = analizadorSintactico.esDeclaracionVariableMutSentencia()

    if (dvm != null) {
        println(dvm)
    }
}

fun pruebaAsignacion(){

    var codigo_fuente = "^result = |"
    println(codigo_fuente)
    var analizadorLexico = AnalizadorLexico(codigo_fuente)
    analizadorLexico.analizar()
    println(analizadorLexico.listaTokens)

    var analizadorSintactico = AnalizadorSintactico(analizadorLexico.listaTokens)
    var dvm = analizadorSintactico.esDeclaracionVariableMutSentencia()

    if (dvm != null) {
        println(dvm)
    }
}
