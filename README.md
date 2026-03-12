# Mrix Programming Language

<p align="justify">
Mrix is a dynamically-typed programming language which supports standard control structures, user-defined functions, and built-in support for matrix operations. This repository contains the source code for the interpreter written in Java.
</p>

## Code Example
```mrix
funct example(a, b) {
    A = eye(a, b);
    B = ones(b, a);
    A[1, 1] = 3;
    return A*B;
}

x = example(3, 4);
print x;
```

## Documentation
Full documentation available [here](docs/index.md).

## Requirements
* JDK 23
* Maven

## Build
1. Clone the repository:
```bash
git clone https://github.com/LeoonKameleon/mrix.git
cd mrix
```

2. Run the following command to create the executable JAR:
```bash
mvn clean package
```
The output file will be generated at `target/mrix.jar`.

## How to run

You can run .mrix files using:
```bash
java -jar target/mrix.jar /path/to/file.mrix
```