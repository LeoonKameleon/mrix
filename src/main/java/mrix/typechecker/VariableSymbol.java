package mrix.typechecker;

public class VariableSymbol {
    private final String name;
    private final DataType type;
    public VariableSymbol(String name, DataType type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public DataType getType() {
        return type;
    }
}
