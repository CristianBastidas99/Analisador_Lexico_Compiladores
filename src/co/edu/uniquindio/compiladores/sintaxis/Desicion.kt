package co.edu.uniquindio.compiladores.sintaxis

import co.edu.uniquindio.compiladores.lexico.Token

class Desicion(var exprLogica:ExpresionLogica, val listaSentenciasIf:ArrayList<Sentencia>, val listaSentenciasElse:ArrayList<Sentencia> ) :Sentencia() {

}
