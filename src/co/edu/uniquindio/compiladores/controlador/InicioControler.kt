package co.edu.uniquindio.compiladores.controlador

import co.edu.uniquindio.compiladores.lexico.AnalizadorLexico
import co.edu.uniquindio.compiladores.lexico.Categoria
import co.edu.uniquindio.compiladores.lexico.Error
import co.edu.uniquindio.compiladores.lexico.Token
import co.edu.uniquindio.compiladores.sintaxis.AnalizadorSintactico
import javafx.collections.FXCollections
import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.scene.control.Tab
import javafx.scene.control.TabPane
import javafx.scene.control.TableColumn
import javafx.scene.control.TableView
import javafx.scene.control.TextArea
import javafx.scene.control.TreeView
import javafx.scene.control.cell.PropertyValueFactory


class InicioControler {

    @FXML lateinit var codigo_fuente : TextArea
    @FXML lateinit var arbolVisual : TreeView<String>
    @FXML lateinit var tabPane : TabPane
    @FXML lateinit var tablaTabLexico : TableView<Token>
    @FXML lateinit var colm_lexemaTabLexico : TableColumn<Token, String>
    @FXML lateinit var colm_catTabLexico : TableColumn<Token, Categoria>
    @FXML lateinit var colm_filaTabLexico : TableColumn<Token, Int>
    @FXML lateinit var colm_colmTabLexico : TableColumn<Token, Int>

    @FXML lateinit var tablaErroresSint : TableView<Error>
    @FXML lateinit var colm_errorErroresSint : TableColumn<Error, String>
    @FXML lateinit var colm_filaErroresSint : TableColumn<Error, Int>
    @FXML lateinit var colm_columnaErroresSint : TableColumn<Error, Int>


    @FXML
    fun analizarCodigo(e : ActionEvent){

        if(codigo_fuente.length > 0) {
            var analizador = AnalizadorLexico(codigo_fuente.text)
            analizador.analizar()
            //println(analizador.listaTokens)

            val list = FXCollections.observableArrayList<Token>()
            list.addAll(analizador.listaTokens)

            tablaTabLexico.items.clear()

            colm_lexemaTabLexico.setCellValueFactory(PropertyValueFactory<Token, String>("lexema"))
            colm_catTabLexico.setCellValueFactory(PropertyValueFactory<Token, Categoria>("categoria"))
            colm_filaTabLexico.setCellValueFactory(PropertyValueFactory<Token, Int>("fila"))
            colm_colmTabLexico.setCellValueFactory(PropertyValueFactory<Token, Int>("columna"))

            tablaTabLexico.items.addAll(list)


            var analizadorSintactico = AnalizadorSintactico(analizador.listaTokens)
            var uc = analizadorSintactico.esUnidadDeCompilacion()

            arbolVisual.setRoot(uc!!.getArbolVisual())

            val listErrorSint = FXCollections.observableArrayList<Error>()
            listErrorSint.addAll(analizadorSintactico.listaErrores)

            tablaErroresSint.items.clear()

            colm_errorErroresSint.setCellValueFactory(PropertyValueFactory<Error, String>("error"))
            colm_filaErroresSint.setCellValueFactory(PropertyValueFactory<Error, Int>("fila"))
            colm_columnaErroresSint.setCellValueFactory(PropertyValueFactory<Error, Int>("columna"))

            tablaErroresSint.items.addAll(listErrorSint)

        }
    }

}