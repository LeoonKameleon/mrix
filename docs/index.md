# Mrix Language Documentation

## 1. Introduction
<p align="justify">
Mrix is a dynamically-typed, interpreted language designed for matrix manipulation. The interpreter is implemented in Java 23.
</p>

## 2. Data Types
Mrix supports following data types:
+ **INT** - 64-bit signed integer.
  + Example: `int_value = 5;`

+ **FLOAT** - 64-bit floating point.
  + Example: `float_value = 6.7;`

+ **BOOL** - Logical values `true` or `false`.
  + Example: `boolean_value = true;`
  + **Note**: For numerical and matrix operations, booleans are automatically evaluated as **FLOAT** values: `true == 1.0` and `false == 0.0`.

+ **STRING** - Sequence of characters.
  + Example: `string_sequence = "mrix";`
  + Supports standard escape sequences (`\n`, `\t`, `\"`, etc.).

+ **MATRIX** - A 2D (or 1D) array of numerical values (**INT**/**FLOAT**).
  + Example: ```matrix = [[1, 2], [3.3, 4]];```

+ **TUPLE** - An immutable, ordered collection of values of any type.
  + Example: `my_tuple = (1, "text", [1, 2]);`

+ *NULL* - Represents the absence of a value, it is used to indicate that a function or operation does not return a result (void). It is not possible to declare this type explicitly.

+ *ANY* - Special data type used by the type-checker during the static analysis phase. This type is evaluated at runtime. It is not possible to declare this type explicitly.


## 3. Variables and Syntax
Variables are declared by a direct assignment. 
+ Shorthand assignment is supported (`+=`, `-=`. `*=`, `/=`, `%=`).
+ Mrix language uses block scoping defined by `{}`. 
+ Every statement must end with a semicolon `;`.

```mrix
a = 3; // global scope
{
    b = 6; // local scope
}
b += 1 // results in an error
```

## 4. Output
<p align="justify">
In mrix, the <code>print</code> statement is a built-in instruction which outputs expressions to the console. It supports multiple arguments separated by commas. Expressions can optionally be enclosed in parentheses.
</p>

```mrix
print x;
print(x);
print (x + 2) * 4;
print "alpha", "beta";
```

## 5. Matrix Structures and Operations
### Matrices
#### Initialization
Matrices in mrix are 0-indexed. They can be initialized in two ways:
+ **FlatMatrix (1D)**:
  
    ```mrix
    A = [1, 2, 3, 4, 5, 6, 7];
    ```

+ **Matrix (2D)**:

    ```mrix
    B = [[1, 2, 3], [4, 5, 6]];
    ```

#### Accessing elements
You can access or change specific elements using square brackets:
```mrix
m = [[10, 20], [30, 40]];
val = m[0, 1];              // val == 20
m[1, 1] = 99;               // modify
print m[1, 1];              // 99

a = [1, 2, 3];
a[1] = 5;
print a;                    // a == [1, 5, 3]
```

### Operations
<p align="justify">
Mrix supports standard algebraic operations as well as element-wise operations between matrices. It uses adaptive operators that automatically adjust behaviour based on the operand types.
</p>

| Operator | Scalar-Scalar | Scalar-Matrix | Matrix-Matrix |
| :---: | :---: | :---: | :---: |
| `+` | Addition | N/A | Element-wise addition |
| `-` | Subtraction | N/A | Element-wise subtraction |
| `*` | Multiplication | Scaling | Dot product |
| `/` | Division | Scaling | Element-wise division |
| `%` | Modulo | N/A | N/A |
| `.*` | N/A | N/A | Element-wise multiplication |
| `'` | N/A | N/A | Transposition |

**Note**: It is also possible to explicitly use element-wise operations between matrices: `.+`, `.-`, `./`.

### String operations

+ `string + string` - String concatenation.
+ `string - string` - Remove the first occurrence of the second string.
+ `string * int` - Repeat the string N times.

## 6. Tuples
Tuple structures allow for grouping multiple values into a single data type.

### Initialization
Tuples are defined using parentheses. They can contain any data types, including other tuples and matrices:
```mrix
empty = ();
single = (5,);
t = (1, 2.5, "mrix", [[1, 0], [0, 1]]);
nested = (1, (2, 3), 4);
```

### Accessing elements
Elements of a tuple can be accessed using square brackets. Unlike matrices, tuple elements are read-only:
```mrix
t = (10, "hello", [1, 2]);
val = t[0]; // 10
msg = t[1]; // "hello"

t[0] = 20; // results in an error (immutability)
```

### Unpacking
Mrix supports tuple unpacking allowing assignment of multiple values to variables in a single statement:
```mrix
funct get_coords() {
    return (10, 20);
}

(x, y) = get_coords();
print x; // 10
print y; // 20
```

### Comparison
Tuples support equality operators. Two tuples are considered equal if they have the same length and all their elements at corresponding indices are equal.

```mrix
t1 = (1, [1, 2], (3, 4));
t2 = (1, [1, 2], (3, 4));
print t1 == t2; // true

t3 = (1, [1, 2], (9, 9));
print t1 == t3; // false
```

## 7. Logic and Comparison
These operators evaluate expressions and return a **BOOL** value:

### Comparison operators
Used to compare numerical values (**INT**, **FLOAT**).
+ `==` - Returns `true` if values are identical.
+ `!=` - Returns `true` if values are different.
+ `>` - Returns `true` if the left operand is larger.
+ `<` - Returns `true` if the left operand is smaller.
+ `>=` - Returns `true` if the left operand is larger or equal.
+ `<=` - Returns `true` if the left operand is smaller or equal.

### Logical operators
Used to compare boolean values (**BOOL**).
+ `and` - Returns true only if both operands evaluate to true.
+ `or` - Returns true if at least one of operands evaluates to true.
+ `not` (or `!`) - A unary operator that inverts the boolean value.

### Equality
Mrix uses **deep equality** for comparison of complex data structures.
+ **Matrices** are equal if they have the same dimensions and all elements match.
+ **Tuples** are equal if they have the same length and all corresponding elements are equal (this is checked recursively for nested structures).
+ **Mixed Types**: Comparison between different data types (e.g., `5 == "5"` or `[1, 2] == (1, 2)`) always returns `false`.


## 8. Matrix generators
Built-in instructions for quick matrix creation:
+ `eye(n)` or `eye(r, c)` - Identity matrix.
+ `zeros(n)` or `zeros(r, c)` - Matrix filled with `0.0`.
+ `ones(n)` or `ones(r, c)` - Matrix filled with `1.0`.

## 9. Control flow
### Conditional statements
```mrix
if (condition) {
    print "OK";
} else {
    print "NOT OK";
}
```

### Loops
Mrix supports `while` and `for` loops:
```mrix
while (x < 15) {
    x += 1;
}

for i = 0:14 {
    print i; // prints numbers from 0 to 14
}
```
**Note**: In `for` loops the range is **inclusive**.
### Loop Control Statements
Mrix provides two keywords to control the execution flow within loops:
+ `break` - Immediately terminates the innermost loop.
+ `continue` - Skips the rest of the current iteration and jumps to the next one.

```mrix
for i = 1:10 {
    if (i == 3) continue; // skip number 3
    if (i == 7) break; // stop the loop when i reaches 7
    print i; // prints: 1, 2, 4, 5, 6
}
```

## 10. Functions
Functions in mrix support recursion and returning values. They are defined with `funct` keyword:
```mrix
funct add(a, b) {
    return a + b;
}

a = add(1, 4);
```

## 11. Imports
Mrix supports importing code from other `.mrix` files using `import` keyword:
<table>
<tr>
<td width="50%">

**fibonacci.mrix**

<pre><code>
funct fibonacci(x) {
    if (x == 0 or x == 1) {
        return 1;
    }
    return fibonacci(x-1) + fibonacci(x-2);
}
</code></pre>

</td>

<td width="50%">

**main.mrix**

<pre><code>
import "fibonacci.mrix";

a = fibonacci(10);
print a;
</code></pre>

</td>
</tr>
</table>

When the file is imported, it is executed from top to bottom so that all global variables and functions defined in the imported file become available in the current scope.

The path can be relative or absolute. Its type must be **STRING**.

## Standard Library of Built-In Functions

### Math & Statistics
|     Function     |                    Accepted types                    |                          Description                          |           Return type            |
|:----------------:|:----------------------------------------------------:|:-------------------------------------------------------------:|:--------------------------------:|
|     `inv(A)`     |                   `A:` **MATRIX**                    |                  Returns the inverse of `A`.                  |            **MATRIX**            |
|     `abs(x)`     |         `x:` **INT**, **FLOAT**, **MATRIX**          |                Returns absolute value of `x`.                 | **INT**, **FLOAT** or **MATRIX** |
|    `sqrt(x)`     |         `x:` **INT**, **FLOAT**, **MATRIX**          |                Returns the square root of `x`.                | **INT**, **FLOAT** or **MATRIX** |
|     `sin(x)`     |               `x:` **INT**, **FLOAT**                |                   Returns the sine of `x`.                    |            **FLOAT**             |
|     `cos(x)`     |               `x:` **INT**, **FLOAT**                |                  Returns the cosine of `x`.                   |            **FLOAT**             |
|     `tan(x)`     |               `x:` **INT**, **FLOAT**                |                  Returns the tangent of `x`.                  |            **FLOAT**             |
|  `log(x, base)`  |  `x:` **INT**, **FLOAT** `base:` **INT**, **FLOAT**  |             Returns the `base` logarithm of `x`.              |            **FLOAT**             |
|     `ln(x)`      |               `x:` **INT**, **FLOAT**                |             Returns the natural logarithm of `x`.             |            **FLOAT**             |
| `pow(base, exp)` | `base:` **INT**, **FLOAT** `exp:` **INT**, **FLOAT** |   Returns the value of `base` raised to the power of `exp`.   |       **INT** or **FLOAT**       |
|     `exp(x)`     |               `x:` **INT**, **FLOAT**                |                Returns the exponential of `x`.                |            **FLOAT**             |
|    `floor(x)`    |               `x:` **INT**, **FLOAT**                |                   Returns the floor of `x`.                   |       **INT** or **FLOAT**       |
|    `ceil(x)`     |               `x:` **INT**, **FLOAT**                |                  Returns the ceiling of `x`.                  |       **INT** or **FLOAT**       |
|  `round(x, n)`   |         `x:` **INT**, **FLOAT** `n:` **INT**         |    Returns the value of `x` rounded to `n` decimal places.    |       **INT** or **FLOAT**       |
|   `sum(x...)`    |         `x:` **INT**, **FLOAT**, **MATRIX**          |       Returns the sum of all given numerical arguments.       |       **INT** or **FLOAT**       |
|   `min(x...)`    |         `x:` **INT**, **FLOAT**, **MATRIX**          |  Returns the minimum value of all given numerical arguments.  |       **INT** or **FLOAT**       |
|   `max(x...)`    |         `x:` **INT**, **FLOAT**, **MATRIX**          |  Returns the maximum value of all given numerical arguments.  |       **INT** or **FLOAT**       |
|   `mean(x...)`   |         `x:` **INT**, **FLOAT**, **MATRIX**          | Returns the arithmetic mean of all given numerical arguments. |       **INT** or **FLOAT**       |

### Utilities
|       Function        |                                    Accepted types                                    |                                           Description                                           |       Return type       |
|:---------------------:|:------------------------------------------------------------------------------------:|:-----------------------------------------------------------------------------------------------:|:-----------------------:|
|       `size(A)`       |                                   `A:` **MATRIX**                                    |                    Returns the dimensions of `A` as a tuple `(rows, cols)`.                     |        **TUPLE**        |
|       `rows(A)`       |                                   `A:` **MATRIX**                                    |                               Returns the number of rows in `A`.                                |         **INT**         |
|       `cols(A)`       |                                   `A:` **MATRIX**                                    |                               Returns the number of cols in `A`.                                |         **INT**         |
|       `len(x)`        |                        `x:` **STRING**, **MATRIX**, **TUPLE**                        | Returns the number of characters in a string or the total number of elements in a matrix/tuple. |         **INT**         |
|   `contains(x, y)`    | `x:` **STRING**, **MATRIX**, **TUPLE** `y:` **STRING**, **INT**, **FLOAT**, **BOOL** |                       Returns true if `x` contains `y`, otherwise false.                        |        **BOOL**         |
|      `at(x, i)`       |                             `x:` **STRING** `i:` **INT**                             |                               Returns the character at index `i`.                               |       **STRING**        |
|       `type(x)`       |                                      `x:` *ANY*                                      |                                  Returns the type name of `x`.                                  |       **STRING**        |
|       `int(x)`        |                    `x:` **INT**, **FLOAT**, **STRING**, **BOOL**                     |                              Returns the value of `x` as **INT**.                               |         **INT**         |
|      `float(x)`       |                    `x:` **INT**, **FLOAT**, **STRING**, **BOOL**                     |                             Returns the value of `x` as **FLOAT**.                              |        **FLOAT**        |
|       `str(x)`        |                    `x:` **INT**, **FLOAT**, **STRING**, **BOOL**                     |                             Returns the value of `x` as **STRING**.                             |       **STRING**        |
|       `bool(x)`       |                    `x:` **INT**, **FLOAT**, **STRING**, **BOOL**                     |                              Returns the value of `x` as **BOOL**.                              |        **BOOL**         |
|      `tuple(x)`       |                                      `x:` *ANY*                                      |                             Returns the value of `x` as **TUPLE**.                              |        **TUPLE**        |
|      `matrix(x)`      |                                    `x:` **TUPLE**                                    |                             Returns numeric tuple `x` as **MATRIX**                             |       **MATRIX**        |
| `range(s, e, [step])` |                      `s:` **INT** `e:` **INT** `step:` **INT**                       |                    Returns a sequence from `s` to `e`. `step` defaults to 1.                    |        **TUPLE**        |
|     `reverse(x)`      |                              `x:` **STRING**, **TUPLE**                              |                                  Returns `x` in reverse order.                                  | **STRING** or **TUPLE** |

### File I/O
| Function | Accepted types | Description | Return type |
| :---: | :---: | :---: | :---: |
| `f_read(path)` | `path:` **STRING** | Returns the content of the file at `path`. | **STRING** |
| `f_readline(path, i)` | `path:` **STRING** `i:` **INT** | Returns the `i`-th line of the file at `path`. | **STRING** |
| `f_lines(path)` | `path:` **STRING** | Returns the number of lines in the file at `path`. | **INT** |
| `f_write(path, s)` | `path:` **STRING** `s:` **STRING** | Writes `s` to the file at `path`. Creates the file if it doesn't exist. | *NULL* |
| `f_append(path, s)` | `path:` **STRING** `s:` **STRING** | Appends `s` to the end of the file at `path`. Creates the file if it doesn't exist. | *NULL* |