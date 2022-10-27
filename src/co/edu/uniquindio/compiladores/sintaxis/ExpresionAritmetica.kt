package co.edu.uniquindio.compiladores.sintaxis

import co.edu.uniquindio.compiladores.lexico.Token


class ExpresionAritmetica (var numero:Token, var operAritmetico:Token?, var exprAritmetica:ExpresionAritmetica?) : Expresion() {

}
