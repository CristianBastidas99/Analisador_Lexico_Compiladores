package co.edu.uniquindio.compiladores.sintaxis

import javafx.scene.control.TreeItem

class Ciclo(var expresionLogica: ExpresionLogica, val listaSentencias:ArrayList<Sentencia>) : Sentencia() {
    override fun getArbolVisual(): TreeItem<String> {
        val raiz = TreeItem("Ciclo")

        raiz.children.add( expresionLogica.getArbolVisual() )

        if(listaSentencias.isNotEmpty()) {

            val sentencias = TreeItem("Sentencias")
            raiz.children.add(sentencias)
            for (sentencia in listaSentencias) {
                sentencias.children.add(sentencia.getArbolVisual())
            }
        }

        return raiz
    }

}
