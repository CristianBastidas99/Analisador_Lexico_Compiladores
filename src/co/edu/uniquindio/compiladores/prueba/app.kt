package co.edu.uniquindio.compiladores.prueba

import co.edu.uniquindio.compiladores.lexico.AnalizadorLexico
import co.edu.uniquindio.compiladores.sintaxis.AnalizadorSintactico

fun main(){
    var codigo_fuente = "pub cls ^persona { \n" +
            "pvt ent ^edad| \n" +
            "pvt str ^nombre| \n" +
            "pub vd ^getEdad ( ent ^a : ^b) { \n" +
            "\n" +
            "} \n" +
            "}"
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