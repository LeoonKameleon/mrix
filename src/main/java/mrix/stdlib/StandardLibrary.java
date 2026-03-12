package mrix.stdlib;

import java.util.HashMap;
import java.util.List;
import java.util.function.Function;

import mrix.exceptions.StandardLibraryException;
import mrix.interpreter.Value;
import mrix.typechecker.DataType;
import static mrix.typechecker.DataType.*;

public class StandardLibrary {
    private HashMap<String, Function<List<Value>, Value>> functions = new HashMap<>();

    public StandardLibrary() {
        functions.put("sqrt", args -> {
            if (args.size() != 1) throw new StandardLibraryException("sqrt() expects 1 argument, but got " + args.size());
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
                throw new StandardLibraryException("sqrt() does not support " + value.getType() + " type");
            }
            return new Value(Math.sqrt(value.toDouble()), FLOAT);
        });
        functions.put("abs", args -> {
            if (args.size() != 1) throw new StandardLibraryException("abs() expects 1 argument, but got " + args.size());
            Value arg = args.get(0);
            if (arg.getType() == INT) {
                return new Value(Math.abs(args.get(0).toInt()), INT);
            }
            if (arg.getType() == FLOAT) {
                return new Value(Math.abs(args.get(0).toDouble()), FLOAT);
            }
            if (arg.getType() == MATRIX) {
                double[][] matrix = arg.toMatrix();
                int rows = matrix.length, cols = matrix[0].length;
                double[][] result = new double[rows][cols];
                for (int i=0; i<rows; i++)
                    for (int j=0; j<cols; j++)
                        result[i][j] = Math.abs(matrix[i][j]);
                return new Value(result, MATRIX);
            }
            throw new StandardLibraryException("abs() does not support " + arg.getType() + " type");
        });
        functions.put("size", args -> {
            if (args.size() != 1) throw new StandardLibraryException("size() expects 1 argument, but got " + args.size());
            Value arg = args.get(0);
            if (arg.getType() == MATRIX) {
                double[][] matrix = arg.toMatrix();
                int rows = matrix.length, cols = matrix[0].length;
                double[][] result = new double[1][2];
                result[0][0] = rows;
                result[0][1] = cols;
                return new Value(result, MATRIX);
            }
            throw new StandardLibraryException("size() does not support " + arg.getType() + " type");
        });
        functions.put("rows", args -> {
            if (args.size() != 1) throw new StandardLibraryException("rows() expects 1 argument, but got " + args.size());
            Value arg = args.get(0);
            if (arg.getType() == MATRIX) {
                return new Value(arg.toMatrix().length, INT);
            }
            throw new StandardLibraryException("rows() does not support " + arg.getType() + " type");
        });
        functions.put("cols", args -> {
            if (args.size() != 1) throw new StandardLibraryException("rows() expects 1 argument, but got " + args.size());
            Value arg = args.get(0);
            if (arg.getType() == MATRIX) {
                return new Value(arg.toMatrix()[0].length, INT);
            }
            throw new StandardLibraryException("rows() does not support " + arg.getType() + " type");
        });
        functions.put("sum", args -> {
            if (args.isEmpty()) throw new StandardLibraryException("sum() expects at least 1 argument");
            double result = 0;
            boolean allInt = true;
            for (Value arg : args) {
                if (arg.getType() == STRING || arg.getType() == FUNCTION)
                    throw new StandardLibraryException("sum() does not support " + arg.getType() + " type");
                if (arg.getType() != INT) allInt = false;
                if (arg.getType() == MATRIX) {
                    allInt = false;
                    for (double[] row : arg.toMatrix())
                        for (double val : row)
                            result += val;
                } else {
                    result += arg.toDouble();
                }
            }
            if (allInt) return new Value((int) result, INT);
            return new Value(result, FLOAT);
        });
        functions.put("min", args -> {
            if (args.isEmpty()) throw new StandardLibraryException("min() expects at least 1 argument");
            double result = Double.MAX_VALUE;
            boolean allInt = true;
            for (Value arg : args) {
                if (arg.getType() == STRING || arg.getType() == FUNCTION)
                    throw new StandardLibraryException("min() does not support " + arg.getType() + " type");
                if (arg.getType() != INT) allInt = false;
                if (arg.getType() == MATRIX) {
                    allInt = false;
                    for (double[] row : arg.toMatrix())
                        for (double val : row)
                            if (val < result) result = val;
                } else {
                    if (arg.toDouble() < result) result = arg.toDouble();
                }
            }
            if (allInt) return new Value((int) result, INT);
            return new Value(result, FLOAT);
        });
        functions.put("max", args -> {
            if (args.isEmpty()) throw new StandardLibraryException("max() expects at least 1 argument");
            double result = Double.MIN_VALUE;
            boolean allInt = true;
            for (Value arg : args) {
                if (arg.getType() == STRING || arg.getType() == FUNCTION)
                    throw new StandardLibraryException("max() does not support " + arg.getType() + " type");
                if (arg.getType() != INT) allInt = false;
                if (arg.getType() == MATRIX) {
                    allInt = false;
                    for (double[] row : arg.toMatrix())
                        for (double val : row)
                            if (val > result) result = val;
                } else {
                    if (arg.toDouble() > result) result = arg.toDouble();
                }
            }
            if (allInt) return new Value((int) result, INT);
            return new Value(result, FLOAT);
        });
    }

    public boolean has(String name) {
        return functions.containsKey(name);
    }

    public Value call(String name, List<Value> args) {
        return functions.get(name).apply(args);
    }
}
