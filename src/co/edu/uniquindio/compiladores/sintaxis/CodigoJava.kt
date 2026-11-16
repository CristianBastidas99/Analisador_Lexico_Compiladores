package co.edu.uniquindio.compiladores.sintaxis

import co.edu.uniquindio.compiladores.lexico.Categoria
import co.edu.uniquindio.compiladores.lexico.Token

/**
 * Utilidades compartidas por los nodos del AST para generar Java valido:
 * nombres de identificadores (palabras reservadas de Java, sombreado de variables locales),
 * literales (cadenas, caracteres, numeros) y encadenado de sentencias.
 */
object CodigoJava {

    private val reservadas = setOf(
        "abstract", "assert", "boolean", "break", "byte", "case", "catch", "char", "class", "const", "continue",
        "default", "do", "double", "else", "enum", "extends", "final", "finally", "float", "for", "goto", "if",
        "implements", "import", "instanceof", "int", "interface", "long", "native", "new", "package", "private",
        "protected", "public", "return", "short", "static", "strictfp", "super", "switch", "synchronized", "this",
        "throw", "throws", "transient", "try", "void", "volatile", "while", "true", "false", "null",
        // Palabras contextuales y restringidas de Java 9-17
        "_", "yield", "record", "var", "sealed", "permits", "non", "module", "open", "opens", "exports", "requires",
        "to", "with", "uses", "provides", "transitive",
        // Nombres usados por el codigo generado
        "args", "String", "System", "JOptionPane", "Boolean",
        // Metodos de Object que una funcion estatica no puede redefinir
        "wait", "notify", "notifyAll", "getClass", "hashCode", "equals", "toString", "clone", "finalize"
    )

    private val ambitos = ArrayDeque<MutableMap<String, String>>()

    /** Nombre Java de un identificador del lenguaje (^nombre): quita el ^ y evita palabras reservadas de Java */
    fun identificador(lexema: String): String {
        val nombre = lexema.substring(1)
        // $ es valido en Java pero el lenguaje fuente no lo puede producir: los nombres generados nunca colisionan con los del usuario
        return if (nombre in reservadas) nombre + "$" else nombre
    }

    fun reiniciarAmbitos() = ambitos.clear()

    fun abrirAmbito() = ambitos.addLast(HashMap())

    fun cerrarAmbito() {
        if (ambitos.isNotEmpty()) ambitos.removeLast()
    }

    /**
     * Declara una variable local o parametro en el ambito actual. Java prohibe declarar una local con el nombre de
     * otra local visible (el lenguaje fuente si lo permite en ambitos anidados), asi que se renombra con sufijo $N.
     */
    fun declarar(lexema: String): String {
        val base = identificador(lexema)
        var candidato = base
        var n = 2
        while (ambitos.any { candidato in it.values }) {
            candidato = base + "$" + n++
        }
        // Sin ambito abierto (uso aislado de un nodo) no se registra: el estado nunca queda sucio
        ambitos.lastOrNull()?.put(lexema, candidato)
        return candidato
    }

    /** Nombre Java de una referencia a variable: la local visible mas interna, o la global */
    fun usar(lexema: String): String {
        for (i in ambitos.indices.reversed()) {
            val nombre = ambitos[i][lexema]
            if (nombre != null) return nombre
        }
        return identificador(lexema)
    }

    /** Literal numerico: #007 -> 7 (evita octales invalidos), @1.5 -> 1.5 */
    fun numero(token: Token): String {
        val texto = token.lexema.substring(1)
        if (token.categoria == Categoria.ENTERO) {
            return texto.trimStart('0').ifEmpty { "0" }
        }
        // Decimal @D*.D*: @1. -> 1.0, @.5 -> 0.5, @. -> 0.0, @007.5 -> 7.5
        val punto = texto.indexOf('.')
        val parteEntera = (if (punto >= 0) texto.substring(0, punto) else texto).trimStart('0').ifEmpty { "0" }
        val parteDecimal = (if (punto >= 0) texto.substring(punto + 1) else "").ifEmpty { "0" }
        return "$parteEntera.$parteDecimal"
    }

    /** Convierte un literal de cadena del lenguaje ('...') a un literal Java (comillas dobles) */
    fun cadena(lexema: String): String {
        val contenido = lexema.substring(1, lexema.length - 1)
        val sb = StringBuilder("\"")
        var i = 0
        while (i < contenido.length) {
            val c = contenido[i]
            if (c == '\\' && i + 1 < contenido.length) {
                val sig = contenido[i + 1]
                when (sig) {
                    '\n' -> sb.append("\\n")
                    '\t' -> sb.append("\\t")
                    '"' -> sb.append("\\\"")
                    '\'' -> sb.append("'")
                    '\\' -> sb.append("\\\\")
                    else -> sb.append(escapar(sig))
                }
                i += 2
            } else {
                sb.append(if (c == '"') "\\\"" else escapar(c))
                i++
            }
        }
        return sb.append('"').toString()
    }

    /** Convierte un literal de caracter del lenguaje (°x° o °\x°) a un literal Java */
    fun caracter(lexema: String): String {
        val contenido = lexema.substring(1, lexema.length - 1)
        val c = if (contenido.length > 1 && contenido[0] == '\\') contenido[1] else contenido[0]
        val texto = when (c) {
            '\'' -> "\\'"
            '\\' -> "\\\\"
            '"' -> "\\\""
            '\n' -> "\\n"
            '\t' -> "\\t"
            else -> escapar(c)
        }
        return "'$texto'"
    }

    private fun escapar(c: Char): String {
        // Java procesa los \\uXXXX antes del lexer: nunca se usan para caracteres de control (\\u000a, \\u000d rompen el literal)
        return when {
            c == '\r' -> "\\r"
            c == '\b' -> "\\b"
            c == '\u000c' -> "\\f"
            c == '\n' -> "\\n"
            c == '\t' -> "\\t"
            c.code < 32 || c.code == 127 -> String.format("\\%03o", c.code)
            c.code in 32..126 -> c.toString()
            else -> String.format("\\u%04x", c.code)
        }
    }

    fun valorPorDefecto(tipo: String): String {
        return when (tipo) {
            "dbe" -> "0.0"
            "str" -> "\"\""
            "bln" -> "false"
            "crt" -> "'\\0'"
            else -> "0"
        }
    }

    /**
     * Codigo de una lista de sentencias. Las que quedan tras una sentencia que siempre retorna se omiten: en el
     * lenguaje fuente son validas, pero Java las rechaza con "unreachable statement" y nunca se ejecutarian.
     */
    fun sentencias(lista: List<Sentencia>): String {
        val sb = StringBuilder()
        for (s in lista) {
            sb.append(s.getCodeJava())
            if (!s.completaNormalmente()) break
        }
        return sb.toString()
    }

    fun completaNormalmente(lista: List<Sentencia>): Boolean = lista.all { it.completaNormalmente() }
}
