package mrix.interpreter.flow;

import mrix.interpreter.value.Value;

public class ReturnException extends RuntimeException {
    public final Value value;
    public ReturnException(Value value) {
        this.value = value;
    }
}
