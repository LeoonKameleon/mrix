# Mrix Language Documentation

## 1. Introduction
<p align="justify">
Mrix is a dynamically-typed, interpreted language designed for matrix manipulation. The interpreter is implemented in Java 23.
</p>

## 2. Data Types
Mrix supports following data types:
+ **INT** - 32-bit signed integer.
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

+ ***ANY*** - Special data type used by the type-checker during the static analysis phase. This type is evaluated at runtime. It is not possible to declare this type explicitly.

## 3. Variables and Syntax
Variables are declared by a direct assignment. 
+ Shorthand assignment is supported (`+=`, `-=`. `*=`, `/=`).
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
Matrices in mrix are 0-indexed. They can be initialized in two ways:
+ **FlatMatrix (1D)**:
  
    ```mrix
    A = [1, 2, 3, 4, 5, 6, 7];
    ```

+ **Matrix (2D)**:

    ```mrix
    B = [[1, 2, 3], [4, 5, 6]];
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
| `.*` | N/A | N/A | Element-wise multiplication |
| `'` | N/A | N/A | Transposition |

**Note**: It is also possible to explicitly use element-wise operations between matrices: `.+`, `.-`, `./`.

### String operations

+ `string + string` - String concatenation.
+ `string - string` - Remove the first occurence of the second string.
+ `string * int` - Repeat the string N times.

## 6. Matrix generators
Built-in instructions for quick matrix creation:
+ `eye(n)` or `eye(r, c)` - Identity matrix.
+ `zeros(n)` or `zeros(r, c)` - Matrix filled with `0.0`.
+ `ones(n)` or `ones(r, c)` - Matrix filled with `1.0`.

## 7. Control flow
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

## 8. Functions
Functions in mrix support recursion and returning values. They are defined with `funct` keyword:
```mrix
funct add(a, b) {
    return a + b;
}

a = add(1, 4);
```