package co.edu.uniquindio.compiladores.sintaxis

import co.edu.uniquindio.compiladores.lexico.Token

class ExpresionCadena(var tipo: Token, var operAgrupacion: Token?, var expresionCadena: ExpresionCadena?) : Expresion() {

}
