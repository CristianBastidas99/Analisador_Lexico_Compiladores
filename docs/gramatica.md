# Gramática y reglas semánticas

Este documento recoge la gramática (BNF) del lenguaje tal como la implementa
`sintaxis/AnalizadorSintactico.kt` (un analizador descendente recursivo con retroceso) y las reglas que
comprueba el análisis semántico (`semantico/` y el método `analizarSemantica` de cada nodo del árbol).

Los terminales en `negrita monoespaciada` son tokens del analizador léxico (ver [lenguaje.md](lenguaje.md)).
`ID` es un identificador (`^nombre`), `ENTERO` un literal `#…`, `DECIMAL` un literal `@…`, `CADENA` un literal
`'…'` y `CARACTER` un literal `°x°`. Los corchetes `[ ]` indican algo opcional y `{ }` repetición de cero o más
veces (esta última solo como notación de este documento; en el lenguaje las llaves `{` `}` son terminales y
aparecen entre comillas).

> La gramática de abajo se revisó función por función contra el código. Donde el comentario KDoc del código
> difiere de lo que el código hace, se documenta lo que hace el código y se anota en la sección 4.

## 1. Gramática (BNF)

### 1.1 Estructura del programa

```
<UnidadDeCompilacion> ::= <Visibilidad> cls ID "{" { <VariableGlobal> } { <Funcion> } "}"

<Visibilidad>         ::= pub | pvt

<VariableGlobal>      ::= <Visibilidad> <Tipo> ID "|"

<Tipo>                ::= ent | dbe | str | bln | crt

<Funcion>             ::= <Visibilidad> <ValorRetornado> ID "(" [ <ListaParametrosConTipo> ] ")"
                          "{" { <Sentencia> } "}"

<ValorRetornado>      ::= vd | <Tipo>

<ListaParametrosConTipo> ::= <ParametroConTipo> { ":" <ParametroConTipo> }

<ParametroConTipo>    ::= <Tipo> ID
```

- Primero van **todas** las variables globales y después **todas** las funciones; no se pueden mezclar.
- Después de la `}` que cierra la clase no puede haber nada, salvo comentarios (error
  `Hay código después de la llave que cierra la clase`).
- Una variable global **no lleva valor inicial**: `pub ent ^g |`.

### 1.2 Sentencias

```
<Sentencia> ::= <Decision> | <DeclaracionVariableInm> | <DeclaracionVariableMut> | <Asignacion>
              | <Impresion> | <Ciclo> | <Retorno> | <InvFuncion> | <IncrDecr>

<Decision>   ::= if "(" <ExpLogica> ")" "{" { <Sentencia> } "}"
                 [ else "{" { <Sentencia> } "}" ]

<Ciclo>      ::= whl "(" <ExpLogica> ")" "{" { <Sentencia> } "}"

<DeclaracionVariableMut> ::= <Tipo> ID "|"

<DeclaracionVariableInm> ::= <Tipo> <Asignacion>

<Asignacion> ::= ID <OperadorAsignacion> <ValorAsignado>

<ValorAsignado> ::= <Expresion> "|"
                  | <Lectura>
                  | <InvFuncion>
                  | ID "|"

<Lectura>    ::= rad "(" CADENA ")" "|"

<InvFuncion> ::= ID "(" [ <ListaParametrosSinTipo> ] ")" "|"

<ListaParametrosSinTipo> ::= ID { ":" ID }

<Impresion>  ::= imp "(" <Expresion> ")" "|"
               | imp "(" ID ")" "|"

<Retorno>    ::= ret <Expresion> "|"
               | ret ID "|"

<IncrDecr>   ::= ID ( "++" | "--" ) "|"

<OperadorAsignacion> ::= "=" | "+=" | "-=" | "*=" | "/=" | "%="
```

Observaciones:

- El orden en que `esSentencia()` prueba las alternativas es: decisión, declaración inmutable, declaración
  mutable, asignación, impresión, ciclo, retorno, invocación e incremento/decremento. Esto importa porque
  varias empiezan igual (`ID …`).
- `<Lectura>` y `<InvFuncion>` **incluyen su propio `|` final**, por eso en `<ValorAsignado>` solo
  `<Expresion>` e `ID` llevan el `|` explícito.
- No existe `else if`: tras `else` debe venir `{`. Para encadenar condiciones se anidan `if` dentro de `else { … }`.
- `ret` siempre lleva un valor (expresión o identificador). No existe `ret |` sin valor; por eso, en la
  práctica, una función `vd` nunca usa `ret` (hacerlo es un error semántico, ver sección 5).
- La invocación de función solo puede usarse como **sentencia** (`^f ( ^a ) |`) o como **valor de una
  asignación / declaración** (`^x = ^f ( ^a ) |`). No se puede usar dentro de una expresión, de un `imp` ni de un `ret`.

### 1.3 Expresiones

```
<Expresion>  ::= <ExpAritmetica> | <ExpRelacional> | <ExpLogica> | <ExpCadena>

<ExpAritmetica> ::= <Numero> [ <OperadorAritmetico> "(" <ExpAritmetica> ")" ]
<Numero>        ::= ENTERO | DECIMAL
<OperadorAritmetico> ::= "+" | "-" | "*" | "/" | "%"

<ExpRelacional> ::= "(" <ExpAritmetica> ")" <OperadorRelacional> "(" <ExpAritmetica> ")"
<OperadorRelacional> ::= "==" | "!=" | "<" | ">" | "<=" | ">="

<ExpLogica>  ::= [ not ] "(" <ExpRelacional> ")" [ <OperadorLogico> <ExpLogica> ]
<OperadorLogico> ::= and | or | xor

<ExpCadena>  ::= <ValorCadena> [ "+" <ExpCadena> ]
<ValorCadena> ::= ENTERO | DECIMAL | CADENA | CARACTER
```

`<Expresion>` prueba las alternativas **en ese orden** y se queda con la primera que reconoce.

## 2. Particularidades (verificadas contra el código)

1. **Las condiciones de `if` y `whl` llevan tres paréntesis.** La condición es una `<ExpLogica>`, que es
   `"(" <ExpRelacional> ")"`, y la relacional encierra cada lado en sus propios paréntesis. Además el `if`
   mismo pone sus paréntesis:

   ```
   if ( ( ( #1 ) > ( #2 ) ) ) { … }
   │  │ │ └────┬────┘     │
   │  │ └─ ( ExpRelacional ) = "(" ( #1 ) > ( #2 ) ")"
   │  └─ "(" ExpLogica ")" (paréntesis de la ExpLogica)
   └─ paréntesis del if
   ```

   Escribir `if ( ( #1 ) > ( #2 ) ) { … }` es un error sintáctico.
2. **Solo hay literales numéricos en aritmética y relacionales.** `<ExpAritmetica>` empieza siempre con un
   `ENTERO` o un `DECIMAL`: **no se pueden usar identificadores** (`( ^a ) > ( #1 )` no es válido). Por eso las
   condiciones del lenguaje son siempre constantes. El número no admite signo (`#-3` no existe; se escribe
   `#0 - ( #3 )`).
3. **Las operaciones aritméticas anidan por la derecha y con paréntesis**: `#1 + ( #2 * ( #3 ) )`. No hay
   precedencia ni encadenamiento por la izquierda: `#1 + ( #2 ) + ( #3 )` es un error sintáctico (tras
   `#1 + ( #2 )` la regla ya terminó y se esperaba el `|`); se escribe `#1 + ( #2 + ( #3 ) )`.
4. **Las expresiones lógicas se encadenan por la derecha**: `( A ) and not ( B ) or ( C )` equivale a
   `A and ( not B or ( C ) )`. El `not` solo puede ir delante de una relacional entre paréntesis, no delante de
   un operador lógico.
5. **Argumentos de invocación**: la lista de argumentos es una lista de **identificadores** separados por
   `:` (nunca literales ni expresiones): `^sumar ( ^x : ^y ) |`. La lista puede estar vacía: `^f ( ) |`.
   Los parámetros de la declaración llevan tipo (`ent ^a : ent ^b`) y se separan también con `:`.
6. **Concatenación**: `<ExpCadena>` une **solo literales** (`ENTERO`, `DECIMAL`, `CADENA`, `CARACTER`) con `+`;
   no admite identificadores. Una `<ExpCadena>` de un solo valor es lo mismo que un literal suelto.
   **Cuidado con el orden de prueba**: como `<ExpAritmetica>` se prueba antes, `#1 + 'a'` empieza reconociendo
   `#1` como aritmética y falla porque después del `+` esperaba un `(`; para concatenar con un número al
   inicio la primera pieza debe ser una cadena o un carácter: `'a' + #1`. (Una expresión de un único número
   se interpreta como aritmética de tipo `ent`/`dbe`, no como cadena.)
7. **`rad` solo puede ser el valor de una asignación o declaración** (`str ^s = rad ( 'Nombre' ) |`,
   `^e = rad ( 'Edad' ) |`); nunca dentro de `imp`, `ret` o una expresión. Su argumento es un literal de
   cadena (no un identificador).
8. **`imp` imprime una expresión o un identificador**: `imp ( 'hola' ) |`, `imp ( #1 + ( #2 ) ) |`,
   `imp ( ^a ) |`. No admite una invocación de función ni una lista de elementos: para imprimir varias cosas
   se encadenan varios `imp`. `imp` no agrega salto de línea; se escribe `\n` en la cadena.
9. **Declaración mutable vs inmutable**. Se distingue por la forma, no por una palabra clave: `ent ^a |` es
   mutable; `ent ^a = #1 |` es inmutable (y se traduce a `final`). Las palabras `mut` e `inm` están
   reservadas pero no se usan en la gramática.
10. **`tre` y `fls` no se usan**: no hay literales booleanos; una variable `bln` solo puede recibir una
    expresión relacional o lógica (`bln ^b = ( #1 ) > ( #2 ) |`), no `tre`/`fls`.
11. **Cuerpos vacíos** `{ }` son válidos en funciones, `if`, `else` y `whl`. Un archivo `pub cls ^a { }` también.
12. **Los comentarios se ignoran** en cualquier posición (el analizador sintáctico los salta).

## 3. Ejemplo mínimo válido de cada sentencia

| Sentencia | Ejemplo |
|-----------|---------|
| Unidad de compilación | `pub cls ^a { }` |
| Variable global | `pvt ent ^g \|` |
| Función con parámetros | `pub ent ^sumar ( ent ^a : ent ^b ) { ret ^a \| }` |
| Función sin parámetros | `pub vd ^main ( ) { }` |
| Declaración mutable | `ent ^a \|` |
| Declaración inmutable | `ent ^a = #1 \|` |
| Asignación (expresión) | `^a = #1 + ( #2 ) \|` |
| Asignación compuesta | `^a += #2 \|` |
| Asignación (identificador) | `^a = ^b \|` |
| Asignación con lectura | `^a = rad ( 'Escriba un valor' ) \|` |
| Declaración con lectura | `str ^s = rad ( 'Su nombre' ) \|` |
| Asignación con invocación | `^a = ^sumar ( ^x : ^y ) \|` |
| Declaración con invocación | `ent ^a = ^sumar ( ^x : ^y ) \|` |
| Invocación como sentencia | `^saludar ( ^nombre ) \|` |
| Impresión de expresión | `imp ( 'Hola\n' ) \|` |
| Impresión de identificador | `imp ( ^a ) \|` |
| Incremento / decremento | `^a ++ \|`  /  `^a -- \|` |
| Retorno de expresión | `ret #0 \|` |
| Retorno de identificador | `ret ^a \|` |
| Decisión | `if ( ( ( #1 ) > ( #2 ) ) ) { }` |
| Decisión con else | `if ( ( ( #1 ) == ( #1 ) ) ) { } else { }` |
| Ciclo | `whl ( ( ( #1 ) < ( #2 ) ) ) { }` |
| Condición compuesta | `if ( ( ( #1 ) < ( #2 ) ) and not ( ( #3 ) == ( #4 ) ) ) { }` |
| Aritmética anidada | `ent ^a = #1 + ( #2 * ( #3 ) ) \|` |
| Cadena concatenada | `str ^s = 'a' + #1 + °c° \|` |

(En la tabla, `\|` es el símbolo `|` escrito dentro de una celda de Markdown.)

### Programa completo

<!-- programa-completo -->
```
pub cls ^calc {
	pvt ent ^total |

	pub ent ^doble ( ent ^n ) {
		ent ^r = ^n |
		ret ^r |
	}

	pub vd ^main ( ) {
		ent ^i |
		^i = #3 |
		whl ( ( ( #1 ) > ( #2 ) ) ) {
			^i ++ |
		}
		^total = ^doble ( ^i ) |
		imp ( ^total ) |
	}
}
```

Más programas completos y válidos están en la carpeta `ejemplos/`.

## 4. Diferencias entre los comentarios KDoc del código y lo que hace el código

Los comentarios del analizador sintáctico usan nombres de una versión anterior en inglés. Se documenta aquí lo
que el **código** hace:

| Regla | KDoc dice | El código hace |
|-------|-----------|----------------|
| Unidad de compilación | `Class` | la palabra reservada es `cls` |
| Variable global | `<Visibilidad> <Tipo> Identificador "\|"` | igual (el KDoc omite la comilla de cierre) |
| Tipo | `ent \| dbe \| str \| booleano \| Caracter` | `ent \| dbe \| str \| bln \| crt` |
| Valor retornado | `Void \| <Tipo>` | `vd \| <Tipo>` |
| Lista de parámetros con tipo | `<Parámetro Con Tipo> [":" <Lista de Parámetros>]` | lo que sigue al `:` es de nuevo una lista de parámetros **con tipo** (el nombre `<Lista de Parámetros>` del KDoc no está definido) |
| Decisión / ciclo | `if "(" <Expresión Lógica> ")" …` | igual, pero la expresión lógica ya trae sus propios paréntesis (tres en total) |
| Expresión relacional | `<ExpAritmetica> OpeRacional "(" <ExpAritmetica> ")"` | **ambos** lados van entre paréntesis: `"(" <ExpAritmetica> ")" OpeRel "(" <ExpAritmetica> ")"` |
| Expresión cadena (`<TipoExpCadena>`) | `ent \| dbe \| str \| crt` (nombres de tipo) | son los **literales** `ENTERO \| DECIMAL \| CADENA \| CARACTER` |
| Declaración mutable / inmutable | usan `<TipoVariable>` | `<Tipo>`; la inmutable es `<Tipo> <Asignacion>` (la asignación aporta su `\|`) |
| Asignación | `<TipoAsig> ::= <Expresión> \| identificador \| <Lectura> \| <InvFunción>` y luego `"\|"` | `<Lectura>` y `<InvFunción>` ya consumen su propio `\|`; las otras dos alternativas llevan `\|` propio |
| Impresión | `print "(" <TipoImpresion> ")" "\|"` | la palabra es `imp`; `<TipoImpresion>` es `<Expresión> \| identificador` |
| Lectura | `read "(" cadena ")" "\|"` | la palabra es `rad` y la cadena es un literal `'…'` |
| Lista de parámetros sin tipo | `<Parámetro Sin Tipo> [":" <Lista de Parámetros sin Tipo>]` | igual; además, si un argumento empieza por un tipo (`ent`, `str`…) se informa `Este parámetro no lleva tipo` |

Las diferencias son solo de nombres y notación; ninguna cambia el lenguaje aceptado respecto de lo descrito en
las secciones 1 y 2. (La especificación de los autómatas léxicos está en `docs/lenguaje.md`.)

## 5. Reglas semánticas

El análisis semántico corre en dos pasadas sobre el árbol: primero llena la **tabla de símbolos** (detecta las
redeclaraciones) y después **analiza** cada sentencia. Solo se ejecuta si no hubo errores léxicos ni sintácticos.
Todos los mensajes llevan la fila y la columna del elemento señalado.

### 5.1 Ámbitos

Cada símbolo se guarda con un ámbito con forma de ruta:

- la clase: `^clase` (variables globales y funciones);
- cada función: `^clase/^funcion:tipoRetorno` (por ejemplo `^calc/^sumar:ent`); sus parámetros y variables locales
  viven aquí;
- cada bloque `if`, `else` o `whl` abre un ámbito anidado, identificado por la posición del operador relacional
  de su condición (por ejemplo `^calc/^main:vd/if@12:20`).

La **búsqueda** de un nombre empieza en el ámbito actual y sube por los ámbitos contenedores hasta la clase. Por
eso una variable local puede **ocultar** otra de un ámbito exterior (un `ent ^x` dentro de un `if` es válido
aunque exista otro `^x` fuera), pero **no** se puede declarar dos veces el mismo nombre en el *mismo* ámbito.
Los hermanos (el `if` y el `else` de una misma decisión, dos `whl`) no se ven entre sí.

### 5.2 Comprobaciones y mensajes

| Regla | Mensaje exacto |
|-------|----------------|
| Redeclaración de una variable, parámetro o global en el mismo ámbito | `El campo con el nombre ^a ya existe dentro del ámbito ^t/^main:vd` |
| Redeclaración de una función (mismo nombre **y** mismos tipos de parámetros, sin importar el tipo de retorno) | `La función con el nombre ^f ya existe dentro del ámbito ^t` |
| Asignar a una variable no declarada | `La variable (^b) que intenta dar asignación no se ha declarado en el ámbito ^t/^main:vd` |
| Tipo de la expresión distinto del de la variable | `El tipo de dato de la expresión (str) no coincide con el tipo de la variable (ent)` |
| Asignar un identificador de otro tipo | `El tipo de dato del identificador (str) no coincide con el tipo de la variable (ent)` |
| Asignar un identificador no declarado | `La variable (^b) que intenta dar como asignación no se ha declarado` |
| Modificar una variable inmutable (asignación, compuesta, `++`, `--`) | `La variable (^a) es inmutable y no se puede modificar` |
| Imprimir un identificador no declarado | `La variable (^c) que intenta imprimir no se ha declarado` |
| `++` / `--` sobre una variable no declarada | `La variable (^z) que intenta incrementar o decrementar no se ha declarado` |
| `++` / `--` sobre una variable que no es `ent` | `El tipo de dato del identificador (^d) no coincide con el tipo admitido (ent)` |
| Argumento de invocación no declarado | `La variable (^s) que intenta dar como argumento no se ha declarado` |
| Función invocada inexistente (no hay una con ese nombre y esos tipos de argumentos) | `La función invocada no existe` |
| Tipo retornado por la función invocada distinto del de la variable destino | `El tipo de la invocación de función (ent) es diferente al de la variable (str)` |
| `rad` asignado a una variable que no es `str`, `ent` ni `dbe` | `La variable (^b) es de tipo bln: la lectura (rad) solo se puede asignar a variables str, ent o dbe` |
| `ret` con expresión de tipo distinto al de la función | `El tipo de dato de la expresión (str) que desea retornar no es correcto en la función (ent)` |
| `ret` con identificador de tipo distinto al de la función | `El tipo de dato del identificador (str) no coincide con el valor de retorno de la función (ent)` |
| `ret` de un identificador no declarado | `La variable (^zz) que intenta retornar no se ha declarado` |
| `ret` dentro de una función `vd` | `La función (^t/^main:vd) no debe retornar ningún valor` |
| `^main` de tipo `vd` con parámetros | `La función ^main no puede tener parámetros` |

### 5.3 Detalles de cada regla

- **Tipos de las expresiones.** Un literal entero o una aritmética de enteros es `ent`; si el primer número es
  decimal es `dbe`; en una aritmética anidada de primer número entero, el tipo es el de la subexpresión (así
  `#1 + ( @1.5 )` es `dbe`). Una relacional o lógica es `bln`. Una cadena `'…'` es `str`; un carácter suelto
  `°x°` es `crt`; cualquier concatenación con `+` es `str`. No hay conversiones implícitas: `dbe ^d = #1 |`
  es un error (el literal es `ent`).
- **Las funciones se identifican por nombre y tipos de parámetros**, no solo por nombre; es decir, se admite
  la sobrecarga: `^f ( ent ^a )` y `^f ( str ^b )` pueden coexistir. Una invocación busca la función
  usando los tipos de los *identificadores* pasados como argumentos. **Limitación**: el ámbito de una función
  es `^clase/^nombre:tipoRetorno`, sin los tipos de los parámetros; dos sobrecargas con el mismo nombre y el
  mismo tipo de retorno comparten ámbito, así que sus parámetros y variables locales **no pueden repetir
  nombre** (`^f ( ent ^a )` y `^f ( str ^a )` dan `El campo con el nombre ^a ya existe dentro del ámbito ^c/^f:ent`).
- **Retorno.** Para funciones con tipo se comprueba el tipo de cada `ret`. **No** se exige que todas las
  rutas terminen en `ret` (el código Java generado añade un `return` con valor por defecto al final si hace falta).
  El código escrito después de un `ret` seguro se acepta, pero no se traduce a Java.
- **Inmutables.** Una variable declarada con valor (`ent ^a = #1 |`) no puede cambiar. La propia asignación de la
  declaración no cuenta como modificación. Las variables globales y los parámetros son siempre mutables.
- **`^main`.** Si una función se llama `^main` y es `vd`, no puede declarar parámetros (se traduce a
  `main(String[] args)`). No se comprueba que exista una función `^main`: sin ella el programa se traduce pero
  no se puede ejecutar.
- **No se comprueba** el tipo de las condiciones (siempre son números), que una variable se declare *antes* de
  usarla dentro del mismo ámbito (la tabla de símbolos se llena completa antes de analizar), ni que los
  operadores compuestos (`+=`, `-=`…) tengan sentido para el tipo de la variable.
  Por ejemplo, `^b = #1 |` antes de `ent ^b |` pasa el semántico, pero el Java generado no compila.
