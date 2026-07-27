package mrix.typing.symbol;

import java.util.HashMap;

public class SymbolTable {
    private final SymbolTable parent;
    private final HashMap<String, VariableSymbol> symbols;

    public SymbolTable(SymbolTable parent) {
        this.parent = parent;
        this.symbols = new HashMap<>();
    }

    public void put(String name, VariableSymbol symbol) {
        symbols.put(name, symbol);
    }

    public VariableSymbol get(String name) {
        if (symbols.containsKey(name)) return symbols.get(name);
        if (parent != null) return parent.get(name);
        return null;
    }

    public SymbolTable pushScope() {
        return new SymbolTable(this);
    }

    public SymbolTable popScope() {
        return parent;
    }
}
