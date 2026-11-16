# Especificación del lenguaje (nivel léxico)

Este documento describe cómo se escribe un programa en el lenguaje del curso de Compiladores: qué palabras
están reservadas, cómo se escriben los números, los identificadores, las cadenas y los comentarios, y qué
errores léxicos puede producir el analizador (`lexico/AnalizadorLexico.kt`).

La estructura de las sentencias (gramática) está en [gramatica.md](gramatica.md) y la traducción a Java en
[traduccion.md](traduccion.md).

## 1. Convenciones generales

- **Espacios en blanco**: el espacio, el tabulador (`\t`), el salto de línea (`\n`) y el retorno de carro (`\r`)
  se ignoran entre tokens (los archivos con finales de línea de Windows, `\r\n`, funcionan sin problema).
- **Filas y columnas**: ambas se numeran **desde 1**. Cada carácter ocupa exactamente una columna, incluido el
  tabulador (un tabulador cuenta como **1** columna, no como 4 u 8) y el `\r`. Un salto de línea (`\n`) pasa a
  la fila siguiente y reinicia la columna a 1. Todos los errores (léxicos, sintácticos y semánticos) se
  informan con la fila y la columna de su token.
- **Maximal munch limitado**: el analizador prueba los autómatas en un orden fijo (palabras reservadas,
  enteros, decimales, identificadores, operadores lógicos, `++`, `--`, relacionales, asignación, aritméticos,
  agrupadores, `|`, `_`, `;`, `:`, comentarios, caracteres, cadenas). Gana el primero que reconoce el texto.
- Las palabras reservadas se reconocen **sin exigir un límite de palabra**: el texto `entero` se lee como la
  palabra `ent` seguida de `ero` (que no es válido). Por eso los nombres propios del programador siempre llevan
  el prefijo `^` (ver identificadores).

## 2. Palabras reservadas

El analizador reconoce 26 palabras reservadas (categoría `PALABRA_RESERVADA`). Algunas se usan en la gramática
actual y otras están **solo reservadas**: el analizador las reconoce como token, pero el analizador sintáctico
todavía no las admite en ninguna sentencia (usarlas produce un error sintáctico).

| Palabra | Significado / equivalente en Java | ¿Se usa en la gramática? |
|---------|-----------------------------------|--------------------------|
| `ent`   | `int` (tipo entero)               | Sí (tipo)                |
| `dbe`   | `double` (tipo decimal)           | Sí (tipo)                |
| `str`   | `String` (tipo cadena)            | Sí (tipo)                |
| `bln`   | `boolean` (tipo lógico)           | Sí (tipo)                |
| `crt`   | `char` (tipo carácter)            | Sí (tipo)                |
| `vd`    | `void` (función sin retorno)      | Sí (valor retornado)     |
| `pub`   | `public`                          | Sí (visibilidad)         |
| `pvt`   | `private` (en la clase se traduce a visibilidad de paquete) | Sí (visibilidad) |
| `cls`   | `class`                           | Sí (clase)               |
| `if`    | `if`                              | Sí (decisión)            |
| `else`  | `else`                            | Sí (decisión)            |
| `whl`   | `while`                           | Sí (ciclo)               |
| `ret`   | `return`                          | Sí (retorno)             |
| `imp`   | `System.out.print` (imprimir)     | Sí (impresión)           |
| `rad`   | leer un dato (`JOptionPane.showInputDialog`) | Sí (lectura)  |
| `mut`   | variable mutable                  | Solo reservada (ver nota) |
| `inm`   | variable inmutable (`final`)      | Solo reservada (ver nota) |
| `tre`   | `true`                            | Solo reservada           |
| `fls`   | `false`                           | Solo reservada           |
| `abs`   | `abstract`                        | Solo reservada           |
| `cth`   | `catch`                           | Solo reservada           |
| `brk`   | `break`                           | Solo reservada           |
| `cas`   | `case`                            | Solo reservada           |
| `ptt`   | `protected`                       | Solo reservada       |
| `cat`   | sin equivalente definido en el código | Solo reservada       |
| `array` | arreglo (sin soporte aún)         | Solo reservada           |

**Nota sobre `mut` e `inm`.** Aunque las palabras existen, la gramática actual **no las escribe el
programador**: la mutabilidad se deduce de la forma de la declaración.

- `ent ^x |` (sin valor) declara una variable **mutable**.
- `ent ^x = #5 |` (con valor inicial) declara una variable **inmutable** (se traduce a `final`). Cualquier
  asignación posterior a ella es un error semántico.

Además de estas, los **operadores lógicos** `and`, `or`, `xor` y `not` también son palabras (ver sección 7).

## 3. Literales numéricos

### 3.1 Enteros: `#` seguido de uno o más dígitos

Autómata: `#` D D\*. Categoría `ENTERO`.

| Válidos | Inválidos |
|---------|-----------|
| `#0`, `#5`, `#09`, `#007`, `#12345` | `#` (falta el dígito), `#a`, `5` (sin `#`), `#-3` (el signo no forma parte del literal) |

Los ceros a la izquierda se aceptan (`#09` es válido y vale 9).

### 3.2 Decimales: `@` D\* `.` D\*

Autómata: `@`, cero o más dígitos, un punto obligatorio y cero o más dígitos. Categoría `DECIMAL`. Las dos
partes de dígitos son **opcionales**, pero el punto no.

| Literal | ¿Válido? | Valor en Java |
|---------|----------|---------------|
| `@1.5`  | Sí       | `1.5`         |
| `@1.`   | Sí (parte decimal vacía) | `1.0` |
| `@.5`   | Sí (parte entera vacía)  | `0.5` |
| `@.`    | Sí (ambas vacías)        | `0.0` |
| `@007.50` | Sí     | `7.50`        |
| `@12`   | **No**: falta el punto   | error léxico |
| `@`     | **No**: falta el punto   | error léxico |
| `1.5`   | **No**: falta el `@`     | error léxico (`1` no es un carácter reconocido) |

## 4. Identificadores

Autómata: `^` + una **letra** + hasta 8 caracteres alfanuméricos más. Longitud máxima: **10 caracteres
contando el `^`** (el `^`, una letra y hasta 8 letras o dígitos). Categoría `IDENTIFICADOR`.

| Válidos | Inválidos |
|---------|-----------|
| `^a`, `^total`, `^x1`, `^abcdefghi` (10 con `^`) | `^1a` (no empieza por letra), `^` (vacío), `total` (sin `^`), `^a_b` (el `_` no es alfanumérico), `^abcdefghij` (11 con `^`) |

Un identificador más largo de 10 caracteres genera el error `Identificador demasiado largo` (ver sección 10):
se conserva el identificador cortado a 10 caracteres y el exceso se convierte en un token desconocido.

Las letras y dígitos son los que reconoce Kotlin (`isLetter`, `isDigit`), por lo que las letras con tilde
(`^año`) también son válidas.

## 5. Cadenas y caracteres

### 5.1 Cadenas: `'…'`

Una cadena empieza y termina con comilla simple (`'`). Categoría `CADENA_CARACTERES`. Puede contener cualquier
carácter excepto `\` y `'` sueltos; incluye comillas dobles (`"`), tabuladores y saltos de línea reales.

Escapes permitidos (la barra invertida solo puede ir seguida de uno de estos cinco caracteres):

| Escape | Significado |
|--------|-------------|
| `\n`   | salto de línea |
| `\t`   | tabulador |
| `\"`   | comilla doble |
| `\'`   | comilla simple |
| `\\`   | barra invertida |

Ejemplos válidos: `'Hola mundo\n'`, `'Dijo \'hola\''`, `'a\\b'`, `'comillas "dobles"'`, `''` (cadena vacía).
Inválidos: `'hola` (sin cerrar), `'a\qb'` (escape `\q` no existe).

### 5.2 Caracteres: `°x°`

Un carácter se escribe entre dos símbolos grado (`°`) y contiene **exactamente un** símbolo, que puede ser un
carácter cualquiera (excepto `°`) o uno de los cinco escapes anteriores. Categoría `CARACTER`.

Ejemplos válidos: `°a°`, `°7°`, `°\n°`, `°\t°`, `°\'°`, `°\"°`, `°\\°`, `°'°`, `°"°`.
Inválidos: `°°` (vacío), `°ab°` (dos símbolos), `°a` (sin cerrar; error `Carácter sin cerrar o mal formado…`), `°\q° (escape inválido).

## 6. Agrupadores, separadores y símbolos especiales

Comprobados en el código (`AnalizadorLexico.kt`):

| Símbolo | Categoría | Uso |
|---------|-----------|-----|
| `(` `)` | `PARENTESIS_IZQUIERDO` / `PARENTESIS_DERECHO` | agrupar condiciones, expresiones, parámetros y argumentos |
| `{` `}` | `LLAVE_IZQUIERDO` / `LLAVE_DERECHO` | cuerpo de la clase, de las funciones y de if/else/whl |
| `[` `]` | `CORCHETE_IZQUIERDO` / `CORCHETE_DERECHO` | se reconocen como tokens, pero la gramática actual no los usa |
| `\|`    | `FIN_SENTENCIA` | **termina** casi todas las sentencias y las variables globales |
| `:`     | `SEPARADOR` | separa parámetros (`ent ^a : ent ^b`) y argumentos (`^x : ^y`) |
| `_`     | `PUNTO` | el "punto" del lenguaje; se reconoce como token pero la gramática actual no lo usa |
| `;`     | `DOS_PUNTOS` | llamado "dos puntos" en el lenguaje; se reconoce como token pero la gramática actual no lo usa |

Atención a la confusión de nombres: el símbolo **`:`** es la categoría `SEPARADOR`; el **`;`** es la
categoría `DOS_PUNTOS` y el **`_`** es la categoría `PUNTO`. Solo `|` y `:` aparecen en programas válidos.

## 7. Operadores

| Grupo | Símbolos | Categoría |
|-------|----------|-----------|
| Aritméticos | `+` `-` `*` `/` `%` | `OPERADOR_ARITMETICO` |
| Asignación | `=` | `OPERADOR_ASIGNACION` |
| Asignación compuesta | `+=` `-=` `*=` `/=` `%=` | `OPERADOR_ASIGNACION` |
| Incremento | `++` | `OPERADOR_INCREMENTO` |
| Decremento | `--` | `OPERADOR_DECREMENTO` |
| Relacionales | `==` `!=` `<` `>` `<=` `>=` | `OPERADOR_RELACIONAL` |
| Lógicos | `and` `or` `xor` `not` | `OPERADOR_LOGICO` |

Observaciones:

- `==` es el operador de igualdad (relacional); `=` solo asigna. Un `!` suelto no es válido: solo existe `!=`.
- `++` y `--` se reconocen antes que `+` y `-`; `+=` y `-=` antes que `+`/`-` por separado, etc. Así, `^i ++ |`
  es un incremento y `^i += #2 |` es una asignación compuesta. Con el texto `^a+++b` el analizador lee `++`
  y luego `+`.
- Los operadores lógicos se escriben con letras (`and`, `or`, `xor`, `not`), no con símbolos como `&&`.
- La asignación compuesta se deja pasar tal cual a Java; el análisis semántico solo comprueba tipos y
  mutabilidad, no que el operador tenga sentido para el tipo (por ejemplo `+=` en un `bln`).

## 8. Comentarios

### 8.1 Comentario de línea: `&…`

Empieza con `&` y llega hasta el salto de línea, que **forma parte del comentario**. Categoría
`COMENTARIO_LINEA`. El autómata exige que termine en salto de línea: si el archivo termina sin `\n`
tras un comentario de línea, se reporta un error léxico (`Comentario de línea sin salto de línea final`).
En la práctica, el último comentario del archivo debe ir seguido de un salto de línea.

```
& Esto es un comentario de línea
ent ^a |   & también al final de una sentencia
```

### 8.2 Comentario de bloque: `"""…"""`

Empieza con tres comillas dobles y termina en la primera aparición de tres comillas dobles consecutivas.
Puede abarcar varias líneas. Categoría `COMENTARIO_BLOQUE`. Si no se cierra, es un error léxico
(`Comentario de bloque sin cerrar`) y **todo el resto del archivo** se consume como un token desconocido.

```
"""
Comentario de
varias líneas
"""
```

Los comentarios (de línea y de bloque) los produce el analizador léxico pero el analizador sintáctico los
**descarta** (los salta al pedir el siguiente token), así que pueden aparecer en cualquier lugar.

## 9. Categorías de token

Tabla de `lexico/Categoria.kt` (25 categorías):

| Categoría | Qué reconoce |
|-----------|--------------|
| `PALABRA_RESERVADA` | las 26 palabras de la sección 2 (`and`, `or`, `xor` y `not` no están aquí: son `OPERADOR_LOGICO`) |
| `ENTERO` | `#` dígitos |
| `DECIMAL` | `@` D\* `.` D\* |
| `IDENTIFICADOR` | `^` letra alfanuméricos (máx. 10) |
| `OPERADOR_ARITMETICO` | `+ - * / %` |
| `OPERADOR_LOGICO` | `and or xor not` |
| `OPERADOR_ASIGNACION` | `= += -= *= /= %=` |
| `OPERADOR_INCREMENTO` | `++` |
| `OPERADOR_DECREMENTO` | `--` |
| `OPERADOR_RELACIONAL` | `== != < > <= >=` |
| `PARENTESIS_IZQUIERDO` / `PARENTESIS_DERECHO` | `(` / `)` |
| `LLAVE_IZQUIERDO` / `LLAVE_DERECHO` | `{` / `}` |
| `CORCHETE_IZQUIERDO` / `CORCHETE_DERECHO` | `[` / `]` |
| `FIN_SENTENCIA` | `\|` |
| `PUNTO` | `_` |
| `DOS_PUNTOS` | `;` |
| `SEPARADOR` | `:` |
| `COMENTARIO_LINEA` | `&…` + salto de línea |
| `COMENTARIO_BLOQUE` | `"""…"""` |
| `CADENA_CARACTERES` | `'…'` |
| `CARACTER` | `°x°` |
| `DESCONOCIDO` | todo lo que ningún autómata reconoce (siempre va acompañado de un error léxico) |

En total hay 26 palabras reservadas más los 4 operadores lógicos escritos con letras.

## 10. Errores léxicos

Un carácter o grupo de caracteres que ningún autómata reconoce se guarda como token `DESCONOCIDO` y se
reporta un error con su fila y columna; el análisis **continúa** con el resto del programa. Mensajes reales:

| Situación | Mensaje | Qué se consume como `DESCONOCIDO` |
|-----------|---------|-----------------------------------|
| Carácter que no pertenece al lenguaje (`$`, `~`, una letra suelta como `x`, `1`…) | `Carácter no reconocido: '$'` (los caracteres de control se muestran escapados: `'\n'`, `'\t'`, `'\u0001'`) | ese único carácter |
| `#` sin dígito | `Entero mal formado: después de '#' se esperaba al menos un dígito` | el `#` |
| `@` sin punto y sin dígitos | `Decimal mal formado: después de '@' se esperaba '.' con dígitos opcionales` | `@` |
| `@` con dígitos y sin punto (`@12`) | `Decimal mal formado: falta el punto ('@12')` | `@12` |
| `^` sin letra a continuación | `Identificador mal formado: después de '^' se esperaba una letra ('^1a')` | `^` y los alfanuméricos que siguen |
| Identificador de más de 10 caracteres | `Identificador demasiado largo: máximo 10 caracteres incluyendo '^' ('^abcdefghi')` | el exceso de letras/dígitos |
| Cadena con escape inválido | `Cadena mal formada: secuencia de escape no válida '\q'` | hasta el fin de la línea (ver nota) |
| Cadena sin comilla de cierre | `Cadena sin cerrar: falta la comilla simple de cierre` | hasta el fin de la línea (ver nota) |
| `°°` | `Carácter mal formado: no puede estar vacío` | el carácter mal formado |
| Carácter con escape inválido | `Carácter mal formado: secuencia de escape no válida '\q'` | el carácter mal formado |
| Carácter sin cerrar, solo cuando el `°` está al final del archivo o tras un escape válido (`°\n`) | `Carácter sin cerrar: falta el símbolo ° de cierre` | hasta el siguiente `°` o fin de línea |
| Carácter con más de un símbolo o sin cerrar (`°ab°`, `°a`, `°a \|`) | `Carácter sin cerrar o mal formado: se esperaba un solo símbolo seguido de °` | hasta el siguiente `°` o fin de línea |
| `"""` sin cierre | `Comentario de bloque sin cerrar: falta el cierre """` | todo el resto del archivo |
| Una comilla doble sola o de a dos (`"`, `""`) | `Carácter no reconocido: '"'` (uno por **cada** comilla: `""` produce dos errores) | cada comilla |
| `&…` al final del archivo sin salto de línea | `Comentario de línea sin salto de línea final` | el comentario |

**Nota sobre cadenas mal formadas.** Como una cadena puede contener saltos de línea reales, el analizador empareja la comilla de apertura con la **siguiente comilla simple del archivo**, aunque esté varias líneas más abajo. Con `imp ( 'hola ) |` seguido en la línea 2 de `imp ( 'x' ) |`, la cadena válida es `'hola ) |` + salto + `imp ( '`; después `x` es un carácter no reconocido (2:8) y el último `'` queda sin cerrar (error `Cadena sin cerrar` en 2:9). Solo cuando ya no queda ninguna comilla en el archivo se reporta la cadena sin cerrar en el punto donde empezó (y entonces el token desconocido llega hasta el fin de esa línea).

Cuando hay errores léxicos, la compilación **no continúa** a las fases siguientes (sintáctica y semántica).

## 11. Programa mínimo

```
pub cls ^hola {
	pub vd ^main ( ) {
		imp ( 'Hola mundo\n' ) |
	}
}
```
