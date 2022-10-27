package co.edu.uniquindio.compiladores.sintaxis

import co.edu.uniquindio.compiladores.lexico.Token

class DeclaracionVariable(var tipoAcceso:Token, var tipo:Token, var nombre:Token?, var asignacion:Asignacion? ) :Sentencia() {

}
