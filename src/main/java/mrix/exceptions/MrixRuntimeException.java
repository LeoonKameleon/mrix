package mrix.exceptions;

public class MrixRuntimeException extends RuntimeException {
    private final int line;

    public MrixRuntimeException(String message, int line) {
        super("Line " + line + ": Runtime error: " + message);
        this.line = line;
    }
}
