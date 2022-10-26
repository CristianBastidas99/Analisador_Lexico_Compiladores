package co.edu.uniquindio.compiladores.lexico

class Error (var error:String, var fila:Int, var comumna:Int) {

    override fun toString(): String {
        return "Error(error='$error', fila=$fila, comumna=$comumna)"
    }
}