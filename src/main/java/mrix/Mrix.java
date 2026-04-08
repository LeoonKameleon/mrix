package mrix;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import mrix.interpreter.Interpreter;
import mrix.nodes.Node;
import mrix.tokens.Token;
import mrix.typechecker.TypeChecker;

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
        Path fileDir = Paths.get(filename).toAbsolutePath().getParent();
        String content = Files.readString(Paths.get(filename));
        run(content, fileDir);
        if (hadError) {
            System.exit(1);
        }
    }
    private static void run(String content, Path fileDir) {
        Scanner scanner = new Scanner(content);
        List<Token> tokens = scanner.tokenize();
        
        Parser parser = new Parser(tokens);
        Node ast = parser.parseProgram();

        TypeChecker typeChecker = new TypeChecker(fileDir);
        ast.accept(typeChecker);
        List<String> errors = typeChecker.getErrors();
        if (!errors.isEmpty()) {
            for (String error : errors) {
                System.out.println(error);
            }
            System.exit(1);
        }

        Interpreter interpreter = new Interpreter(fileDir);
        ast.accept(interpreter);
        interpreter.finish();
    }
    static void error(int line, String message) {
        System.out.println("Line " + line + " error: " + message);
    }
}
