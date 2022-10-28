package co.edu.uniquindio.compiladores.sintaxis

import co.edu.uniquindio.compiladores.lexico.Token
import javafx.scene.control.TreeItem

class Desicion(var exprLogica:ExpresionLogica, val listaSentenciasIf:ArrayList<Sentencia>, val listaSentenciasElse:ArrayList<Sentencia> ) :Sentencia() {
    override fun getArbolVisual(): TreeItem<String> {
        val raiz = TreeItem("Desicion")

        raiz.children.add( exprLogica.getArbolVisual() )

        if(listaSentenciasIf.isNotEmpty()) {

            val sentenciasIf = TreeItem("Sentencias en if")
            raiz.children.add(sentenciasIf)
            for (sentencia in listaSentenciasIf) {
                sentenciasIf.children.add(sentencia.getArbolVisual())
            }
        }

        if(listaSentenciasElse.isNotEmpty()) {

            val sentenciasElse = TreeItem("Sentencias en else")
            raiz.children.add(sentenciasElse)
            for (sentencia in listaSentenciasElse) {
                sentenciasElse.children.add(sentencia.getArbolVisual())
            }
        }

        return  raiz
    }


}
