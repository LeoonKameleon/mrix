# mrix programming language

Mrix is a dynamically-typed programming language that supports standard logic, functions, and native matrix operations.<br> This repository contains the source code for the interpreter written in Java.

## Documentation
WIP...

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
java -jar target/mrix.jar <filename>.mrix
```