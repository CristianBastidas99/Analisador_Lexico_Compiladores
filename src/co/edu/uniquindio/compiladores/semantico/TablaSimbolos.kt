package co.edu.uniquindio.compiladores.semantico

import co.edu.uniquindio.compiladores.lexico.Error

class TablaSimbolos (var listaErrores: ArrayList<Error>) {

    var listaSimbolos: ArrayList<Simbolo> = ArrayList()

    /**
     * Permite Guardar un simbolo que representa una variables, una constante, un parametro, un arreglo
     */
    fun guardasSimboloValor( nombre: String, tipo: String, modificable: Boolean, ambito: String, acceso: String, fila: Int, columna: Int  ){
        val s = buscarSimboloValor(nombre, ambito)

        if(s == null){
            listaSimbolos.add(Simbolo(nombre, tipo, modificable, ambito, acceso, fila, columna))
        }else{
            listaErrores.add(Error("El campo con el nombre $nombre ya existe dentro del ambito $ambito", fila, columna))
        }
    }

    /**
     * Permite guardar un simbolo que representa una funcion (o metodo)
     */
    fun guardasSimboloFuncion( nombre: String, tipo: String, tiposParametros: ArrayList<String>, ambito: String, acceso: String, fila: Int, columna: Int  ){
        val s = buscarSimboloFuncion(nombre, tiposParametros)

        if(s == null){
            listaSimbolos.add(Simbolo(nombre, tipo, tiposParametros, ambito, acceso))
        }else{
            listaErrores.add(Error("La funcion con el nombre $nombre ya existe dentro del ambito $ambito", fila, columna))
        }
    }

    /**
     * Permite buscar un valor dentro de la tabla de simbolos
     */
    fun buscarSimboloValor(nombre: String?, ambito: String?): Simbolo?{
        for(s in listaSimbolos){
            if(s.tiposParametros == null) {
                if (s.nombre.equals(nombre) && s.ambito.equals(ambito)) {
                    return s
                }
            }
        }
        return null
    }


    /**
     * Permite buscar una funcion dentro de la tabla de simbolos
     */
    fun buscarSimboloFuncion(nombre: String?, tiposParametros: ArrayList<String>): Simbolo?{
        for(s in listaSimbolos){
            if(s.tiposParametros != null) {
                if (s.nombre == nombre && s.tiposParametros == tiposParametros) {
                    return s
                }
            }
        }
        return null
    }

    override fun toString(): String {
        return "TablaSimbolos(listaErrores=$listaErrores \n listaSimbolos=$listaSimbolos)"
    }

}