package mrix.interpreter;

import mrix.exceptions.BreakException;
import mrix.exceptions.ContinueException;
import mrix.exceptions.MrixRuntimeException;
import mrix.exceptions.ReturnException;
import mrix.exceptions.StandardLibraryException;
import mrix.nodes.*;
import mrix.stdlib.StandardLibrary;
import mrix.tokens.Token;
import mrix.typechecker.DataType;

import static mrix.tokens.TokenType.*;
import static mrix.typechecker.DataType.*;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class Interpreter implements InterpreterVisitor {
    private Memory memory = new Memory(null);
    private PrintWriter out = new PrintWriter(System.out);
    private StandardLibrary stdlib = new StandardLibrary();

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
                    if (j < matrix[i].length - 1) sb.append(", ");
                }
                sb.append("]");
                if (i < matrix.length - 1) sb.append("\n");
            }
            return sb.toString();
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
            double[][] matrix = value.toMatrix();
            if (matrix.length == 1) {
                int col = variable.getExpressionList().get(0).accept(this).toInt();
                return new Value(matrix[0][col], FLOAT);
            }
            int row = variable.getExpressionList().get(0).accept(this).toInt();
            int col = variable.getExpressionList().get(1).accept(this).toInt();
            if (row < 0 || row >= matrix.length || col < 0 || col >= matrix[0].length) {
                throw new MrixRuntimeException("Matrix index out of bounds", variable.getId().getLine());
            }
            return new Value(matrix[row][col], FLOAT);
        }
        return value;
    }

    private Value applyOp(Value left, Token op, Value right) {
        switch (op.getTokenType()) {
            case EQ:
                return new Value(left.getValue().equals(right.getValue()), BOOL);
            case NOT_EQ:
                return new Value(!left.getValue().equals(right.getValue()), BOOL);
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
                    return new Value(left.toLong() + right.toLong(), INT);
                }
                if (left.getType() == MATRIX && right.getType() == MATRIX) {
                    return applyOp(left, new Token(DOT_ADD, ".+", null, op.getLine()), right);
                }
                if (left.getType() == DataType.STRING && right.getType() == DataType.STRING) {
                    return new Value(left.toString() + right.toString(), DataType.STRING);
                }
                return new Value(left.toDouble() + right.toDouble(), FLOAT);
            case SUB:
                if (left.getType() == INT && right.getType() == INT) {
                    return new Value(left.toLong() - right.toLong(), INT);
                }
                if (left.getType() == MATRIX && right.getType() == MATRIX) {
                    return applyOp(left, new Token(DOT_SUB, ".-", null, op.getLine()), right);
                }
                if (left.getType() == DataType.STRING && right.getType() == DataType.STRING) {
                    return new Value(left.toString().replaceFirst(right.toString(), ""), DataType.STRING);
                }
                return new Value(left.toDouble() - right.toDouble(), FLOAT);
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
                    return new Value(left.toLong() / right.toLong(), INT);
                }
                if (left.getType() == MATRIX && right.getType() == MATRIX) {
                    return applyOp(left, new Token(DOT_DIV, "./", null, op.getLine()), right);
                }
                if (right.toDouble() == 0) {
                    throw new MrixRuntimeException("Zero division", op.getLine());
                }
                return new Value(left.toDouble() / right.toDouble(), FLOAT);
            case MUL:
                if (left.getType() == INT && right.getType() == INT) {
                    return new Value(left.toLong() * right.toLong(), INT);
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
                return new Value(left.toDouble() * right.toDouble(), FLOAT);
            case MOD:
                if (left.getType() == INT && right.getType() == INT) {
                    if (right.toLong() == 0) {
                        throw new MrixRuntimeException("Zero division", op.getLine());
                    }
                    return new Value(left.toLong() % right.toLong(), INT);
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

    public Value visitPrimaryNode(PrimaryNode node) {
        if (node.getCachedValue() != null) return node.getCachedValue();
        Token token = node.getValue();
        Value result;
        switch (token.getTokenType()) {
            case INT_NUM: result = Value.of((long) token.getLiteral()); break;
            case FLOAT_NUM: result = new Value((double) token.getLiteral(), FLOAT); break;
            case STRING: result = new Value(token.getLiteral(), DataType.STRING); break;
            case TRUE: result = Value.TRUE; break;
            case FALSE: result = Value.FALSE; break;
            default: return Value.NULL; 
        }
        node.setCachedValue(result);
        return result;
    }

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
            double[][] matrix = existing.toMatrix();
            if (matrix.length == 1) {
                int col = variable.getExpressionList().get(0).accept(this).toInt();
                matrix[0][col] = value.toDouble();
            } else {
                int row = variable.getExpressionList().get(0).accept(this).toInt();
                int col = variable.getExpressionList().get(1).accept(this).toInt();
                matrix[row][col] = value.toDouble();
            }
        } else {
            if (isNew) memory.put(name, value);
            else memory.set(name, value);
        }
    }

    public Value visitAssignNode(AssignNode node) {
        Value value = node.getExpression().accept(this);
        VariableNode variable = (VariableNode) node.getVariable();
        switch (node.getOp().getTokenType()) {
            case ASSIGN:
                assignToVariable(variable, value, true);
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
            }
            default:
                throw new MrixRuntimeException("Unknown assignment operator: '" + node.getOp().getLexeme()+ "'", node.getOp().getLine());
        }
        return null;
    }

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

    public Value visitUnaryOpNode(UnaryOpNode node) {
        Token op = node.getOp();
        Value value = node.getUnaryExpression().accept(this);
        if (op.getTokenType() == NOT) {
            if (value.getType() == BOOL) return Value.of(!value.toBoolean());
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

    public Value visitPostfixNode(PostfixNode node) {
        Token op = node.getOp();
        Value value = node.getPrimary().accept(this);
        if (op == null) return value;
        if (op.getTokenType() == TRANSPOSE && value.getType() == MATRIX) {
            double[][] matrix = value.toMatrix();
            int rows = matrix.length;
            int cols = matrix[0].length;
            double[][] result = new double[cols][rows];
            for (int i=0; i<rows; i++) {
                for (int j=0; j<cols; j++) {
                    result[j][i] = matrix[i][j];
                }
            }
            return new Value(result, MATRIX);
        }
        throw new MrixRuntimeException("Unknown postfix operator: '" + op.getLexeme() + "'", op.getLine());
    }

    public Value visitMatrixNode(MatrixNode node) {
        List<List<Node>> rows = node.getRows();
        int rowCount = rows.size();
        int colCount = rows.get(0).size();
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

    public Value visitCreateMatrixNode(CreateMatrixNode node) {
        List<Node> expressionList = node.getExpressionList();
        Token fun = node.getFun();
        int rows, cols;
        if (expressionList.size() == 1) {
            int n = expressionList.get(0).accept(this).toInt();
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

    public Value visitIfNode(IfNode node) {
        Value value = node.getCondition().accept(this);
        if (value.getType() != BOOL) {
            throw new MrixRuntimeException("IF condition must be BOOL, got: " + value.getType(), node.getCondition().getLine());
        }
        if (value.toBoolean()) node.getThenNode().accept(this);
        else if (node.getElseNode() != null) node.getElseNode().accept(this);
        return null;
    }

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
            } catch (ContinueException e) {
            }
        }
        memory = memory.pop();
        return null;
    }

    public Value visitForNode(ForNode node) {
        int rangeStart = node.getRangeStart().accept(this).toInt();
        int rangeEnd = node.getRangeEnd().accept(this).toInt();
        String id = node.getId().getLexeme();
        memory = memory.push();
        for (int i=rangeStart; i<=rangeEnd; i++) {
            memory.put(id, Value.of(i));
            try {
                node.getInstruction().accept(this);
            } catch (BreakException e) {
                break;
            } catch (ContinueException e) {
            }
        }
        memory = memory.pop();
        return null;
    }

    public Value visitBreakNode(BreakNode node) {
        throw new BreakException();
    }

    public Value visitContinueNode(ContinueNode node) {
        throw new ContinueException();
    }

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
                    out.print(val.toString());
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

    public Value visitReturnNode(ReturnNode node) {
        Node expression = node.getExpression();
        if (expression != null) {
            throw new ReturnException(expression.accept(this));
        }
        throw new ReturnException(null);
    }

    public Value visitBlockNode(BlockNode node) {
        Node instructions = node.getInstructions();
        memory = memory.push();
        instructions.accept(this);
        memory = memory.pop();
        return null;
    }

    public Value visitProgramNode(ProgramNode node) {
        List<Node> instructions = node.getInstructions();
        for (Node instruction : instructions) {
            instruction.accept(this);
        }
        return null;
    }

    public Value visitFunctionNode(FunctionNode node) {
        String name = node.getId().getLexeme();
        memory.put(name, new Value(node, DataType.FUNCTION));
        return null;
    }

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

        memory = memory.push();
        for (int i=0; i<params.size(); i++) {
            Value argValue = args.get(i).accept(this);
            memory.put(params.get(i).getLexeme(), argValue);
        }
        
        Value result = null;
        try {
            function.getInstruction().accept(this);
        } catch (ReturnException e) {
            result = e.value;
        }
        
        memory = memory.pop();
        return result;
    }

    public Value visitExpressionNode(ExpressionNode node) {
        return node.getOrExpression().accept(this);
    }

    public void finish() {
        out.flush();
    }
}
