package mrix.exception;

public class MrixRuntimeException extends RuntimeException {
    public MrixRuntimeException(String message, int line) {
        super("Line " + line + " Runtime error: " + message);
    }
}
