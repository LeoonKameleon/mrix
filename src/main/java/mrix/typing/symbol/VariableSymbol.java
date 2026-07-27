package mrix.typing.symbol;

import mrix.typing.type.DataType;

public class VariableSymbol {
    private final DataType type;

    public VariableSymbol(String name, DataType type) {
        this.type = type;
    }

    public DataType getType() {
        return type;
    }
}
