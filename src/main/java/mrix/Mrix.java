package mrix;

import mrix.ast.Node;
import mrix.exception.MrixRuntimeException;
import mrix.exception.MrixSyntaxException;
import mrix.interpreter.Interpreter;
import mrix.parser.Parser;
import mrix.scanner.Scanner;
import mrix.scanner.token.Token;
import mrix.typing.TypeChecker;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class Mrix {
    private static final boolean hadError = false;

    public static void main(String[] args) throws IOException {
        if (args.length == 0) {
            System.out.println("Usage: mrix <filename> OR mrix -c \"<code>\"");
            System.exit(1);
        } else if (args[0].equals("-c")) {
            if (args.length < 2) {
                System.out.println("Error: No code provided after -c");
                System.exit(1);
            }
            String code = args[1];
            run(code, Paths.get(".").toAbsolutePath());
        } else if (args[0].endsWith(".mrix")) {
            runFile(args[0]);
        } else {
            System.out.println("Invalid file extension");
        }
    }

    private static void runFile(String filename) throws IOException {
        Path path = Paths.get(filename);
        Path fileDir = path.toAbsolutePath().getParent();
        String content = Files.readString(path);
        run(content, fileDir);
        if (hadError) {
            System.exit(1);
        }
    }

    private static void run(String content, Path fileDir) {
        Scanner scanner = new Scanner(content);
        List<Token> tokens = scanner.tokenize();

        Node ast;
        try {
            Parser parser = new Parser(tokens);
            ast = parser.parseProgram();
        } catch (MrixSyntaxException e) {
            System.err.println("Syntax error");
            System.err.println("------------");
            System.err.println(e.getMessage());
            System.exit(1);
            return;
        }

        TypeChecker typeChecker = new TypeChecker(fileDir);
        ast.accept(typeChecker);
        List<String> errors = typeChecker.getErrors();
        if (!errors.isEmpty()) {
            System.err.println("Type-checker errors");
            System.err.println("-------------------");
            for (String error : errors) {
                System.err.println(error);
            }
            System.exit(1);
        }

        Interpreter interpreter = new Interpreter(fileDir);
        try {
            ast.accept(interpreter);
            interpreter.finish();
        } catch (MrixRuntimeException e) {
            interpreter.finish();
            System.err.println("Runtime error");
            System.err.println("-------------");
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }

    public static void error(int line, String message) {
        System.out.println("Line " + line + " error: " + message);
    }
}
