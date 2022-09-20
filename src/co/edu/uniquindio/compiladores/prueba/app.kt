package co.edu.uniquindio.compiladores.prueba

import co.edu.uniquindio.compiladores.lexico.AnalizadorLexico

fun main(){
    var codigo_fuente = "&hola mi panita\n ^Jessica65 °r°"
    println(codigo_fuente)
    var analizadorLexico = AnalizadorLexico(codigo_fuente)
    analizadorLexico.analizar()
    print(analizadorLexico.listaTokens)
    //\b \$ \n
}