package mrix.stdlib;

import java.util.HashMap;
import java.util.List;
import java.util.function.Function;

import mrix.exceptions.StandardLibraryException;
import mrix.interpreter.Value;
import static mrix.typechecker.DataType.*;

public class StandardLibrary {
    private HashMap<String, Function<List<Value>, Value>> functions = new HashMap<>();

    public StandardLibrary() {
        functions.put("sqrt", args -> {
            if (args.size() != 1) throw new StandardLibraryException("sqrt expects 1 argument, but got " + args.size());
            Value value = args.get(0); 
            if (value.getType() == MATRIX) {
                double[][] matrix = value.toMatrix();
                int rows = matrix.length, cols = matrix[0].length;
                double[][] result = new double[rows][cols];
                for (int i=0; i<rows; i++)
                    for (int j=0; j<cols; j++)
                        result[i][j] = Math.sqrt(matrix[i][j]);
                return new Value(result, MATRIX);
            }
            if (value.getType() == STRING || value.getType() == BOOL) {
                throw new StandardLibraryException("sqrt does not support " + value.getType() + " type");
            }
            return new Value(Math.sqrt(value.toDouble()), FLOAT);
        });
        functions.put("abs", args -> {
            if (args.size() != 1) throw new StandardLibraryException("abs expects 1 argument, but got " + args.size());
            Value value = args.get(0);
            if (value.getType() == INT) {
                return new Value(Math.abs(args.get(0).toInt()), INT);
            }
            if (value.getType() == FLOAT) {
                return new Value(Math.abs(args.get(0).toDouble()), FLOAT);
            }
            if (value.getType() == MATRIX) {
                double[][] matrix = value.toMatrix();
                int rows = matrix.length, cols = matrix[0].length;
                double[][] result = new double[rows][cols];
                for (int i=0; i<rows; i++)
                    for (int j=0; j<cols; j++)
                        result[i][j] = Math.abs(matrix[i][j]);
                return new Value(result, MATRIX);
            }
            throw new StandardLibraryException("abs does not support " + value.getType() + " type");
        });
    }

    public boolean has(String name) {
        return functions.containsKey(name);
    }

    public Value call(String name, List<Value> args) {
        return functions.get(name).apply(args);
    }
}
