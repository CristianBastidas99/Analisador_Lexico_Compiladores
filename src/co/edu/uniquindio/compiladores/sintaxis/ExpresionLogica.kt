package co.edu.uniquindio.compiladores.sintaxis

import co.edu.uniquindio.compiladores.lexico.Token

class ExpresionLogica(var negacion:Token?, var exprRelacional:ExpresionRelacional, var operLogico:Token?, var exprLogica:ExpresionLogica?) {

}
