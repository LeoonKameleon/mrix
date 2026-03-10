package mrix.exceptions;

import mrix.interpreter.Value;

public class ReturnException extends RuntimeException {
    public final Value value;
    public ReturnException(Value value) {
        this.value = value;
    }
}
