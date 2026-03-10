package mrix.interpreter;

import mrix.exceptions.BreakException;
import mrix.exceptions.ContinueException;
import mrix.exceptions.ReturnException;
import mrix.nodes.*;
import mrix.tokens.Token;
import mrix.tokens.TokenType;
import mrix.typechecker.DataType;

import static mrix.tokens.TokenType.*;
import static mrix.typechecker.DataType.*;

import java.io.PrintWriter;
import java.util.List;

public class Interpreter implements InterpreterVisitor {
    private Memory memory = new Memory(null);
    private PrintWriter out = new PrintWriter(System.out);

    private double toDouble(Value v) {
        if (v.getType() == INT) return ((Integer) v.getValue()).doubleValue();
        if (v.getType() == BOOL) return (Boolean) v.getValue() ? 1.0 : 0.0;
        return (Double) v.getValue();
    }

    private String formatValue(Value v) {
        if (v.getType() == MATRIX) {
            double[][] matrix = (double[][]) v.getValue();
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
        if (variable.getExpressionList() != null && !variable.getExpressionList().isEmpty()) {
            double[][] matrix = (double[][]) value.getValue();
            if (matrix.length == 1) {
                int col = (int) toDouble(variable.getExpressionList().get(0).accept(this));
                return new Value(matrix[0][col], FLOAT);
            }
            int row = (int) toDouble(variable.getExpressionList().get(0).accept(this));
            int col = (int) toDouble(variable.getExpressionList().get(1).accept(this));
            return new Value(matrix[row][col], FLOAT);
        }
        return value;
    }

    private Value applyOp(Value left, TokenType op, Value right) {
        switch (op) {
            case EQ:
                return new Value(left.getValue().equals(right.getValue()), BOOL);
            case NOT_EQ:
                return new Value(!left.getValue().equals(right.getValue()), BOOL);
            case GREATER:
                return new Value(toDouble(left) > toDouble(right), BOOL);
            case GREATER_EQ:
                return new Value(toDouble(left) >= toDouble(right), BOOL);
            case LESS:
                return new Value(toDouble(left) < toDouble(right), BOOL);
            case LESS_EQ:
                return new Value(toDouble(left) <= toDouble(right), BOOL);
            case ADD:
                if (left.getType() == INT && right.getType() == INT) {
                    return new Value((Integer) left.getValue() + (Integer) right.getValue(), INT);
                }
                if (left.getType() == DataType.STRING && right.getType() == DataType.STRING) {
                    return new Value((String) left.getValue() + (String) right.getValue(), DataType.STRING);
                }
                return new Value(toDouble(left) + toDouble(right), FLOAT);
            case SUB:
                if (left.getType() == INT && right.getType() == INT) {
                    return new Value((Integer) left.getValue() - (Integer) right.getValue(), INT);
                }
                if (left.getType() == DataType.STRING && right.getType() == DataType.STRING) {
                    return new Value(((String) left.getValue()).replaceFirst((String) right.getValue(), ""), DataType.STRING);
                }
                return new Value(toDouble(left) - toDouble(right), FLOAT);
            case DIV:
                if (left.getType() == MATRIX && (right.getType() == INT || right.getType() == FLOAT)) {
                    double[][] matrix = (double[][]) left.getValue();
                    double scalar = toDouble(right);
                    double[][] result = new double[matrix.length][matrix[0].length];
                    for (int i = 0; i < matrix.length; i++)
                        for (int j = 0; j < matrix[0].length; j++)
                            result[i][j] = matrix[i][j] / scalar;
                    return new Value(result, MATRIX);
                }
                if (left.getType() == INT && right.getType() == INT) {
                    return new Value((Integer)left.getValue() / (Integer)right.getValue(), INT);
                }
                return new Value(toDouble(left) / toDouble(right), FLOAT);
            case MUL:
                if (left.getType() == INT && right.getType() == INT) {
                    return new Value((Integer)left.getValue() * (Integer)right.getValue(), INT);
                }
                if (left.getType() == DataType.STRING && right.getType() == INT) {
                    return new Value(((String) left.getValue()).repeat((Integer) right.getValue()), DataType.STRING);
                }
                if (left.getType() == INT && right.getType() == DataType.STRING) {
                    return new Value(((String) left.getValue()).repeat((Integer) right.getValue()), DataType.STRING);
                }
                if (left.getType() == MATRIX && (right.getType() == INT || right.getType() == FLOAT)) {
                    double[][] matrix = (double[][]) left.getValue();
                    double scalar = toDouble(right);
                    double[][] result = new double[matrix.length][matrix[0].length];
                    for (int i=0; i<matrix.length; i++)
                        for (int j=0; j<matrix[0].length; j++)
                            result[i][j] = matrix[i][j] * scalar;
                    return new Value(result, MATRIX);
                }
                if ((left.getType() == INT || left.getType() == FLOAT) && right.getType() == MATRIX) {
                    double[][] matrix = (double[][]) right.getValue();
                    double scalar = toDouble(left);
                    double[][] result = new double[matrix.length][matrix[0].length];
                    for (int i=0; i<matrix.length; i++)
                        for (int j=0; j<matrix[0].length; j++)
                            result[i][j] = matrix[i][j] * scalar;
                    return new Value(result, MATRIX);
                }
                if (left.getType() == MATRIX && right.getType() == MATRIX) {
                    double[][] leftMatrix = (double[][]) left.getValue();
                    double[][] rightMatrix = (double[][]) right.getValue();
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
                    }
                }
                return new Value(toDouble(left) * toDouble(right), FLOAT);
            case DOT_ADD:
            case DOT_SUB:
            case DOT_MUL:
            case DOT_DIV:
                double[][] leftMatrix = (double[][]) left.getValue();
                double[][] rightMatrix = (double[][]) right.getValue();
                double[][] result = new double[leftMatrix.length][leftMatrix[0].length];
                for (int i=0; i<leftMatrix.length; i++) {
                    for (int j=0; j<leftMatrix[0].length; j++) {
                        switch (op) {
                            case DOT_ADD: result[i][j] = leftMatrix[i][j] + rightMatrix[i][j]; break;
                            case DOT_SUB: result[i][j] = leftMatrix[i][j] - rightMatrix[i][j]; break;
                            case DOT_MUL: result[i][j] = leftMatrix[i][j] * rightMatrix[i][j]; break;
                            case DOT_DIV: result[i][j] = leftMatrix[i][j] / rightMatrix[i][j]; break;
                            default: break;
                        }
                    }
                }
                return new Value(result, DataType.MATRIX);
            default:
                return null;
        }
    }

    public Value visitPrimaryNode(PrimaryNode node) {
        if (node.getCachedValue() != null) return node.getCachedValue();
        Token token = node.getValue();
        Value result;
        switch (token.getTokenType()) {
            case INT_NUM: result = Value.of(Integer.parseInt(token.getLexeme())); break;
            case FLOAT_NUM: result = new Value(Double.parseDouble(token.getLexeme()), FLOAT); break;
            case STRING: result = new Value(token.getLexeme(), DataType.STRING); break;
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
            throw new RuntimeException("Line " + node.getId().getLine() + ": Undefined variable '" + node.getId().getLexeme() + "'");
        return value;
    }

    private void assignToVariable(VariableNode variable, Value value, boolean isNew) {
        String name = variable.getId().getLexeme();
        if (variable.getExpressionList() != null && !variable.getExpressionList().isEmpty()) {
            double[][] matrix = (double[][]) memory.get(name).getValue();
            if (matrix.length == 1) {
                int col = (int) toDouble(variable.getExpressionList().get(0).accept(this));
                matrix[0][col] = toDouble(value);
            } else {
                int row = (int) toDouble(variable.getExpressionList().get(0).accept(this));
                int col = (int) toDouble(variable.getExpressionList().get(1).accept(this));
                matrix[row][col] = toDouble(value);
            }
        } else {
            if (isNew) memory.put(name, value);
            else memory.set(name, value);
        }
    }

    public Value visitAssignNode(AssignNode node) {
        Value value = node.getExpression().accept(this);
        VariableNode variable = (VariableNode) node.getVariable();
        switch (node.getOp()) {
            case ASSIGN:
                assignToVariable(variable, value, true);
                break;
            case ADD_ASSIGN:
                assignToVariable(variable, applyOp(getVariable(variable), ADD, value), false);
                break;
            case SUB_ASSIGN:
                assignToVariable(variable, applyOp(getVariable(variable), SUB, value), false);
                break;
            case MUL_ASSIGN:
                assignToVariable(variable, applyOp(getVariable(variable), MUL, value), false);
                break;
            case DIV_ASSIGN:
                assignToVariable(variable, applyOp(getVariable(variable), DIV, value), false);
                break;
            default:
                throw new RuntimeException("Line " + variable.getId().getLine() + " Error: Unknown assignment operator: " + node.getOp());
        }
        return null;
    }

    public Value visitBinaryOpNode(BinaryOpNode node) {
        Token op = node.getOp();
        if (op.getTokenType() == AND) {
            Value left = node.getLeft().accept(this);
            if (!(Boolean) left.getValue()) return Value.FALSE;
            return Value.of((Boolean) node.getRight().accept(this).getValue());
        }
        if (op.getTokenType() == OR) {
            Value left = node.getLeft().accept(this);
            if ((Boolean) left.getValue()) return Value.TRUE;
            return Value.of((Boolean) node.getRight().accept(this).getValue());
        }
        Value leftValue = node.getLeft().accept(this);
        Value rightValue = node.getRight().accept(this);
        if (leftValue == null || rightValue == null) return null;
        return applyOp(leftValue, op.getTokenType(), rightValue);
    }

    public Value visitUnaryOpNode(UnaryOpNode node) {
        Token op = node.getOp();
        Value value = node.getUnaryExpression().accept(this);
        if (op.getTokenType() == NOT) {
            if (value.getType() == BOOL) return Value.of(!(Boolean) value.getValue());
            return null;
        }
        if (op.getTokenType() == SUB) {
            if (value.getType() == INT) return Value.of(-(Integer) value.getValue());
            if (value.getType() == FLOAT) return new Value(-(double) value.getValue(), FLOAT);
            if (value.getType() == MATRIX) {
                double[][] original = (double[][]) value.getValue();
                double[][] result = new double[original.length][original[0].length];
                for (int i=0; i<result.length; i++) {
                    for (int j=0; j<result[0].length; j++) {
                        result[i][j] = -original[i][j];
                    }
                }
                return new Value(result, MATRIX);
            }
            return null;
        }
        return null;
    }

    public Value visitPostfixNode(PostfixNode node) {
        Token op = node.getOp();
        Value value = node.getPrimary().accept(this);
        if (op == null) return value;
        if (op.getTokenType() == TRANSPOSE && value.getType() == MATRIX) {
            double[][] matrix = (double[][]) value.getValue();
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
        return null;
    }

    public Value visitMatrixNode(MatrixNode node) {
        List<List<Node>> rows = node.getRows();
        int rowCount = rows.size();
        int colCount = rows.get(0).size();
        double[][] result = new double[rowCount][colCount];
        for (int i=0; i<rowCount; i++) {
            for (int j=0; j<colCount; j++) {
                Value value = rows.get(i).get(j).accept(this);
                result[i][j] = toDouble(value);
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
            result[0][i] = toDouble(value);
        }
        return new Value(result, MATRIX);
    }

    public Value visitCreateMatrixNode(CreateMatrixNode node) {
        List<Node> expressionList = node.getExpressionList();
        Token fun = node.getFun();
        int rows, cols;
        if (expressionList.size() == 1) {
            int n = (int) toDouble(expressionList.get(0).accept(this));
            rows = n;
            cols = n;
        } else {
            rows = (int) toDouble(expressionList.get(0).accept(this));
            cols = (int) toDouble(expressionList.get(1).accept(this));
        }
        double[][] result = new double[rows][cols];
        switch (fun.getTokenType()) {
            case EYE:
                for (int i = 0; i < rows; i++)
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
        if ((Boolean) value.getValue()) node.getThenNode().accept(this);
        else if (node.getElseNode() != null) node.getElseNode().accept(this);
        return null;
    }

    public Value visitWhileNode(WhileNode node) {
        memory = memory.push();
        while ((Boolean) node.getCondition().accept(this).getValue()) {
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
        int rangeStart = (int) toDouble(node.getRangeStart().accept(this));
        int rangeEnd = (int) toDouble(node.getRangeEnd().accept(this));
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
                    out.print((int) val.getValue());
                    break;
                case FLOAT:
                    out.print((double) val.getValue());
                    break;
                case STRING:
                    out.print((String) val.getValue());
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
        Value value = memory.get(id.getLexeme());
        if (value == null) {
            throw new RuntimeException("Line " + node.getId().getLine() + ": Undefined function '" + node.getId().getLexeme() + "'");
        }
        if (value.getType() != DataType.FUNCTION) {
            throw new RuntimeException("Line " + node.getId().getLine() + ": '" + node.getId().getLexeme() + "' is not a function");
        }
        FunctionNode function = (FunctionNode) value.getValue();
        List<Token> params = function.getParameterList();
        List<Node> args = node.getExpressionList();

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
