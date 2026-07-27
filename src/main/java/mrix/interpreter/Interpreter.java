package mrix.interpreter;

import mrix.ast.*;
import mrix.exception.MrixRuntimeException;
import mrix.exception.StandardLibraryException;
import mrix.interpreter.flow.BreakException;
import mrix.interpreter.flow.ContinueException;
import mrix.interpreter.flow.ReturnException;
import mrix.interpreter.value.HMapValue;
import mrix.interpreter.value.TupleValue;
import mrix.interpreter.value.Value;
import mrix.parser.Parser;
import mrix.scanner.Scanner;
import mrix.scanner.token.Token;
import mrix.stdlib.StandardLibrary;
import mrix.typing.type.DataType;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static mrix.scanner.token.TokenType.*;
import static mrix.typing.type.DataType.*;
import static mrix.typing.type.DataType.HMAP;

public class Interpreter implements InterpreterVisitor {
    private Memory memory = new Memory(null);
    private final PrintWriter out = new PrintWriter(System.out, true);
    private final StandardLibrary stdlib;

    public Interpreter(Path fileDir) {
        stdlib = new StandardLibrary(fileDir);
    }

    private String formatValue(Value v) {
        if (v.getType() == MATRIX) {
            double[][] matrix = v.toMatrix();
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < matrix.length; i++) {
                sb.append("[");
                for (int j = 0; j < matrix[i].length; j++) {
                    if (matrix[i][j] == (int) matrix[i][j])
                        sb.append((int) matrix[i][j]);
                    else
                        sb.append(matrix[i][j]);
                    if (j < matrix[i].length - 1) sb.append(" ");
                }
                sb.append("]");
                if (i < matrix.length - 1) sb.append("\n");
            }
            return sb.toString();
        }
        if (v.getType() == TUPLE) {
            return v.getValue().toString();
        }
        if (v.getType() == HMAP) {
            return v.getValue().toString();
        }
        return String.valueOf(v.getValue());
    }

    private Value getVariable(VariableNode variable) {
        String name = variable.getId().getLexeme();
        Value value = memory.get(name);
        if (value == null) {
            throw new MrixRuntimeException("Undefined variable: '" + name + "'", variable.getLine());
        }
        if (variable.getExpressionList() != null && !variable.getExpressionList().isEmpty()) {
            if (value.getType() == TUPLE) {
                TupleValue tuple = (TupleValue) value.getValue();
                int idx = toIndex(variable.getExpressionList().getFirst().accept(this), tuple.getValues().size(), variable.getLine());
                return tuple.getValues().get(idx);
            }
            if (value.getType() == HMAP) {
                HMapValue hmap = value.toHMap();
                Value key = variable.getExpressionList().getFirst().accept(this);
                if (!hmap.containsKey(key)) {
                    throw new MrixRuntimeException("Key '" + key + "' not found in hmap", variable.getLine());
                }
                return hmap.get(key);
            }
            double[][] matrix = value.toMatrix();
            if (matrix.length == 1) {
                int col;
                if (variable.getExpressionList().size() == 2) {
                    col = toIndex(variable.getExpressionList().get(1).accept(this), matrix[0].length, variable.getLine());
                } else {
                    col = toIndex(variable.getExpressionList().getFirst().accept(this), matrix[0].length, variable.getLine());
                }
                return new Value(matrix[0][col], FLOAT);
            }
            int row = toIndex(variable.getExpressionList().get(0).accept(this), matrix.length, variable.getLine());
            if (variable.getExpressionList().size() == 2) {
                int col = toIndex(variable.getExpressionList().get(1).accept(this), matrix[0].length, variable.getLine());
                return new Value(matrix[row][col], FLOAT);
            }
            double[][] res = new double[1][matrix[row].length];
            System.arraycopy(matrix[row], 0, res[0], 0, matrix[row].length);
            return new Value(res, MATRIX);
        }
        return value;
    }

    private Value applyOp(Value left, Token op, Value right) {
        switch (op.getTokenType()) {
            case EQ:
                if (left.getType() == TUPLE && right.getType() == TUPLE) {
                    return Value.of(
                        left.toTuple().equals(right.toTuple())
                    );
                }
                return new Value(left.equals(right), BOOL);
            case NOT_EQ:
                if (left.getType() == TUPLE && right.getType() == TUPLE) {
                    return Value.of(
                        !left.toTuple().equals(right.toTuple())
                    );
                }
                return new Value(!left.equals(right), BOOL);
            case GREATER:
                return new Value(left.toDouble() > right.toDouble(), BOOL);
            case GREATER_EQ:
                return new Value(left.toDouble() >= right.toDouble(), BOOL);
            case LESS:
                return new Value(left.toDouble() < right.toDouble(), BOOL);
            case LESS_EQ:
                return new Value(left.toDouble() <= right.toDouble(), BOOL);
            case ADD:
                if (left.getType() == INT && right.getType() == INT) {
                    return Value.of(left.toLong() + right.toLong());
                }
                if (left.getType() == MATRIX && right.getType() == MATRIX) {
                    return applyOp(left, new Token(DOT_ADD, ".+", null, op.getLine()), right);
                }
                if (left.getType() == DataType.STRING && right.getType() == DataType.STRING) {
                    return new Value(left.toString() + right, DataType.STRING);
                }
                if ((left.getType() == INT || left.getType() == FLOAT) &&
                        (right.getType() == INT || right.getType() == FLOAT)) {
                    return new Value(left.toDouble() + right.toDouble(), FLOAT);
                }
                throw new MrixRuntimeException(
                        "Invalid operand types for '+': " + left.getType() + " and " + right.getType(),
                        op.getLine()
                );
            case SUB:
                if (left.getType() == INT && right.getType() == INT) {
                    return Value.of(left.toLong() - right.toLong());
                }
                if (left.getType() == MATRIX && right.getType() == MATRIX) {
                    return applyOp(left, new Token(DOT_SUB, ".-", null, op.getLine()), right);
                }
                if (left.getType() == DataType.STRING && right.getType() == DataType.STRING) {
                    return new Value(left.toString().replaceFirst(right.toString(), ""), DataType.STRING);
                }
                if ((left.getType() == INT || left.getType() == FLOAT) &&
                        (right.getType() == INT || right.getType() == FLOAT)) {
                    return new Value(left.toDouble() - right.toDouble(), FLOAT);
                }
                throw new MrixRuntimeException(
                        "Invalid operand types for '-': " + left.getType() + " and " + right.getType(),
                        op.getLine()
                );
            case DIV:
                if (left.getType() == MATRIX && (right.getType() == INT || right.getType() == FLOAT)) {
                    double[][] matrix = left.toMatrix();
                    double scalar = right.toDouble();
                    if (scalar == 0) {
                        throw new MrixRuntimeException("Zero division", op.getLine());
                    }
                    double[][] result = new double[matrix.length][matrix[0].length];
                    for (int i = 0; i < matrix.length; i++)
                        for (int j = 0; j < matrix[0].length; j++)
                            result[i][j] = matrix[i][j] / scalar;
                    return new Value(result, MATRIX);
                }
                if (left.getType() == INT && right.getType() == INT) {
                    if (right.toLong() == 0) {
                        throw new MrixRuntimeException("Zero division", op.getLine());
                    }
                    return Value.of(left.toLong() / right.toLong());
                }
                if (left.getType() == MATRIX && right.getType() == MATRIX) {
                    return applyOp(left, new Token(DOT_DIV, "./", null, op.getLine()), right);
                }
                if (right.toDouble() == 0) {
                    throw new MrixRuntimeException("Zero division", op.getLine());
                }
                if ((left.getType() == INT || left.getType() == FLOAT) &&
                        (right.getType() == INT || right.getType() == FLOAT)) {
                    return new Value(left.toDouble() / right.toDouble(), FLOAT);
                }
                throw new MrixRuntimeException(
                        "Invalid operand types for '/': " + left.getType() + " and " + right.getType(),
                        op.getLine()
                );
            case MUL:
                if (left.getType() == INT && right.getType() == INT) {
                    return Value.of(left.toLong() * right.toLong());
                }
                if (left.getType() == DataType.STRING && right.getType() == INT) {
                    return new Value(left.toString().repeat(right.toInt()), DataType.STRING);
                }
                if (left.getType() == INT && right.getType() == DataType.STRING) {
                    return new Value(right.toString().repeat(left.toInt()), DataType.STRING);
                }
                if (left.getType() == MATRIX && (right.getType() == INT || right.getType() == FLOAT)) {
                    double[][] matrix = left.toMatrix();
                    double scalar = right.toDouble();
                    double[][] result = new double[matrix.length][matrix[0].length];
                    for (int i=0; i<matrix.length; i++)
                        for (int j=0; j<matrix[0].length; j++)
                            result[i][j] = matrix[i][j] * scalar;
                    return new Value(result, MATRIX);
                }
                if ((left.getType() == INT || left.getType() == FLOAT) && right.getType() == MATRIX) {
                    double[][] matrix = right.toMatrix();
                    double scalar = left.toDouble();
                    double[][] result = new double[matrix.length][matrix[0].length];
                    for (int i=0; i<matrix.length; i++)
                        for (int j=0; j<matrix[0].length; j++)
                            result[i][j] = matrix[i][j] * scalar;
                    return new Value(result, MATRIX);
                }
                if (left.getType() == MATRIX && right.getType() == MATRIX) {
                    double[][] leftMatrix = left.toMatrix();
                    double[][] rightMatrix = right.toMatrix();
                    if (leftMatrix[0].length == rightMatrix.length) {
                        int rows = leftMatrix.length;
                        int cols = rightMatrix[0].length;
                        int inner = rightMatrix.length;

                        double[][] result = new double[rows][cols];

                        for (int i=0; i<rows; i++) {
                            for (int j=0; j<cols; j++) {
                                for (int k=0; k<inner; k++) {
                                    result[i][j] += leftMatrix[i][k] * rightMatrix[k][j];
                                }
                            }
                        }
                        return new Value(result, MATRIX);
                    } else {
                        throw new MrixRuntimeException("Matrix multiplication size mismatch", op.getLine());
                    }
                }
                if ((left.getType() == INT || left.getType() == FLOAT) &&
                        (right.getType() == INT || right.getType() == FLOAT)) {
                    return new Value(left.toDouble() * right.toDouble(), FLOAT);
                }
                throw new MrixRuntimeException(
                        "Invalid operand types for '*': " + left.getType() + " and " + right.getType(),
                        op.getLine()
                );
            case MOD:
                if (left.getType() == INT && right.getType() == INT) {
                    if (right.toLong() == 0) {
                        throw new MrixRuntimeException("Zero division", op.getLine());
                    }
                    return Value.of(left.toLong() % right.toLong());
                }
                if (right.toDouble() == 0) {
                    throw new MrixRuntimeException("Zero division", op.getLine());
                }
                return new Value(left.toDouble() % right.toDouble(), FLOAT);
            case DOT_ADD:
            case DOT_SUB:
            case DOT_MUL:
            case DOT_DIV:
                double[][] leftMatrix = left.toMatrix();
                double[][] rightMatrix = right.toMatrix();
                if (leftMatrix.length != rightMatrix.length ||
                    leftMatrix[0].length != rightMatrix[0].length) {
                    throw new MrixRuntimeException("Matrix size mismatch", op.getLine());
                }
                double[][] result = new double[leftMatrix.length][leftMatrix[0].length];
                for (int i=0; i<leftMatrix.length; i++) {
                    for (int j=0; j<leftMatrix[0].length; j++) {
                        switch (op.getTokenType()) {
                            case DOT_ADD: result[i][j] = leftMatrix[i][j] + rightMatrix[i][j]; break;
                            case DOT_SUB: result[i][j] = leftMatrix[i][j] - rightMatrix[i][j]; break;
                            case DOT_MUL: result[i][j] = leftMatrix[i][j] * rightMatrix[i][j]; break;
                            case DOT_DIV: {
                                if (rightMatrix[i][j] == 0) {
                                    throw new MrixRuntimeException("Zero division", op.getLine());
                                }
                                result[i][j] = leftMatrix[i][j] / rightMatrix[i][j];
                                break;
                            } 
                            default: break;
                        }
                    }
                }
                return new Value(result, DataType.MATRIX);
            default:
                throw new MrixRuntimeException("Unsupported operator: " + op.getTokenType(), op.getLine());
        }
    }

    private int toIndex(Value value, int max, int line) {
        long idx = value.toLong();
        if (idx < 0 || idx >= max) {
            throw new MrixRuntimeException("Index " + idx + " out of bounds", line);
        }
        return (int) idx;
    }

    private List<Value> toIterable(Value v, int line) {
        switch (v.getType()) {
            case STRING:
                List<Value> chars = new ArrayList<>();
                for (char c : v.toString().toCharArray()) {
                    chars.add(new Value(String.valueOf(c), DataType.STRING));
                }
                return chars;

            case TUPLE:
                return v.toTuple().getValues();

            case MATRIX:
                double[][] m = v.toMatrix();
                List<Value> flat = new ArrayList<>();
                for (double[] row : m) {
                    for (double x : row) {
                        flat.add(new Value(x, DataType.FLOAT));
                    }
                }
                return flat;

            case HMAP:
                List<Value> pairs = new ArrayList<>();
                for (var e : v.toHMap().getMap().entrySet()) {
                    pairs.add(Value.of(new TupleValue(List.of(e.getKey(), e.getValue()))));
                }
                return pairs;

            case ANY:
                throw new MrixRuntimeException("Cannot iterate over ANY type", line);

            default:
                throw new MrixRuntimeException("Type " + v.getType() + " is not iterable", line);
        }
    }

    @Override
    public Value visitPrimaryNode(PrimaryNode node) {
        if (node.getCachedValue() != null) return node.getCachedValue();
        Token token = node.getValue();
        Value result;
        switch (token.getTokenType()) {
            case INT_NUM: result = Value.of((long) token.getLiteral()); break;
            case FLOAT_NUM: result = new Value(token.getLiteral(), FLOAT); break;
            case STRING: result = new Value(token.getLiteral(), DataType.STRING); break;
            case TRUE: result = Value.TRUE; break;
            case FALSE: result = Value.FALSE; break;
            case NONE: result = Value.NONE; break;
            default: return Value.NONE;
        }
        node.setCachedValue(result);
        return result;
    }

    @Override
    public Value visitVariableNode(VariableNode node) {
        Value value = getVariable(node);
        if (value == null)
            throw new MrixRuntimeException("Undefined variable '" + node.getId().getLexeme() + "'", node.getLine());
        return value;
    }

    private void assignToVariable(VariableNode variable, Value value, boolean isNew) {
        String name = variable.getId().getLexeme();
        if (variable.getExpressionList() != null && !variable.getExpressionList().isEmpty()) {
            Value existing = memory.get(name);
            if (existing == null) {
                throw new MrixRuntimeException("Undefined variable '" + name + "'", variable.getId().getLine());
            }
            if (existing.getType() == TUPLE) {
                throw new MrixRuntimeException("Tuples are immutable. Cannot assign to element of '" + name + "'", variable.getLine());
            }
            if (existing.getType() == HMAP) {
                HMapValue hmap = existing.toHMap();
                Value key = variable.getExpressionList().getFirst().accept(this);
                if (key.getType() == MATRIX || key.getType() == HMAP) {
                    throw new MrixRuntimeException("Invalid hmap key type: " + key.getType(), variable.getLine());
                }
                hmap.put(key, value);
                return;
            }
            if (existing.getType() == MATRIX) {
                double[][] matrix = existing.toMatrix();
                if (matrix.length == 1) {
                    int col = toIndex(variable.getExpressionList().getFirst().accept(this), matrix[0].length, variable.getLine());
                    matrix[0][col] = value.toDouble();
                } else {
                    int row = toIndex(variable.getExpressionList().get(0).accept(this), matrix.length, variable.getLine());
                    int col = toIndex(variable.getExpressionList().get(1).accept(this), matrix[0].length, variable.getLine());
                    matrix[row][col] = value.toDouble();
                }
                return;
            }
        }
        if (isNew) memory.put(name, value);
        else memory.set(name, value);
    }

    @Override
    public Value visitAssignNode(AssignNode node) {
        Value value = node.getExpression().accept(this);
        if (node.getVariable() instanceof TuplePatternNode pattern) {
            if (value.getType() != TUPLE) {
                throw new MrixRuntimeException("Cannot unpack non-tuple type: " + value.getType(), node.getLine());
            }
            
            List<Value> elements = ((TupleValue) value.getValue()).getValues();
            List<Token> ids = pattern.getIds();

            if (elements.size() != ids.size()) {
                throw new MrixRuntimeException("Tuple size mismatch. Expected " + elements.size() + " elements, got " + ids.size(), node.getLine());
            }

            for (int i = 0; i < ids.size(); i++) {
                memory.put(ids.get(i).getLexeme(), elements.get(i));
            }
            return value;
        }
        if (node.getVariable() instanceof VariableNode variable) {
            switch (node.getOp().getTokenType()) {
                case ASSIGN:
                    assignToVariable(variable, value, memory.get(variable.getId().getLexeme()) == null);
                    break;
                case ADD_ASSIGN: {
                    Token op = new Token(ADD, "+", null, node.getOp().getLine());
                    assignToVariable(variable, applyOp(getVariable(variable), op, value), false);
                    break;
                }
                case SUB_ASSIGN: {
                    Token op = new Token(SUB, "-", null, node.getOp().getLine());
                    assignToVariable(variable, applyOp(getVariable(variable), op, value), false);
                    break;
                }
                case MUL_ASSIGN: {
                    Token op = new Token(MUL, "*", null, node.getOp().getLine());
                    assignToVariable(variable, applyOp(getVariable(variable), op, value), false);
                    break;
                }
                case DIV_ASSIGN: {
                    Token op = new Token(DIV, "/", null, node.getOp().getLine());
                    assignToVariable(variable, applyOp(getVariable(variable), op, value), false);
                    break;
                }
                case MOD_ASSIGN: {
                    Token op = new Token(MOD, "/", null, node.getOp().getLine());
                    assignToVariable(variable, applyOp(getVariable(variable), op, value), false);
                    break;
                }
                default:
                    throw new MrixRuntimeException("Unknown assignment operator: '" + node.getOp().getLexeme()+ "'", node.getOp().getLine());
            }
            return null;
        }
        return null;
    }

    @Override
    public Value visitBinaryOpNode(BinaryOpNode node) {
        Token op = node.getOp();
        if (op.getTokenType() == AND) {
            Value left = node.getLeft().accept(this);
            if (!left.toBoolean()) return Value.FALSE;
            return Value.of(node.getRight().accept(this).toBoolean());
        }
        if (op.getTokenType() == OR) {
            Value left = node.getLeft().accept(this);
            if (left.toBoolean()) return Value.TRUE;
            return Value.of(node.getRight().accept(this).toBoolean());
        }
        Value leftValue = node.getLeft().accept(this);
        Value rightValue = node.getRight().accept(this);
        if (leftValue == null || rightValue == null) return null;
        return applyOp(leftValue, op, rightValue);
    }

    @Override
    public Value visitUnaryOpNode(UnaryOpNode node) {
        Token op = node.getOp();
        Value value = node.getUnaryExpression().accept(this);
        if (op.getTokenType() == NOT) {
            if (value.getType() == BOOL || value.getType() == DataType.NONE) return Value.of(!value.toBoolean());
            throw new MrixRuntimeException("Invalid type for NOT operator: " + value.getType(), op.getLine());
        }
        if (op.getTokenType() == SUB) {
            if (value.getType() == INT) return Value.of(-value.toLong());
            if (value.getType() == FLOAT) return new Value(-value.toDouble(), FLOAT);
            if (value.getType() == MATRIX) {
                double[][] original = value.toMatrix();
                double[][] result = new double[original.length][original[0].length];
                for (int i=0; i<result.length; i++) {
                    for (int j=0; j<result[0].length; j++) {
                        result[i][j] = -original[i][j];
                    }
                }
                return new Value(result, MATRIX);
            }
            throw new MrixRuntimeException("Invalid type for SUB operator: " + value.getType(), op.getLine());
        }
        throw new MrixRuntimeException("Invalid operand for unary operator: " + op.getTokenType(), op.getLine());
    }

    @Override
    public Value visitPostfixNode(PostfixNode node) {
        Token op = node.getOp();
        Value value = node.getPrimary().accept(this);
        if (op == null) return value;
        if (op.getTokenType() == TRANSPOSE) {
            if (value.getType() != MATRIX) {
                throw new MrixRuntimeException(
                    "Transpose operator expected MATRIX, but got: " + value.getType(),
                    op.getLine()
                );
            }

            double[][] matrix = value.toMatrix();
            int rows = matrix.length;
            int cols = matrix[0].length;

            double[][] result = new double[cols][rows];

            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    result[j][i] = matrix[i][j];
                }
            }

            return new Value(result, MATRIX);
        }
        throw new MrixRuntimeException("Unknown postfix operator: '" + op.getLexeme() + "'", op.getLine());
    }

    @Override
    public Value visitMatrixNode(MatrixNode node) {
        List<List<Node>> rows = node.getRows();
        int rowCount = rows.size();
        int colCount = rows.getFirst().size();
        for (List<Node> row : rows) {
            if (row.size() != colCount) {
                throw new MrixRuntimeException("All rows in a matrix must have the same length", node.getLine());
            }
        }
        double[][] result = new double[rowCount][colCount];
        for (int i=0; i<rowCount; i++) {
            for (int j=0; j<colCount; j++) {
                Value value = rows.get(i).get(j).accept(this);
                result[i][j] = value.toDouble();
            }
        }
        return new Value(result, MATRIX);
    }

    @Override
    public Value visitFlatMatrixNode(FlatMatrixNode node) {
        List<Node> row = node.getExpressionList();
        double[][] result = new double[1][row.size()];
        int size = row.size();
        for (int i=0; i<size; i++) {
            Value value = row.get(i).accept(this);
            result[0][i] = value.toDouble();
        }
        return new Value(result, MATRIX);
    }

    @Override
    public Value visitCreateMatrixNode(CreateMatrixNode node) {
        List<Node> expressionList = node.getExpressionList();
        Token fun = node.getFun();
        int rows, cols;
        if (expressionList.size() == 1) {
            int n = expressionList.getFirst().accept(this).toInt();
            rows = n;
            cols = n;
        } else {
            rows = expressionList.get(0).accept(this).toInt();
            cols = expressionList.get(1).accept(this).toInt();
        }
        if (rows <= 0 || cols <= 0) {
            throw new MrixRuntimeException("Negative matrix size", fun.getLine());
        }
        double[][] result = new double[rows][cols];
        switch (fun.getTokenType()) {
            case EYE:
                for (int i = 0; i < Math.min(rows, cols); i++)
                    result[i][i] = 1.0;
                break;
            case ONES:
                for (int i = 0; i < rows; i++)
                    for (int j = 0; j < cols; j++)
                        result[i][j] = 1.0;
                break;
            case ZEROS:
                break;
            default:
                return null;
        }
        return new Value(result, MATRIX);
    }

    @Override
    public Value visitIfNode(IfNode node) {
        Value value = node.getCondition().accept(this);
        if (value.getType() != BOOL) {
            throw new MrixRuntimeException("IF condition must be BOOL, got: " + value.getType(), node.getCondition().getLine());
        }
        if (value.toBoolean()) node.getThenNode().accept(this);
        else if (node.getElseNode() != null) node.getElseNode().accept(this);
        return null;
    }

    @Override
    public Value visitWhileNode(WhileNode node) {
        memory = memory.push();
        Value value = node.getCondition().accept(this);
        if (value.getType() != BOOL) {
            throw new MrixRuntimeException("WHILE condition must be BOOL, got: " + value.getType(), node.getCondition().getLine());
        }
        while (value.toBoolean()) {
            try {
                node.getThenNode().accept(this);
            } catch (BreakException e) {
                break;
            } catch (ContinueException _) {
            }
            value = node.getCondition().accept(this);
        }
        memory = memory.pop();
        return null;
    }

    @Override
    public Value visitForNode(ForNode node) {
        long rangeStart = node.getRangeStart().accept(this).toLong();
        long rangeEnd = node.getRangeEnd().accept(this).toLong();
        String id = node.getId().getLexeme();
        memory = memory.push();
        if (rangeStart <= rangeEnd) {
            for (long i=rangeStart; i<=rangeEnd; i++) {
                memory.put(id, Value.of(i));
                try {
                    node.getInstruction().accept(this);
                } catch (BreakException e) {
                    break;
                } catch (ContinueException _) {
                }
            }
        } else {
            for (long i=rangeStart; i>=rangeEnd; i--) {
                memory.put(id, Value.of(i));
                try {
                    node.getInstruction().accept(this);
                } catch (BreakException e) {
                    break;
                } catch (ContinueException _) {
                }
            }
        }
        memory = memory.pop();
        return null;
    }

    @Override
    public Value visitIterNode(IterNode node) {
        Value iterableValue = node.getIterable().accept(this);
        List<Value> elements = toIterable(iterableValue, node.getLine());
        memory = memory.push();
        for (Value v : elements) {
            if (node.getId() instanceof TuplePatternNode pattern) {
                List<Token> ids = pattern.getIds();
                List<Value> values = v.toTuple().getValues();
                if (ids.size() != values.size()) {
                    throw new MrixRuntimeException("Tuple size mismatch in iter", node.getLine());
                }
                for (int i = 0; i < ids.size(); i++) {
                    memory.put(ids.get(i).getLexeme(), values.get(i));
                }
            } else {
                memory.put(((VariableNode) node.getId()).getId().getLexeme(), v);
            }
            try {
                node.getInstruction().accept(this);
            } catch (BreakException e) {
                break;
            } catch (ContinueException _) {
            }
        }
        memory = memory.pop();
        return null;
    }

    @Override
    public Value visitBreakNode(BreakNode node) {
        throw new BreakException();
    }

    @Override
    public Value visitContinueNode(ContinueNode node) {
        throw new ContinueException();
    }

    @Override
    public Value visitPrintNode(PrintNode node) {
        List<Node> expressionList = node.getExpressionList();
        for (int i = 0; i < expressionList.size(); i++) {
            Value val = expressionList.get(i).accept(this);
            switch (val.getType()) {
                case INT:
                    out.print(val.toLong());
                    break;
                case FLOAT:
                    out.print(val.toDouble());
                    break;
                case STRING:
                    out.print(val);
                    break;
                default:
                    out.print(formatValue(val));
                    break;
            }
            if (i < expressionList.size() - 1) out.print(" ");
        }
        out.println();
        return null;
    }

    @Override
    public Value visitReturnNode(ReturnNode node) {
        Node expression = node.getExpression();
        if (expression == null) throw new ReturnException(Value.NONE);
        Value v = expression.accept(this);
        throw new ReturnException(v == null ? Value.NONE : v);
    }

    @Override
    public Value visitBlockNode(BlockNode node) {
        Node instructions = node.getInstructions();
        memory = memory.push();
        instructions.accept(this);
        memory = memory.pop();
        return null;
    }

    @Override
    public Value visitProgramNode(ProgramNode node) {
        List<Node> instructions = node.getInstructions();
        for (Node instruction : instructions) {
            instruction.accept(this);
        }
        return null;
    }

    @Override
    public Value visitFunctionNode(FunctionNode node) {
        String name = node.getId().getLexeme();
        memory.put(name, new Value(node, DataType.FUNCTION));
        return null;
    }

    @Override
    public Value visitFunctionCallNode(FunctionCallNode node) {
        Token id = node.getId();
        if (stdlib.has(id.getLexeme())) {
            List<Value> args = new ArrayList<>();
            for (Node arg : node.getExpressionList()) {
                args.add(arg.accept(this));
            }
            try {
                return stdlib.call(id.getLexeme(), args);
            } catch (StandardLibraryException e) {
                throw new MrixRuntimeException(e.getMessage(), id.getLine());
            }
        }
        Value value = memory.get(id.getLexeme());
        if (value == null) {
            throw new MrixRuntimeException("Undefined function: '" + id.getLexeme() + "'", id.getLine());
        }
        if (value.getType() != DataType.FUNCTION) {
            throw new MrixRuntimeException("'" + id.getLexeme() + "' is not a function", id.getLine());
        }
        FunctionNode function = (FunctionNode) value.getValue();
        List<Token> params = function.getParameterList();
        List<Node> args = node.getExpressionList();

        if (params.size() != args.size()) {
            throw new MrixRuntimeException("Wrong number of arguments in function '" + id.getLexeme() + "'", id.getLine());
        }

        List<Value> argValues = new ArrayList<>();
        for (Node arg : args) argValues.add(arg.accept(this));

        Memory initial = memory;
        memory = memory.push();
        for (int i = 0; i < params.size(); i++)
            memory.put(params.get(i).getLexeme(), argValues.get(i));
        
        Value result = Value.NONE;
        try {
            function.getInstruction().accept(this);
        } catch (ReturnException e) {
            result = (e.value == null) ? Value.NONE : e.value;
        } finally {
            memory = initial;
        }
        
        return result;
    }

    @Override
    public Value visitExpressionNode(ExpressionNode node) {
        return node.getOrExpression().accept(this);
    }

    @Override
    public Value visitImportNode(ImportNode node) {
        String path = node.getPath().getLiteral().toString();
        try {
            String content = Files.readString(stdlib.resolvePath(path));
            Scanner scanner = new Scanner(content);
            List<Token> tokens = scanner.tokenize();
            Parser parser = new Parser(tokens);
            Node ast = parser.parseProgram();
            ast.accept(this);        
        } catch (IOException | InvalidPathException e) {
            throw new MrixRuntimeException("Cannot import file '" + path + "'", node.getLine());
        }
        return null;
    }

    @Override
    public Value visitTupleNode(TupleNode node ) {
        List<Value> values = new ArrayList<>();
        
        for (Node element : node.getElements()) {
            values.add(element.accept(this));
        }
        return Value.of(new TupleValue(values));
    }

    @Override
    public Value visitTuplePatternNode(TuplePatternNode node) {
        return null; 
    }

    @Override
    public Value visitHMapNode(HMapNode node) {
        HMapValue map = new HMapValue();
        List<Node> keys = node.getKeys();
        List<Node> values = node.getValues();
        if (keys.size() != values.size()) {
            throw new MrixRuntimeException("HMap size mismatch. Expected " + keys.size() + " elements, got " + values.size(), node.getLine());
        }
        for (int i = 0; i < keys.size(); i++) {
            Value key = keys.get(i).accept(this);
            if (key.getType() == MATRIX || key.getType() == HMAP) {
                throw new MrixRuntimeException("Invalid hmap key type: " + key.getType(), node.getLine());
            }
            map.put(key, values.get(i).accept(this));
        }
        return Value.of(map);
    }

    public void finish() {
        out.flush();
    }
}
