package co.edu.uniquindio.compiladores.sintaxis

import co.edu.uniquindio.compiladores.lexico.Token
import javafx.scene.control.TreeItem

class InvFuncion (var nombre:Token, val listaParametrosSinTipo: ArrayList<ParametroSinTipo>) : Sentencia() {

    override fun getArbolVisual(): TreeItem<String> {
        val raiz = TreeItem("Invocacion Funcion")

        raiz.children.add( TreeItem("${nombre.lexema}"))

        if(listaParametrosSinTipo.isNotEmpty()) {

            val parametrosSinTipo = TreeItem("Parametros sin tipo")
            raiz.children.add(parametrosSinTipo)
            for (parametrosinTipo in listaParametrosSinTipo) {
                parametrosSinTipo.children.add(parametrosinTipo.getArbolVisual())
            }
        }

        return raiz
    }

}
