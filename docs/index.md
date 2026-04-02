# Mrix Language Documentation

## 1. Introduction
<p align="justify">
Mrix is a dynamically-typed, interpreted language designed for matrix manipulation. The interpreter is implemented in Java 23.
</p>

## 2. Data Types
Mrix supports following data types:
+ **INT** - 64-bit signed integer.
  + example: `int_value = 5;`

+ **FLOAT** - 64-bit floating point.
  + example: `float_value = 6.7;`

+ **BOOL** - Logical values `true` or `false`.
  + example: `boolean_value = true;`
  + **Note**: For numerical and matrix operations, booleans are automatically evaluated as **FLOAT** values: `true == 1.0` and `false == 0.0`.

+ **STRING** - Sequence of characters.
  + example: `string_sequence = "mrix";`

+ **MATRIX** - A 2D (or 1D) array of numerical values (**INT**/**FLOAT**).
  + example: ```matrix = [[1, 2], [3.3, 4]];```

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
+ `string - string` - Remove the first occurence of the second string.
+ `string * int` - Repeat the string N times.

## 6. Logic and Comparison
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


## 7. Matrix generators
Built-in instructions for quick matrix creation:
+ `eye(n)` or `eye(r, c)` - Identity matrix.
+ `zeros(n)` or `zeros(r, c)` - Matrix filled with `0.0`.
+ `ones(n)` or `ones(r, c)` - Matrix filled with `1.0`.

## 8. Control flow
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
    if (i == 6) continue;
    print i;
    if (i == 10) break;
}
```

## 9. Functions
Functions in mrix support recursion and returning values. They are defined with `funct` keyword:
```mrix
funct add(a, b) {
    return a + b;
}

a = add(1, 4);
```

## 10. Imports
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
|     Function     |                              Accepted types                               |                                        Description                                        | Return type |
|:----------------:|:-------------------------------------------------------------------------:|:-----------------------------------------------------------------------------------------:|:-----------:|
|    `size(A)`     |                              `A:` **MATRIX**                              |                         Returns the size (`[rows, cols]`) of `A`.                         | **MATRIX**  |
|    `rows(A)`     |                              `A:` **MATRIX**                              |                            Returns the number of rows in `A`.                             |   **INT**   |
|    `cols(A)`     |                              `A:` **MATRIX**                              |                            Returns the number of cols in `A`.                             |   **INT**   |
|     `len(x)`     |                        `x:` **STRING**, **MATRIX**                        | Returns the number of characters in a string or the total number of elements in a matrix. |   **INT**   |
| `contains(x, y)` | `x:` **STRING**, **MATRIX** `y:` **STRING**, **INT**, **FLOAT**, **BOOL** |                    Returns true if `x` contains `y`, otherwise false.                     |  **BOOL**   |
|    `at(x, i)`    |                       `x:` **STRING** `y:` **INT**                        |                            Returns the character at index `i`.                            | **STRING**  |
|    `type(x)`     |                                `x:` *ANY*                                 |                               Returns the type name of `x`.                               | **STRING**  |
|     `int(x)`     |               `x:` **INT**, **FLOAT**, **STRING**, **BOOL**               |                           Returns the value of `x` as **INT**.                            |   **INT**   |
|    `float(x)`    |               `x:` **INT**, **FLOAT**, **STRING**, **BOOL**               |                          Returns the value of `x` as **FLOAT**.                           |  **FLOAT**  |
|     `str(x)`     |               `x:` **INT**, **FLOAT**, **STRING**, **BOOL**               |                          Returns the value of `x` as **STRING**.                          | **STRING**  |
|    `bool(x)`     |               `x:` **INT**, **FLOAT**, **STRING**, **BOOL**               |                           Returns the value of `x` as **BOOL**.                           |  **BOOL**   |

### File I/O
| Function | Accepted types | Description | Return type |
| :---: | :---: | :---: | :---: |
| `f_read(path)` | `path:` **STRING** | Returns the content of the file at `path`. | **STRING** |
| `f_readline(path, i)` | `path:` **STRING** `i:` **INT** | Returns the `i`-th line of the file at `path`. | **STRING** |
| `f_lines(path)` | `path:` **STRING** | Returns the number of lines in the file at `path`. | **INT** |
| `f_write(path, s)` | `path:` **STRING** `s:` **STRING** | Writes `s` to the file at `path`. Creates the file if it doesn't exist. | *NULL* |
| `f_append(path, s)` | `path:` **STRING** `s:` **STRING** | Appends `s` to the end of the file at `path`. Creates the file if it doesn't exist. | *NULL* |