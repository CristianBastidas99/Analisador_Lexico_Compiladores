# Traducción del lenguaje a Java

El compilador traduce un programa correcto (sin errores léxicos, sintácticos ni semánticos) a una clase Java
que, en general, se puede compilar (el análisis semántico no detecta, por ejemplo, el uso de una variable antes de
declararla en el mismo ámbito, que Java rechazaría; ver la sección 5 de [gramatica.md](gramatica.md)) y ejecutar con `javac` y `java`. La generación está en los métodos `getCodeJava()` de
cada nodo del árbol (`sintaxis/`) y en las utilidades de `sintaxis/CodigoJava.kt`.

## 1. Estructura general

El código generado siempre empieza con la importación de `JOptionPane` (la usa `rad`) y contiene una sola
clase con el nombre de la clase del programa sin el `^`:

```
import javax.swing.JOptionPane;

public class nombre {
    <variables globales>
    <funciones>
}
```

## 2. Tabla de equivalencias

### 2.1 Tipos

| Lenguaje | Java | Valor por defecto (variable mutable) |
|----------|------|--------------------------------------|
| `ent`    | `int`     | `0`    |
| `dbe`    | `double`  | `0.0`  |
| `str`    | `String`  | `""`   |
| `bln`    | `boolean` | `false` |
| `crt`    | `char`    | `'\0'` |
| `vd`     | `void`    | (no aplica) |

### 2.2 Estructura, visibilidad y declaraciones

| Lenguaje | Java |
|----------|------|
| `pub cls ^a { … }` | `public class a { … }` |
| `pvt cls ^a { … }` | `class a { … }` (una clase de primer nivel no puede ser `private`: `pvt` se traduce a visibilidad de paquete, sin modificador) |
| `pub ent ^g \|` (global) | `public static int g;` (las globales son siempre `static`; no se inicializan, así que valen `0`, `0.0`, `null` o `false` según el tipo) |
| `pvt ent ^g \|` | `private static int g;` |
| `pub ent ^f ( ent ^a : ent ^b ) { … }` | `public static int f ( int a, int b ){ … }` (las funciones son siempre `static`; el separador `:` pasa a `,`) |
| `pvt ent ^f ( ) { … }` | `private static int f (  ){ … }` |
| `pub vd ^main ( ) { … }` | `public static void main ( String[] args ){ … }` |
| `ent ^a \|` (mutable, sin valor) | `int a = 0;` (se inicializa con el valor por defecto de su tipo, porque Java no deja leer una variable sin inicializar) |
| `ent ^a = #5 \|` (inmutable) | `final int a = 5 ;` |
| `^a = #5 \|` | `a = 5 ;` |
| `^a += #2 \|` | `a += 2 ;` |
| `^a ++ \|` / `^a -- \|` | `a++;` / `a--;` |
| `ret ^a \|` | `return a;` |

Notas:

- Solo `^main` de tipo `vd` se convierte en `main(String[] args)` y siempre es `public`, aunque se declare
  `pvt`. Por eso el semántico prohíbe que esa función tenga parámetros (se perderían). Una función `^main` de
  otro tipo se traduce como una función cualquiera.
- El programador **no escribe** `inm` ni `mut`. El `final` sale de declarar la variable **con valor inicial**
  (`ent ^a = #5 |`); una variable declarada sin valor (`ent ^a |`) es la mutable. Una variable inmutable no puede
  reasignarse (el compilador del lenguaje ya lo rechaza antes de generar Java). Ver [gramatica.md](gramatica.md).

### 2.3 Literales

| Lenguaje | Java | Explicación |
|----------|------|-------------|
| `#5` | `5` | se quita el `#` |
| `#09`, `#007` | `9`, `7` | se quitan los ceros a la izquierda (en Java `09` sería un octal inválido); `#0` → `0` |
| `@1.5` | `1.5` | se quita el `@` |
| `@1.` | `1.0` | parte decimal vacía → `0` |
| `@.5` | `0.5` | parte entera vacía → `0` |
| `@.` | `0.0` | |
| `@007.50` | `7.50` | |
| `'Hola\n'` | `"Hola\n"` | las comillas simples pasan a dobles |
| `'dijo \'hola\''` | `"dijo 'hola'"` | `\'` pasa a `'` dentro de una cadena Java |
| `'a "b"'` | `"a \"b\""` | las comillas dobles reales se escapan |
| `'a\\b'` | `"a\\b"` | |
| `°a°` | `'a'` | un carácter es un `char` |
| `°\n°`, `°\t°` | `'\n'`, `'\t'` | |
| `°'°`, `°\'°` | `'\''` | |
| `°"°`, `°\"°` | `'\"'` | |
| `°\\°` | `'\\'` | |

Los caracteres de control y los no ASCII dentro de cadenas se escriben con escapes de Java (`\r`, octales como
`\001`, o `\uXXXX`) para no romper el literal. Un salto de línea real dentro de una cadena se traduce a `\n`.

### 2.4 Operadores y expresiones

| Lenguaje | Java |
|----------|------|
| `+ - * / %` | igual |
| `= += -= *= /= %=` | igual |
| `== != < > <= >=` | igual |
| `and` | `&&` |
| `or`  | `\|\|` |
| `xor` | `^` (sobre booleanos es el o exclusivo) |
| `not ( … )` | `!( … )` |
| `( #1 ) > ( #2 )` | `(1 ) > (2 )` |
| `#1 + ( #2 * ( #3 ) )` | `1 + (2 * (3 ))` |
| `( A ) and ( B )` | `(A) && ((B))` (cada relacional queda entre paréntesis) |
| `'a' + #1 + °c°` | `"" + "a" + 1 + 'c'` (el prefijo `""` obliga a concatenar texto; sin él, en Java `'a' + 'b'` sumaría números) |
| `imp ( ^a ) \|` | `System.out.print(a);` |
| `imp ( 'Hola' ) \|` | `System.out.print("Hola");` |

`imp` usa `System.out.print` (no `println`): no agrega salto de línea; hay que escribir `\n` en la cadena.

### 2.5 Sentencias de control

| Lenguaje | Java |
|----------|------|
| `if ( C ) { … }` | `if ( C ) { … }` |
| `if ( C ) { … } else { … }` | `if ( C ) { … } else { … }` |
| `whl ( C ) { … }` | `while ( Boolean.valueOf( C ) ) { … }` |

**¿Por qué `Boolean.valueOf(...)` en el `whl`?** Las condiciones del lenguaje solo se forman con literales, así
que en Java serían *expresiones constantes*. Para `while ( (1) > (2) )` el compilador de Java sabe que la
condición es siempre falsa y rechaza el cuerpo como "código inalcanzable"; para `while ( (2) > (1) )` rechaza
cualquier código posterior al ciclo. `Boolean.valueOf(…)` hace que la condición deje de ser constante, y el
programa compila y se comporta como se espera (el cuerpo no se ejecuta, o el ciclo es infinito, según la
condición). El `if` no tiene este problema y se traduce directamente.

### 2.6 Lectura: `rad`

`rad ( 'mensaje' )` abre un cuadro de diálogo (`JOptionPane.showInputDialog`) y devuelve un `String`. Según el
tipo de la variable que recibe el valor, el texto se convierte:

| Lenguaje | Java |
|----------|------|
| `str ^s = rad ( 'Nombre' ) \|` | `final String s = JOptionPane.showInputDialog("Nombre");` |
| `^e = rad ( 'Edad' ) \|` (con `^e` de tipo `ent`) | `e = Integer.parseInt(JOptionPane.showInputDialog("Edad"));` |
| `dbe ^d = rad ( 'Nota' ) \|` | `final double d = Double.parseDouble(JOptionPane.showInputDialog("Nota"));` |

Si el usuario cancela el diálogo o escribe algo que no es un número, el programa Java generado lanza una
excepción en tiempo de ejecución (no se maneja).

### 2.7 Renombrado de identificadores

Los identificadores del lenguaje pierden el `^`. Para que el Java generado compile, algunos nombres se renombran:

| Situación | Ejemplo en el lenguaje | Java generado |
|-----------|------------------------|---------------|
| El nombre es una palabra reservada de Java (o un nombre que usa el código generado: `args`, `String`, `System`, `JOptionPane`, `Boolean`, y los métodos de `Object` como `equals`…) | `ent ^class \|` | `int class$ = 0;` |
| Un parámetro llamado como un nombre reservado | `ent ^args` | `int args$` |
| Una variable de un ámbito interior repite el nombre de una variable visible (el lenguaje lo permite; Java no) | `ent ^x \|` y luego, dentro de un `if`, `ent ^x = #9 \|` | `int x = 0;` … `final int x$2 = 9 ;` |

El símbolo `$` es válido en Java pero el lenguaje fuente no puede producirlo en un identificador, así que los
nombres generados nunca chocan con los del programador. Las referencias posteriores usan el nombre renombrado
de la variable más interna visible.

### 2.8 Retorno

- **Código tras un `ret`.** En el lenguaje se puede escribir código después de un `ret`; Java lo rechaza
  (`unreachable statement`). El traductor **omite** las sentencias que quedan después de una sentencia que
  siempre retorna (un `ret`, o un `if`/`else` donde ambas ramas retornan). Ejemplo:

  | Lenguaje | Java |
  |----------|------|
  | `ret #1 \| imp ( 'b' ) \|` | `return 1 ;` (el `imp` no se genera) |

- **`return` por defecto.** Java exige que una función con tipo retorne en todos los caminos. Si el final de
  la función se puede alcanzar sin pasar por un `ret` (por ejemplo, un `if` sin `else` que retorna solo en la
  rama verdadera, o una función con cuerpo vacío), se añade al final `return <valor por defecto>;`
  (`0`, `0.0`, `""`, `false` o `'\0'`):

  | Lenguaje | Java |
  |----------|------|
  | `pub ent ^h ( ) { if ( ( ( #1 ) > ( #2 ) ) ) { ret #1 \| } }` | `public static int h (  ){ if ( ((1 ) > (2 )) ) { return 1 ; } return 0; }` (con los saltos de línea del generador) |

## 3. Ejemplo completo

Programa fuente (`ejemplos/03-funciones.txt`):

<!-- ejemplo-fuente -->
```text
& Ejemplo 3: funciones.
& Muestra variables globales, funciones con parámetros y retorno, funciones vd,
& invocación como sentencia y como valor, y variables inmutables.
pub cls ^calc {
	& Variable global (visible en todas las funciones)
	pvt ent ^total |

	& Función con dos parámetros (separados por :) que retorna un ent
	pub ent ^sumar ( ent ^a : ent ^b ) {
		ent ^res |
		^res = ^a |
		^res += ^b |
		ret ^res |
	}

	& Función vd con un parámetro: no retorna nada
	pub vd ^saludar ( str ^nombre ) {
		imp ( 'Hola, ' ) |
		imp ( ^nombre ) |
		imp ( '\n' ) |
	}

	pub vd ^main ( ) {
		& Invocación como sentencia (los argumentos son identificadores)
		str ^quien = 'estudiante' |
		^saludar ( ^quien ) |
		& Variables inmutables (se asignan al declararlas)
		ent ^x = #3 |
		ent ^y = #4 |
		& Invocación como valor, asignada a la global
		^total = ^sumar ( ^x : ^y ) |
		imp ( 'sumar(3, 4) = ' ) |
		imp ( ^total ) |
		imp ( '\n' ) |
		& Invocación como sentencia (el valor retornado se descarta)
		^sumar ( ^x : ^total ) |
		& Invocación como valor al declarar una variable inmutable
		ent ^doble = ^sumar ( ^total : ^total ) |
		imp ( 'doble = ' ) |
		imp ( ^doble ) |
		imp ( '\n' ) |
	}
}
```

Java generado por el compilador:

<!-- ejemplo-java -->
```java
import javax.swing.JOptionPane;

public class calc {

private static int total;


public static int sumar ( int a, int b ){

int res = 0;
res = a;
res += b;
return res;
}

public static void saludar ( String nombre ){

System.out.print("Hola, ");
System.out.print(nombre);
System.out.print("\n");
}

public static void main ( String[] args ){

final String quien = "estudiante";
saludar( quien );
final int x = 3 ;
final int y = 4 ;
total = sumar( x, y );
System.out.print("sumar(3, 4) = ");
System.out.print(total);
System.out.print("\n");
sumar( x, total );
final int doble = sumar( total, total );
System.out.print("doble = ");
System.out.print(doble);
System.out.print("\n");
}

}
```

Este ejemplo no se escribió a mano: la prueba `DocumentacionTest` (carpeta `test/`) lee los dos bloques
anteriores, compila el fuente con el compilador y comprueba que el Java documentado sea **exactamente** el que
se genera (ignorando los finales de línea). Si el generador cambia, la prueba falla hasta que este documento se
actualice.

### Qué observar en el ejemplo

- `pvt ent ^total |` → `private static int total;` (global, `static`).
- `^sumar ( ent ^a : ent ^b )` → `sumar ( int a, int b )`; el `:` se vuelve `,`.
- `ent ^res |` (mutable) → `int res = 0;` con su valor por defecto.
- `pub vd ^main ( )` → `public static void main ( String[] args )`.
- `str ^quien = 'estudiante' |` y `ent ^x = #3 |` (inmutables) → `final String quien = "estudiante";`, `final int x = 3 ;`.
- `^total = ^sumar ( ^x : ^y ) |` → `total = sumar( x, y );`.
- `^sumar ( ^x : ^total ) |` (invocación como sentencia) → `sumar( x, total );`.
- Cada `imp` → `System.out.print(…);` (los saltos de línea están en las cadenas, `'\n'`).
