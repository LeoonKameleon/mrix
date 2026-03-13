package mrix.stdlib;

import java.util.HashMap;
import java.util.List;
import java.util.function.Function;

import mrix.exceptions.StandardLibraryException;
import mrix.interpreter.Value;
import static mrix.typechecker.DataType.*;
import mrix.typechecker.DataType;

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
            double result = Math.sqrt(value.toDouble());
            if (result == (long) result) return Value.of((long) result);
            return new Value(result, FLOAT);
        });
        functions.put("abs", args -> {
            if (args.size() != 1) throw new StandardLibraryException("abs() expects 1 argument, but got " + args.size());
            Value arg = args.get(0);
            if (arg.getType() == INT) {
                return Value.of(Math.abs(args.get(0).toLong()));
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
                return Value.of(arg.toMatrix().length);
            }
            throw new StandardLibraryException("rows() does not support " + arg.getType() + " type");
        });
        functions.put("cols", args -> {
            if (args.size() != 1) throw new StandardLibraryException("rows() expects 1 argument, but got " + args.size());
            Value arg = args.get(0);
            if (arg.getType() == MATRIX) {
                return Value.of(arg.toMatrix()[0].length);
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
            if (allInt) return Value.of((long) result);
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
            if (allInt) return Value.of((long) result);
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
            if (allInt) return Value.of((long) result);
            return new Value(result, FLOAT);
        });
        functions.put("mean", args -> {
            if (args.isEmpty()) throw new StandardLibraryException("mean() expects at least 1 argument");
            double sum = 0;
            int count = 0;
            for (Value arg : args) {
                if (arg.getType() == STRING || arg.getType() == FUNCTION)
                    throw new StandardLibraryException("sum() does not support " + arg.getType() + " type");
                if (arg.getType() == MATRIX) {
                    for (double[] row : arg.toMatrix())
                        for (double val : row) {
                            sum += val;
                            count++;
                        } 
                } else {
                    sum += arg.toDouble();
                    count++;
                }
            }
            double result = sum/count;
            if (result == (long) result) return Value.of((long) result);
            return new Value(result, FLOAT);
        });
        functions.put("type", args -> {
            if (args.size() != 1) throw new StandardLibraryException("type() expects 1 argument, but got " + args.size());
            Value arg = args.get(0);
            return new Value(arg.getType().name(), STRING);
        });
        functions.put("pow", args -> {
            if (args.size() != 2) throw new StandardLibraryException("pow() expects 2 arguments");
            Value baseVal = args.get(0);
            Value expVal = args.get(1);
            
            DataType baseType = baseVal.getType();
            DataType expType = expVal.getType();
            
            if (baseType == STRING || baseType == MATRIX || baseType == FUNCTION ||
                expType == STRING || expType == MATRIX || expType == FUNCTION) {
                throw new StandardLibraryException("pow() does not support " + baseType + " and " + expType);
            }
            
            double result = Math.pow(baseVal.toDouble(), expVal.toDouble());
            if (result == (long) result) return Value.of((long) result);
            return new Value(result, FLOAT);
        });
        functions.put("len", args -> {
            if (args.size() != 1) throw new StandardLibraryException("len() expects 1 argument, but got " + args.size());
            Value arg = args.get(0);
            if (arg.getType() == MATRIX) {
                double[][] m = arg.toMatrix();
                return Value.of((long) m.length * m[0].length);
            }
            if (arg.getType() == STRING) {
                return Value.of(arg.toString().length());
            }
            throw new StandardLibraryException("len() does not support " + arg.getType() + " type");
        });
        functions.put("at", args -> {
            if (args.size() != 2) throw new StandardLibraryException("at() expects 2 arguments");
            Value string = args.get(0);
            Value index = args.get(1);
            if (string.getType() == STRING && index.getType() == INT) {
                return new Value(string.toString().substring(index.toInt(), index.toInt()+1), STRING);
            }
            throw new StandardLibraryException("pow() does not support " + string.getType() + " and " + index.getType());
        });
        functions.put("int", args -> {
            if (args.size() != 1) throw new StandardLibraryException("int() expects 1 argument, but got " + args.size());
            Value arg = args.get(0);
            if (arg.getType() == STRING) {
                try {
                    return Value.of(Long.parseLong(arg.toString()));
                } catch (NumberFormatException e) {
                    throw new StandardLibraryException("int() is unable to convert STRING to INT"); 
                }
            }
            if (arg.getType() == FLOAT) {
                return Value.of((long) arg.toDouble());
            }
            if (arg.getType() == BOOL) {
                return Value.of(arg.toLong());
            }
            if (arg.getType() == MATRIX) {
                throw new StandardLibraryException("int() does not support " + arg.getType() + " type");
            }
            if (arg.getType() == INT) {
                return arg;
            }
            throw new StandardLibraryException("int() does not support " + arg.getType() + " type");
        });
        functions.put("float", args -> {
            if (args.size() != 1) throw new StandardLibraryException("float() expects 1 argument, but got " + args.size());
            Value arg = args.get(0);
            if (arg.getType() == STRING) {
                try {
                    return new Value(Double.parseDouble(arg.toString()), FLOAT);
                } catch (NumberFormatException e) {
                    throw new StandardLibraryException("float() is unable to convert STRING to FLOAT"); 
                }
            }
            if (arg.getType() == INT) {
                return new Value((double) arg.toLong(), FLOAT);
            }
            if (arg.getType() == BOOL) {
                return Value.of(arg.toLong());
            }
            if (arg.getType() == MATRIX) {
                throw new StandardLibraryException("int() does not support " + arg.getType() + " type");
            }
            if (arg.getType() == FLOAT) {
                return arg;
            }
            throw new StandardLibraryException("float() does not support " + arg.getType() + " type");
        });
        functions.put("str", args -> {
            if (args.size() != 1) throw new StandardLibraryException("str() expects 1 argument, but got " + args.size());
            Value arg = args.get(0);
            if (arg.getType() == INT) {
                return new Value(String.valueOf(arg.toLong()), STRING);
            }
            if (arg.getType() == FLOAT) {
                return new Value(String.valueOf(arg.toDouble()), STRING);
            }
            if (arg.getType() == BOOL) {
                return new Value(arg.toBoolean() ? "true" : "false", STRING);
            }
            if (arg.getType() == MATRIX) {
                throw new StandardLibraryException("str() does not support " + arg.getType() + " type");
            }
            if (arg.getType() == STRING) {
                return arg;
            }
            throw new StandardLibraryException("str() does not support " + arg.getType() + " type");
        });
    }

    public boolean has(String name) {
        return functions.containsKey(name);
    }

    public Value call(String name, List<Value> args) {
        return functions.get(name).apply(args);
    }
}
