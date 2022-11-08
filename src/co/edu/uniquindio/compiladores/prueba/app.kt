package co.edu.uniquindio.compiladores.prueba

import co.edu.uniquindio.compiladores.lexico.AnalizadorLexico
import co.edu.uniquindio.compiladores.sintaxis.AnalizadorSintactico

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

    pruebaUnidadDeCompilacion()
    //pruebaDeclaracionVariable()
}

fun pruebaUnidadDeCompilacion(){
    var codigo_fuente = "&Unidad de Compilacion\n"
    println(codigo_fuente)
    var analizadorLexico = AnalizadorLexico(codigo_fuente)
    analizadorLexico.analizar()
    println(analizadorLexico.listaTokens)

    var analizadorSintactico = AnalizadorSintactico(analizadorLexico.listaTokens)
    var uc = analizadorSintactico.esUnidadDeCompilacion()
    if (uc != null) {
        println(uc)
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
