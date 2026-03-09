package mrix;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Mrix {
    private static boolean hadError = false;

    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.out.println("Usage: java Mrix <filename>");
            System.exit(1);
        } else if (args[0].endsWith(".mrix")) {
            runFile(args[0]);
        } else {
            System.out.println("Invalid file extension");
        }
    }
    private static void runFile(String filename) throws IOException {
        String content = Files.readString(Paths.get(filename));
        run(content);
        if (hadError) {
            System.exit(1);
        }
    }
    private static void run(String content) {
        Scanner scanner = new Scanner(content);
        List<Token> tokens = scanner.tokenize();

        Parser parser = new Parser(tokens);
        parser.parseProgram();
    }
    static void error(int line, String message) {
        System.out.println("Line " + line + "error: " + message);
    }
}
