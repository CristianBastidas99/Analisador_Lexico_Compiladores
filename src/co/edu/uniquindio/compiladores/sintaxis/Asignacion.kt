package co.edu.uniquindio.compiladores.sintaxis

import co.edu.uniquindio.compiladores.lexico.Token

class Asignacion(var identificador:Token, var operAsignacion:Token, var expresion:Expresion?, var identificador2:Token?, var sentencia: Sentencia?) :Sentencia() {

}
