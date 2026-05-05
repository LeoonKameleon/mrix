package mrix.stdlib;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.function.Function;

import mrix.exception.StandardLibraryException;
import mrix.interpreter.value.TupleValue;
import mrix.interpreter.value.Value;
import static mrix.typing.type.DataType.*;
import mrix.typing.type.DataType;

public class StandardLibrary {
    private final HashMap<String, Function<List<Value>, Value>> functions = new HashMap<>();
    private final Path fileDir;
    private final Random rng = new Random();

    public StandardLibrary(Path fileDir) {
        this.fileDir = fileDir;
        functions.put("sqrt", args -> {
            if (args.size() != 1) throw new StandardLibraryException("sqrt() expects 1 argument, but got " + args.size());
            Value value = args.getFirst(); 
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
        functions.put("sin", args -> {
            if (args.size() != 1) throw new StandardLibraryException("sin() expects 1 argument, but got " + args.size());
            Value arg = args.getFirst();
            if (arg.getType() == FLOAT || arg.getType() == INT) {
                return new Value(Math.sin(arg.toDouble()), FLOAT);
            }
            throw new StandardLibraryException("sin() does not support " + arg.getType() + " type");
        });
        functions.put("cos", args -> {
            if (args.size() != 1) throw new StandardLibraryException("cos() expects 1 argument, but got " + args.size());
            Value arg = args.getFirst();
            if (arg.getType() == FLOAT || arg.getType() == INT) {
                return new Value(Math.cos(arg.toDouble()), FLOAT);
            }
            throw new StandardLibraryException("cos() does not support " + arg.getType() + " type");
        });
        functions.put("tan", args -> {
            if (args.size() != 1) throw new StandardLibraryException("tan() expects 1 argument, but got " + args.size());
            Value arg = args.getFirst();
            if ( arg.getType() == FLOAT || arg.getType() == INT) {
                return new Value(Math.tan(arg.toDouble()), FLOAT);
            }
            throw new StandardLibraryException("tan() does not support " + arg.getType() + " type");
        });
        functions.put("round", args -> {
            if (args.size() != 2) throw new StandardLibraryException("round() expects 2 arguments, but got " + args.size());
            Value arg = args.getFirst();
            Value n = args.get(1);
            if (n.getType() != INT) throw new StandardLibraryException("round() expects INT as second argument, but got" + n.getType());
            if (arg.getType() == FLOAT) {
                double m = Math.pow(10, n.toLong());
                return new Value(Math.round(arg.toDouble()*m)/m, FLOAT);
            }
            if (arg.getType() == INT) {
                return arg;
            }
            throw new StandardLibraryException("round() does not support " + arg.getType() + " type");
        });
        functions.put("floor", args -> {
            if (args.size() != 1) throw new StandardLibraryException("floor() expects 1 argument, but got " + args.size());
            Value arg = args.getFirst();
            if (arg.getType() == FLOAT) {
                return new Value(Math.floor(arg.toDouble()), FLOAT);
            }
            if (arg.getType() == INT) {
                return arg;
            }
            throw new StandardLibraryException("floor() does not support " + arg.getType() + " type");
        });
        functions.put("ceil", args ->{
            if (args.size() != 1) throw new StandardLibraryException("ceil() expects 1 argument, but got " + args.size());
            Value arg = args.getFirst();
            if (arg.getType() == FLOAT) {
                return new Value(Math.ceil(arg.toDouble()), FLOAT);
            }
            if (arg.getType() == INT) {
                return arg;
            }
            throw new StandardLibraryException("ceil() does not support " + arg.getType() + " type");
        });
        functions.put("log", args -> {
            if (args.size() != 2) throw new StandardLibraryException("log() expects 2 arguments, but got " + args.size());
            Value arg = args.getFirst();
            Value base = args.get(1);
            if ((arg.getType() == FLOAT || arg.getType() == INT) && (base.getType() == FLOAT || base.getType() == INT)) {
                return new Value(Math.log(arg.toDouble())/Math.log(base.toDouble()), FLOAT);
            }
            throw new StandardLibraryException("log() does not support " + arg.getType() + " and " + base.getType() + " types");
        });
        functions.put("ln", args -> {
            if (args.size() != 1) throw new StandardLibraryException("ln() expects 1 argument, but got " + args.size());
            Value arg = args.getFirst();
            if (arg.getType() == FLOAT || arg.getType() == INT) {
                return new Value(Math.log(arg.toDouble()), FLOAT);
            }
            throw new StandardLibraryException("ln() does not support " + arg.getType() + " type");
        });
        functions.put("abs", args -> {
            if (args.size() != 1) throw new StandardLibraryException("abs() expects 1 argument, but got " + args.size());
            Value arg = args.getFirst();
            if (arg.getType() == INT) {
                return Value.of(Math.abs(args.getFirst().toLong()));
            }
            if (arg.getType() == FLOAT) {
                return new Value(Math.abs(args.getFirst().toDouble()), FLOAT);
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
        functions.put("inv", args -> {
            if (args.size() != 1) throw new StandardLibraryException("inv() expects 1 argument, but got " + args.size());
            Value arg = args.getFirst();
            if (arg.getType() == MATRIX) {
                double[][] matrix = arg.toMatrix();
                if (matrix.length != matrix[0].length) throw new StandardLibraryException("inv() expects a square matrix, but got size " + matrix.length + ", " + matrix[0].length);
                int n = matrix.length;
                double[][] extended = new double[n][2*n];
                for (int i=0; i<n; i++) {
                    System.arraycopy(matrix[i], 0, extended[i], 0, n);
                    extended[i][i+n] = 1;
                }
                for (int col=0; col<n; col++) {
                    int pivot = -1;
                    for (int row=col; row<n; row++) {
                        if (Math.abs(extended[row][col]) > 1e-12) {
                            pivot = row;
                            break;
                        }
                    }
                    if (pivot == -1) throw new StandardLibraryException("inv(): matrix is singular");
                    double[] tmp = extended[col];
                    extended[col] = extended[pivot];
                    extended[pivot] = tmp;

                    double div = extended[col][col];
                    for (int j=0; j<2*n; j++) extended[col][j] /= div;

                    for (int row=0; row<n; row++) {
                        if (row == col) continue;
                        double factor = extended[row][col];
                        for (int j=0; j<2*n; j++) extended[row][j] -= factor * extended[col][j];
                    }
                }

                double[][] result = new double[n][n];
                for (int i=0; i<n; i++)
                    System.arraycopy(extended[i], n, result[i], 0, n);

                return new Value(result, MATRIX);
            }
            throw new StandardLibraryException("inv() does not support " + arg.getType() + " type");
        });
        functions.put("size", args -> {
            if (args.size() != 1) throw new StandardLibraryException("size() expects 1 argument, but got " + args.size());
            Value arg = args.getFirst();
            if (arg.getType() == MATRIX) {
                double[][] matrix = arg.toMatrix();
                int r = matrix.length;
                int c = (r > 0) ? matrix[0].length : 0;
                TupleValue result = new TupleValue(List.of(Value.of(r), Value.of(c)));
                return Value.of(result);
            }
            throw new StandardLibraryException("size() does not support " + arg.getType() + " type");
        });
        functions.put("rows", args -> {
            if (args.size() != 1) throw new StandardLibraryException("rows() expects 1 argument, but got " + args.size());
            Value arg = args.getFirst();
            if (arg.getType() == MATRIX) {
                return Value.of(arg.toMatrix().length);
            }
            throw new StandardLibraryException("rows() does not support " + arg.getType() + " type");
        });
        functions.put("cols", args -> {
            if (args.size() != 1) throw new StandardLibraryException("cols() expects 1 argument, but got " + args.size());
            Value arg = args.getFirst();
            if (arg.getType() == MATRIX) {
                return Value.of(arg.toMatrix()[0].length);
            }
            throw new StandardLibraryException("cols() does not support " + arg.getType() + " type");
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
            double result = Double.NEGATIVE_INFINITY;
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
                    throw new StandardLibraryException("mean() does not support " + arg.getType() + " type");
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
            Value arg = args.getFirst();
            return new Value(arg.getType().name(), STRING);
        });
        functions.put("pow", args -> {
            if (args.size() != 2) throw new StandardLibraryException("pow() expects 2 arguments");
            Value baseVal = args.getFirst();
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
        functions.put("exp", args -> {
            if (args.size() != 1) throw new StandardLibraryException("exp() expects 1 argument, but got " + args.size());
            Value arg = args.getFirst();
            if (arg.getType() == INT || arg.getType() == FLOAT) {
                return new Value(Math.exp(arg.toDouble()), FLOAT);
            }
            throw new StandardLibraryException("exp() does not support " + arg.getType() + " type");
        });
        functions.put("len", args -> {
            if (args.size() != 1) throw new StandardLibraryException("len() expects 1 argument, but got " + args.size());
            Value arg = args.getFirst();
            if (arg.getType() == MATRIX) {
                double[][] m = arg.toMatrix();
                return Value.of((long) m.length * m[0].length);
            }
            if (arg.getType() == TUPLE) {
                return Value.of(arg.toTuple().getValues().size());
            }
            if (arg.getType() == STRING) {
                return Value.of(arg.toString().length());
            }
            throw new StandardLibraryException("len() does not support " + arg.getType() + " type");
        });
        functions.put("at", args -> {
            if (args.size() != 2) throw new StandardLibraryException("at() expects 2 arguments, but got " + args.size());
            Value string = args.getFirst();
            Value index = args.get(1);
            if (string.getType() == STRING && index.getType() == INT) {
                long idx = index.toLong();
                if (idx < 0 || idx >= string.toString().length()) {
                    throw new StandardLibraryException("at() index out of bounds: " + idx);
                }
                return new Value(string.toString().substring((int) idx, (int) idx + 1), STRING);
            }
            throw new StandardLibraryException("at() does not support " + string.getType() + " and " + index.getType());
        });
        functions.put("contains", args -> {
            if (args.size() != 2) throw new StandardLibraryException("contains() expects 2 arguments, but got " + args.size());
            Value array = args.getFirst();
            Value element = args.get(1);
            if (array.getType() == STRING && element.getType() == STRING) {
                return new Value(array.toString().contains(element.toString()), BOOL);
            }
            if (array.getType() == MATRIX && (element.getType() == INT || element.getType() == FLOAT || element.getType() == BOOL)) {
                double[][] m = array.toMatrix();
                for (double[] row : m) {
                    for (double val : row) {
                        if (val == element.toDouble()) return new Value(true, BOOL);
                    }
                }
                return new Value(false, BOOL);
            }
            if (array.getType() == TUPLE) {
                TupleValue tuple = array.toTuple();
                boolean found = tuple.getValues().contains(element); 
                return Value.of(found);
            }
            throw new StandardLibraryException("contains() does not support " + array.getType() + " and " + element.getType());
        });
        functions.put("int", args -> {
            if (args.size() != 1) throw new StandardLibraryException("int() expects 1 argument, but got " + args.size());
            Value arg = args.getFirst();
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
            if (arg.getType() == INT) {
                return arg;
            }
            throw new StandardLibraryException("int() does not support " + arg.getType() + " type");
        });
        functions.put("float", args -> {
            if (args.size() != 1) throw new StandardLibraryException("float() expects 1 argument, but got " + args.size());
            Value arg = args.getFirst();
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
                return new Value((double) arg.toLong(), FLOAT);
            }
            if (arg.getType() == FLOAT) {
                return arg;
            }
            throw new StandardLibraryException("float() does not support " + arg.getType() + " type");
        });
        functions.put("str", args -> {
            if (args.size() != 1) throw new StandardLibraryException("str() expects 1 argument, but got " + args.size());
            Value arg = args.getFirst();
            if (arg.getType() == INT) {
                return new Value(String.valueOf(arg.toLong()), STRING);
            }
            if (arg.getType() == FLOAT) {
                return new Value(String.valueOf(arg.toDouble()), STRING);
            }
            if (arg.getType() == BOOL) {
                return new Value(Boolean.toString(arg.toBoolean()), STRING);
            }
            if (arg.getType() == STRING) {
                return arg;
            }
            throw new StandardLibraryException("str() does not support " + arg.getType() + " type");
        });
        functions.put("bool", args -> {
            if (args.size() != 1) throw new StandardLibraryException("bool() expects 1 argument, but got " + args.size());
            Value arg = args.getFirst();
            if (arg.getType() == INT) {
                return new Value(arg.toLong() != 0, BOOL);
            }
            if (arg.getType() == FLOAT) {
                return new Value(arg.toDouble() != 0.0, BOOL);
            }
            if (arg.getType() == BOOL) {
                return arg;
            }
            throw new StandardLibraryException("bool() does not support " + arg.getType() + " type");
        });
        functions.put("tuple", args -> {
            if (args.size() != 1) throw new StandardLibraryException("tuple() expects 1 argument, but got " + args.size());
            Value arg = args.getFirst();
            if (arg.getType() == MATRIX) {
                double[][] matrix = arg.toMatrix();
                List<Value> tupleRows = new ArrayList<>();

                for (double[] row : matrix) {
                    List<Value> rowElements = new ArrayList<>();
                    for (double val : row) {
                        rowElements.add(new Value(val, FLOAT));
                    }
                    tupleRows.add(Value.of(new TupleValue(rowElements)));
                }
                return Value.of(new TupleValue(tupleRows));
            }
            if (arg.getType() == STRING) {
                String s = arg.toString();
                List<Value> chars = new ArrayList<>();
                for (char c : s.toCharArray()) {
                    chars.add(new Value(String.valueOf(c), STRING));
                }
                return Value.of(new TupleValue(chars));
            }
            if (arg.getType() == TUPLE) {
                return arg;
            }
            return Value.of(new TupleValue(List.of(arg)));
        });
        functions.put("matrix", args -> {
            if (args.size() != 1) throw new StandardLibraryException("matrix() expects 1 argument, but got " + args.size());
            if (args.getFirst().getType() != TUPLE) throw new StandardLibraryException("matrix() expects TUPLE, but got " + args.getFirst().getType());

            TupleValue t = args.getFirst().toTuple();
            int rows = t.size();
            if (rows == 0) return new Value(new double[0][0], MATRIX);

            boolean is2D = t.get(0).getType() == TUPLE;
            int cols = is2D ? t.get(0).toTuple().size() : rows;
            double[][] data = new double[is2D ? rows : 1][cols];

            for (int i = 0; i < (is2D ? rows : 1); i++) {
                TupleValue row = is2D ? t.get(i).toTuple() : t;
                if (row.size() != cols) throw new StandardLibraryException("matrix() inconsistent tuple dimensions");
                for (int j = 0; j < cols; j++) {
                    Value element = row.get(j);
                    if (element.getType() != DataType.INT && element.getType() != DataType.FLOAT) {
                        throw new StandardLibraryException("matrix() cannot convert " + element.getType() + " to double");
                    }
                    data[i][j] = element.toDouble();
                }
            }
            return new Value(data, MATRIX);
        });
        functions.put("range", args -> {
            int size = args.size();
            if (size < 2 || size > 3) throw new StandardLibraryException("range() expects 2 or 3 arguments");

            long start = args.get(0).toInt();
            long end = args.get(1).toInt();
            long step = (size == 3) ? args.get(2).toInt() : 1;

            if (step == 0) throw new StandardLibraryException("range() step cannot be zero");

            List<Value> range = new ArrayList<>();

            if (step > 0) {
                for (long i = start; i <= end; i += step) range.add(Value.of(i));
            } else {
                for (long i = start; i >= end; i += step) range.add(Value.of(i));
            }

            return Value.of(new TupleValue(range));
        });
        functions.put("reverse", args -> {
            if (args.size() != 1) throw new StandardLibraryException("reverse() expects 1 argument, but got " + args.size());
            Value arg = args.getFirst();
            if (arg.getType() == STRING) {
                return new Value(new StringBuilder(arg.toString()).reverse().toString(), STRING);
            }
            if (arg.getType() == TUPLE) {
                List<Value> values = arg.toTuple().getValues();
                return Value.of(new TupleValue(values.reversed()));
            }
            throw new StandardLibraryException("reverse() does not support " + arg.getType() + " type");
        });
        functions.put("rand", args -> {
            if (!args.isEmpty()) throw new StandardLibraryException("rand() expects 0 arguments, but got " + args.size());
            return new Value(rng.nextDouble(), FLOAT);
        });
        functions.put("randint", args -> {
            if (args.size() != 2) throw new StandardLibraryException("randint() expects 2 arguments, but got " + args.size());
            Value min = args.get(0);
            Value max = args.get(1);
            if (min.getType() != INT || max.getType() != INT) throw new StandardLibraryException("randint() expects INT arguments");
            long minVal = min.toLong();
            long maxVal = max.toLong();
            if (minVal > maxVal) throw new StandardLibraryException("randint() min cannot be greater than max");
            return Value.of(rng.nextLong((maxVal - minVal + 1)) + minVal);
        });
        functions.put("randmat", args -> {
            if (args.size() != 2) throw new StandardLibraryException("randmat() expects 2 arguments, but got " + args.size());
            Value rows = args.get(0);
            Value cols = args.get(1);
            if (rows.getType() != INT || cols.getType() != INT) throw new StandardLibraryException("randmat() expects INT arguments");
            int rowsVal = rows.toInt();
            int colsVal = cols.toInt();
            double[][] matrix = new double[rowsVal][colsVal];
            for (int i = 0; i < rowsVal; i++) {
                for (int j = 0; j < colsVal; j++) {
                    matrix[i][j] = rng.nextDouble();
                }
            }
            return new Value(matrix, MATRIX);
        });
        functions.put("seed", args -> {
            if (args.size() != 1) throw new StandardLibraryException("seed() expects 1 argument, but got " + args.size());
            Value seed = args.getFirst();
            if (seed.getType() != INT) throw new StandardLibraryException("seed() expects INT argument");
            rng.setSeed(seed.toLong());
            return Value.NONE;
        });
        functions.put("f_read", args -> {
            if (args.size() != 1) throw new StandardLibraryException("f_read() expects 1 argument, but got " + args.size());
            if (args.getFirst().getType() != STRING) throw new StandardLibraryException("f_read() expects STRING as first argument");
            String path = args.getFirst().toString();
            try {
                String result = Files.readString(resolvePath(path));
                return new Value(result, STRING);
            } catch (IOException e) {
                throw new StandardLibraryException("f_read() cannot read from file: " + e.getMessage());
            }
        });
        functions.put("f_readline", args -> {
            if (args.size() != 2) throw new StandardLibraryException("f_readline() expects 2 arguments, but got " + args.size());
            if (args.getFirst().getType() != STRING) throw new StandardLibraryException("f_readline() expects STRING as first argument, but got" + args.getFirst().getType());
            if (args.get(1).getType() != INT) throw new StandardLibraryException("f_readline() expects INT as second argument, but  got" + args.get(1).getType());
            String path = args.getFirst().toString();
            long n = args.get(1).toLong();
            try {
                List<String> lines = Files.readAllLines(resolvePath(path));
                if (n < 0 || n >= lines.size()) throw new StandardLibraryException("f_readline() line index out of bounds");
                return new Value(lines.get((int) n), STRING);
            } catch (IOException e) {
                throw new StandardLibraryException("f_readline() cannot read file: " + e.getMessage());
            }
        });
        functions.put("f_lines", args -> {
            if (args.size() != 1) throw new StandardLibraryException("f_lines() expects 1 argument, but got " + args.size());
            if (args.getFirst().getType() != STRING) throw new StandardLibraryException("f_lines() expects STRING");
            try {
                return Value.of(java.nio.file.Files.readAllLines(resolvePath(args.getFirst().toString())).size());
            } catch (java.io.IOException e) {
                throw new StandardLibraryException("f_lines() cannot read from file: " + e.getMessage());
            }
        });
        functions.put("f_append", args -> {
            if (args.size() != 2) throw new StandardLibraryException("f_append() expects 2 arguments, but got " + args.size());
            if (args.getFirst().getType() != STRING) throw new StandardLibraryException("f_append() expects STRING as first argument");
            if (args.get(1).getType() != STRING) throw new StandardLibraryException("f_append() expects STRING as second argument");
            String path = args.getFirst().toString();
            String content = args.get(1).toString();
            try {
                Files.writeString(resolvePath(path), content, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
                return Value.NONE;
            } catch (IOException e) {
                throw new StandardLibraryException("f_append() cannot append to file: " + e.getMessage());
            }
        });
        functions.put("f_write", args -> {
            if (args.size() != 2) throw new StandardLibraryException("f_write() expects 2 arguments, but got " + args.size());
            if (args.getFirst().getType() != STRING) throw new StandardLibraryException("f_write() expects STRING as first argument");
            if (args.get(1).getType() != STRING) throw new StandardLibraryException("f_write() expects STRING as second argument");
            String path = args.getFirst().toString();
            String content = args.get(1).toString();
            try {
                Files.writeString(resolvePath(path), content, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
                return Value.NONE;
            } catch (IOException e) {
                throw new StandardLibraryException("f_write() cannot write to file: " + e.getMessage());
            }
        });
    }

    public boolean has(String name) {
        return functions.containsKey(name);
    }

    public Value call(String name, List<Value> args) {
        return functions.get(name).apply(args);
    }

    public Path resolvePath(String path) {
        return fileDir.resolve(path).normalize();
    }
}
