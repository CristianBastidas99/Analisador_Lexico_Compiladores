package co.edu.uniquindio.compiladores.controlador

import co.edu.uniquindio.compiladores.lexico.AnalizadorLexico
import co.edu.uniquindio.compiladores.lexico.Categoria
import co.edu.uniquindio.compiladores.lexico.Error
import co.edu.uniquindio.compiladores.lexico.Token
import co.edu.uniquindio.compiladores.semantico.AnalizadorSemantico
import co.edu.uniquindio.compiladores.semantico.Simbolo
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

    @FXML lateinit var tablaSimbolos : TableView<Simbolo>
    @FXML lateinit var colm_tsNombre : TableColumn<Simbolo, String>
    @FXML lateinit var colm_tsTipo : TableColumn<Simbolo, String>
    @FXML lateinit var colm_tsModificable : TableColumn<Simbolo, Boolean>
    @FXML lateinit var colm_tsAmbito : TableColumn<Simbolo, String>
    @FXML lateinit var colm_tsAcceso : TableColumn<Simbolo, String>
    @FXML lateinit var colm_tsFila : TableColumn<Simbolo, Int>
    @FXML lateinit var colm_tsColumna : TableColumn<Simbolo, Int>
    @FXML lateinit var colm_tsTiposParametros : TableColumn<Simbolo, ArrayList<String>>

    @FXML lateinit var tablaErroresSem : TableView<Error>
    @FXML lateinit var colm_esemError : TableColumn<Error, String>
    @FXML lateinit var colm_esemFila : TableColumn<Error, Int>
    @FXML lateinit var colm_esemColumna : TableColumn<Error, Int>


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

            if(uc != null) {
                arbolVisual.setRoot(uc!!.getArbolVisual())

                val listErrorSint = FXCollections.observableArrayList<Error>()
                listErrorSint.addAll(analizadorSintactico.listaErrores)

                tablaErroresSint.items.clear()

                colm_errorErroresSint.setCellValueFactory(PropertyValueFactory<Error, String>("error"))
                colm_filaErroresSint.setCellValueFactory(PropertyValueFactory<Error, Int>("fila"))
                colm_columnaErroresSint.setCellValueFactory(PropertyValueFactory<Error, Int>("columna"))

                tablaErroresSint.items.addAll(listErrorSint)

                val semantica = AnalizadorSemantico(uc)
                semantica.llenarTablaSimbolos()
                semantica.analizarSemantica()
                println(semantica.tablaSimbolos)

                if(semantica.tablaSimbolos.listaSimbolos.isNotEmpty()) {

                    val listTablaSim = FXCollections.observableArrayList<Simbolo>()
                    listTablaSim.addAll(semantica.tablaSimbolos.listaSimbolos)

                    tablaSimbolos.items.clear()

                    colm_tsNombre.setCellValueFactory(PropertyValueFactory<Simbolo, String>("nombre"))
                    colm_tsTipo.setCellValueFactory(PropertyValueFactory<Simbolo, String>("tipo"))
                    colm_tsModificable.setCellValueFactory(PropertyValueFactory<Simbolo, Boolean>("modificable"))
                    colm_tsAmbito.setCellValueFactory(PropertyValueFactory<Simbolo, String>("ambito"))
                    colm_tsAcceso.setCellValueFactory(PropertyValueFactory<Simbolo, String>("acceso"))
                    colm_tsFila.setCellValueFactory(PropertyValueFactory<Simbolo, Int>("fila"))
                    colm_tsColumna.setCellValueFactory(PropertyValueFactory<Simbolo, Int>("columna"))
                    colm_tsTiposParametros.setCellValueFactory(PropertyValueFactory<Simbolo, ArrayList<String>>("tiposParametros"))

                    tablaSimbolos.items.addAll(listTablaSim)

                }else{
                    tablaErroresSem.items.clear()
                }

                if(semantica.tablaSimbolos.listaErrores.isNotEmpty()) {

                    val listErrorSema = FXCollections.observableArrayList<Error>()
                    listErrorSema.addAll(semantica.tablaSimbolos.listaErrores)

                    tablaErroresSem.items.clear()

                    colm_esemError.setCellValueFactory(PropertyValueFactory<Error, String>("error"))
                    colm_esemFila.setCellValueFactory(PropertyValueFactory<Error, Int>("fila"))
                    colm_esemColumna.setCellValueFactory(PropertyValueFactory<Error, Int>("columna"))

                    tablaErroresSem.items.addAll(listErrorSema)
                }else{
                    tablaErroresSem.items.clear()
                }

            }
        }
    }

}