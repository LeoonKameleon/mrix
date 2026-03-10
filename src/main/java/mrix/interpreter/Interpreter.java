package mrix.interpreter;

import mrix.exceptions.BreakException;
import mrix.exceptions.ContinueException;
import mrix.exceptions.ReturnException;
import mrix.nodes.*;
import mrix.tokens.Token;
import mrix.typechecker.DataType;

import static mrix.tokens.TokenType.*;
import static mrix.typechecker.DataType.*;

import java.util.List;

public class Interpreter implements InterpreterVisitor {
    private Memory memory = new Memory(null);
    private int loopDepth = 0;

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

    public Value visitPrimaryNode(PrimaryNode node) {
        Token token = node.getValue();
        switch (token.getTokenType()) {
            case INT_NUM: return new Value(Integer.parseInt(token.getLexeme()), INT);
            case FLOAT_NUM: return new Value(Double.parseDouble(token.getLexeme()), FLOAT);
            case STRING: return new Value(token.getLexeme(), DataType.STRING);
            case TRUE: return new Value(Boolean.TRUE, BOOL);
            case FALSE: return new Value(Boolean.FALSE, BOOL);
            default: return new Value(null, UNKNOWN); 
        }
    }

    public Value visitVariableNode(VariableNode node) {
        String name = node.getId().getLexeme();
        Value value = memory.get(name);
        if (value == null) {
            throw new RuntimeException("Line " + node.getId().getLine() + ": Undefined variable '" + name + "'");
        }
        return value;
    }

    public Value visitAssignNode(AssignNode node) {
        Value value = node.getExpression().accept(this);
        VariableNode variable = (VariableNode) node.getVariable();
        String name = variable.getId().getLexeme();
        memory.put(name, value);
        return null;
    }

    public Value visitBinaryOpNode(BinaryOpNode node) {
        Value leftValue = node.getLeft().accept(this);
        Value rightValue = node.getRight().accept(this);
        Token op = node.getOp();
        if (leftValue == null || rightValue == null) return null;
        switch (op.getTokenType()) {
            case AND:
                return new Value((Boolean) leftValue.getValue() && (Boolean) rightValue.getValue(), BOOL);
            case OR:
                return new Value((Boolean) leftValue.getValue() || (Boolean) rightValue.getValue(), BOOL);
            case EQ:
                return new Value(leftValue.getValue().equals(rightValue.getValue()), BOOL);
            case NOT_EQ:
                return new Value(!leftValue.getValue().equals(rightValue.getValue()), BOOL);
            case GREATER:
                return new Value(toDouble(leftValue) > toDouble(rightValue), BOOL);
            case GREATER_EQ:
                return new Value(toDouble(leftValue) >= toDouble(rightValue), BOOL);
            case LESS:
                return new Value(toDouble(leftValue) < toDouble(rightValue), BOOL);
            case LESS_EQ:
                return new Value(toDouble(leftValue) <= toDouble(rightValue), BOOL);
            case ADD:
                if (leftValue.getType() == INT && rightValue.getType() == INT) {
                    return new Value((Integer) leftValue.getValue() + (Integer) rightValue.getValue(), INT);
                }
                if (leftValue.getType() == DataType.STRING && rightValue.getType() == DataType.STRING) {
                    return new Value((String) leftValue.getValue() + (String) rightValue.getValue(), DataType.STRING);
                }
                return new Value(toDouble(leftValue) + toDouble(rightValue), FLOAT);
            case SUB:
                if (leftValue.getType() == INT && rightValue.getType() == INT) {
                    return new Value((Integer) leftValue.getValue() - (Integer) rightValue.getValue(), INT);
                }
                if (leftValue.getType() == DataType.STRING && rightValue.getType() == DataType.STRING) {
                    return new Value(((String) leftValue.getValue()).replaceFirst((String) rightValue.getValue(), ""), DataType.STRING);
                }
                return new Value(toDouble(leftValue) - toDouble(rightValue), FLOAT);
            case DIV:
                if (leftValue.getType() == MATRIX && (rightValue.getType() == INT || rightValue.getType() == FLOAT)) {
                    double[][] matrix = (double[][]) leftValue.getValue();
                    double scalar = toDouble(rightValue);
                    double[][] result = new double[matrix.length][matrix[0].length];
                    for (int i = 0; i < matrix.length; i++)
                        for (int j = 0; j < matrix[0].length; j++)
                            result[i][j] = matrix[i][j] / scalar;
                    return new Value(result, MATRIX);
                }
                if (leftValue.getType() == INT && rightValue.getType() == INT) {
                    return new Value((Integer)leftValue.getValue() / (Integer)rightValue.getValue(), INT);
                }
                return new Value(toDouble(leftValue) / toDouble(rightValue), FLOAT);
            case MUL:
                if (leftValue.getType() == MATRIX && (rightValue.getType() == INT || rightValue.getType() == FLOAT)) {
                    double[][] matrix = (double[][]) leftValue.getValue();
                    double scalar = toDouble(rightValue);
                    double[][] result = new double[matrix.length][matrix[0].length];
                    for (int i=0; i<matrix.length; i++)
                        for (int j=0; j<matrix[0].length; j++)
                            result[i][j] = matrix[i][j] * scalar;
                    return new Value(result, MATRIX);
                }
                if (leftValue.getType() == INT && rightValue.getType() == INT) {
                    return new Value((Integer)leftValue.getValue() * (Integer)rightValue.getValue(), INT);
                }
                if (leftValue.getType() == DataType.STRING && rightValue.getType() == INT) {
                    return new Value(((String) leftValue.getValue()).repeat((Integer) rightValue.getValue()), DataType.STRING);
                }
                if (leftValue.getType() == INT && rightValue.getType() == DataType.STRING) {
                    return new Value(((String) rightValue.getValue()).repeat((Integer) leftValue.getValue()), DataType.STRING);
                }
                return new Value(toDouble(leftValue) * toDouble(rightValue), FLOAT);
            case DOT_ADD:
            case DOT_SUB:
            case DOT_MUL:
            case DOT_DIV:
                double[][] left = (double[][]) leftValue.getValue();
                double[][] right = (double[][]) rightValue.getValue();
                double[][] result = new double[left.length][left[0].length];
                for (int i=0; i<left.length; i++) {
                    for (int j=0; j<left[0].length; j++) {
                        switch (op.getTokenType()) {
                            case DOT_ADD: result[i][j] = left[i][j] + right[i][j]; break;
                            case DOT_SUB: result[i][j] = left[i][j] - right[i][j]; break;
                            case DOT_MUL: result[i][j] = left[i][j] * right[i][j]; break;
                            case DOT_DIV: result[i][j] = left[i][j] / right[i][j]; break;
                            default: break;
                        }
                    }
                }
                return new Value(result, DataType.MATRIX);
            default:
                return null;
        }
    }

    public Value visitUnaryOpNode(UnaryOpNode node) {
        Token op = node.getOp();
        Value value = node.getUnaryExpression().accept(this);
        if (op.getTokenType() == NOT) {
            if (value.getType() == BOOL) return new Value(!(Boolean) value.getValue(), BOOL);
            return null;
        }
        if (op.getTokenType() == SUB) {
            if (value.getType() == INT) return new Value(-(Integer) value.getValue(), INT);
            if (value.getType() == FLOAT) return new Value(-(double) value.getValue(), INT);
            if (value.getType() == MATRIX) {
                double[][] result = (double[][]) value.getValue();
                for (int i=0; i<result.length; i++) {
                    for (int j=0; j<result[0].length; j++) {
                        result[i][j] *= -1;
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
        double[][] result = new double[rows.size()][rows.get(0).size()];
        for (int i=0; i<rows.size(); i++) {
            for (int j=0; j<rows.get(i).size(); j++) {
                Value value = rows.get(i).get(j).accept(this);
                result[i][j] = toDouble(value);
            }
        }
        return new Value(result, MATRIX);
    }

    public Value visitFlatMatrixNode(FlatMatrixNode node) {
        List<Node> row = node.getExpressionList();
        double[][] result = new double[1][row.size()];
        for (int i=0; i<row.size(); i++) {
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
            int n = (Integer) expressionList.get(0).accept(this).getValue();
            rows = n;
            cols = n;
        } else {
            rows = (Integer) expressionList.get(0).accept(this).getValue();
            cols = (Integer) expressionList.get(1).accept(this).getValue();
        }
        double[][] result = new double[rows][cols];
        switch (fun.getTokenType()) {
            case EYE:
                for (int i = 0; i < rows; i++)
                    result[i][i] = 1.0;
                break;
            case ZEROS:
                for (int i = 0; i < rows; i++)
                    for (int j = 0; j < cols; j++)
                        result[i][j] = 1.0;
                break;
            case ONES:
                break;
            default:
                return null;
        }
        return new Value(result, MATRIX);
    }

    public Value visitIfNode(IfNode node) {
        Value value = node.getCondition().accept(this);
        if ((Boolean) value.getValue()) node.getThenNode().accept(this);
        if (node.getElseNode() != null) node.getElseNode().accept(this);
        return null;
    }

    public Value visitWhileNode(WhileNode node) {
        Value value = node.getCondition().accept(this);
        while ((Boolean) value.getValue()) {
            memory = memory.push();
            try {
                node.getThenNode().accept(this);
            } catch (BreakException e) {
                memory = memory.pop();
                break;
            } catch (ContinueException e) {
            }
            memory = memory.pop();
        }
        return null;
    }

    public Value visitForNode(ForNode node) {
        int rangeStart = (Integer) node.getRangeStart().accept(this).getValue();
        int rangeEnd = (Integer) node.getRangeEnd().accept(this).getValue();
        String id = node.getId().getLexeme();
        for (int i=rangeStart; i<=rangeEnd; i++) {
            memory = memory.push();
            memory.put(id, new Value(i, DataType.INT));
            try {
                node.getInstruction().accept(this);
            } catch (BreakException e) {
                memory = memory.pop();
                break;
            } catch (ContinueException e) {
            }
            memory = memory.pop();
        }
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
            System.out.print(formatValue(val));
            if (i < expressionList.size() - 1) System.out.print(" ");
        }
        System.out.println();
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
}
