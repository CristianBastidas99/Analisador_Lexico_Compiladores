package co.edu.uniquindio.compiladores.semantico

class Simbolo {

    var nombre : String? = null
    var tipo: String? = null
    var modificable: Boolean? = null
    var ambito: String? = null
    var acceso: String? = null
    var fila: Int? = null
    var columna: Int? = null
    var tiposParametros: ArrayList<String>? = null

    /**
     * Contructor para crear un simbolo de tipo valor
     */
    constructor( nombre: String?, tipo: String?, modificable: Boolean?, ambito: String?, acceso: String?, fila: Int?, columna: Int? ) {
        this.nombre = nombre
        this.tipo = tipo
        this.modificable = modificable
        this.ambito = ambito
        this.acceso = acceso
        this.fila = fila
        this.columna = columna
    }

    /**
     * Contructor para crear un simbolo de tipo Metodo (Funcion)
     */
    constructor( nombre: String?, tipo: String?, tiposParametros: ArrayList<String>, ambito: String?, acceso: String? ) {
        this.nombre = nombre
        this.tipo = tipo
        this.tiposParametros = tiposParametros
        this.ambito = ambito
        this.acceso = acceso
    }

    override fun toString(): String {

        if(tiposParametros != null){
            return "Simbolo(nombre=$nombre, tipo=$tipo, ambito=$ambito, acceso=$acceso, tiposParametros=$tiposParametros)"
        }else{
            return "Simbolo(nombre=$nombre, tipo=$tipo, modificable=$modificable, ambito=$ambito, acceso=$acceso, fila=$fila, columna=$columna)"
        }

    }


}