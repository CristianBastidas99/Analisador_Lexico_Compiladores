package co.edu.uniquindio.compiladores.sintaxis

import co.edu.uniquindio.compiladores.lexico.Token


class UnidadDeCompilacion(var visibilidad:Token, var nombre:Token, val listaVariablesGlobales:ArrayList<VariableGlobal>, val listaFunciones:ArrayList<Funcion>) {

}