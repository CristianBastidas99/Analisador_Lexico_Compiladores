package co.edu.uniquindio.compiladores.sintaxis

import co.edu.uniquindio.compiladores.lexico.Token

class Parametro(var tipo:Token?, var nombre:Token) {


    override fun toString(): String {
        if(tipo != null) {
            return "Parametro(tipo=${tipo!!.lexema}, nombre=${nombre.lexema})"
        }else{
            return "Parametro(nombre=${nombre.lexema})"
        }
    }
}
