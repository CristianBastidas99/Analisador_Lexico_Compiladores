package co.edu.uniquindio.compiladores.controlador

import co.edu.uniquindio.compiladores.lexico.AnalizadorLexico
import co.edu.uniquindio.compiladores.lexico.Categoria
import co.edu.uniquindio.compiladores.lexico.Token
import javafx.collections.FXCollections
import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.scene.control.TableColumn
import javafx.scene.control.TableView
import javafx.scene.control.TextArea
import javafx.scene.control.cell.PropertyValueFactory


class InicioControler {

    @FXML lateinit var codigo_fuente : TextArea
    @FXML lateinit var tabla_lexema : TableView<Token>
    @FXML lateinit var colm_lexema : TableColumn<Token, String>
    @FXML lateinit var colm_categoria : TableColumn<Token, Categoria>
    @FXML lateinit var colm_fila : TableColumn<Token, Int>
    @FXML lateinit var colm_columna : TableColumn<Token, Int>

    @FXML
    fun analizarCodigo(e : ActionEvent){

        if(codigo_fuente.length > 0) {
            var analizador = AnalizadorLexico(codigo_fuente.text)
            analizador.analizar()
            println(analizador.listaTokens)

            val list = FXCollections.observableArrayList<Token>()
            list.addAll(analizador.listaTokens)

            tabla_lexema.items.clear()

            colm_lexema.setCellValueFactory(PropertyValueFactory<Token, String>("lexema"))
            colm_categoria.setCellValueFactory(PropertyValueFactory<Token, Categoria>("categoria"))
            colm_fila.setCellValueFactory(PropertyValueFactory<Token, Int>("fila"))
            colm_columna.setCellValueFactory(PropertyValueFactory<Token, Int>("columna"))

            tabla_lexema.items.addAll(list)

        }
    }

}