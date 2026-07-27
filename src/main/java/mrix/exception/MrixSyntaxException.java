package mrix.exception;

public class MrixSyntaxException extends RuntimeException {
    public MrixSyntaxException(String message, int line) {
        super("Line " + line + ": " + message);
    }
}
