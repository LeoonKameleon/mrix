package mrix.typechecker;

import mrix.nodes.*;
import mrix.tokens.Token;
import mrix.tokens.TokenType;

import static mrix.tokens.TokenType.*;
import static mrix.typechecker.DataType.*;

import java.util.ArrayList;
import java.util.List;

public class TypeChecker implements NodeVisitor {
    private SymbolTable table = new SymbolTable(null);
    private List<String> errors = new ArrayList<String>();
    private int loopDepth;

    public DataType visitPrimaryNode(PrimaryNode node) {
        switch(node.getValue().getTokenType()) {
            case INT_NUM: return INT;
            case FLOAT_NUM: return FLOAT;
            case TokenType.STRING: return DataType.STRING;
            case TRUE:
            case FALSE: return BOOL;
            default: return UNKNOWN;
        }
    }

    public DataType visitVariableNode(VariableNode node) {
        VariableSymbol symbol = table.get(node.getId().getLexeme());
        if (symbol == null) {
            errors.add("Line " + node.getId().getLine() + ": Undefined variable '" + node.getId().getLexeme() + "'");
            return UNKNOWN;
        } 
        return symbol.getType();
    }

    public DataType visitAssignNode(AssignNode node) {
        DataType type = node.getExpression().accept(this);
        VariableNode variable = (VariableNode) node.getVariable();
        String name = variable.getId().getLexeme();
        table.put(name, new VariableSymbol(name, type));
        return UNKNOWN;
    }

    public DataType visitBinaryOpNode(BinaryOpNode node) {
        DataType leftType = node.getLeft().accept(this);
        DataType rightType = node.getRight().accept(this);
        Token op = node.getOp();
        if (leftType == UNKNOWN || rightType == UNKNOWN) return UNKNOWN;
        switch (op.getTokenType()) {
            case AND:
            case OR: 
                if (leftType == BOOL && rightType == BOOL) return BOOL;
                errors.add("Line " + op.getLine() + ": Type mismatch for operator '" + op.getTokenType() + "': " + leftType + " and " + rightType);
            case EQ:
            case NOT_EQ:
            case GREATER:
            case GREATER_EQ:
            case LESS:
            case LESS_EQ:
                if (leftType == rightType) return BOOL;
                if ((leftType == INT && rightType == FLOAT) ||
                    (leftType == FLOAT && rightType == INT)) return BOOL;
                errors.add("Line " + op.getLine() + ": Type mismatch for operator '" + op.getTokenType() + "': " + leftType + " and " + rightType);
            case ADD:
            case SUB:
                if (leftType == rightType) {
                    if (leftType != BOOL) return leftType;
                }
                if ((leftType == INT && rightType == FLOAT) ||
                    (leftType == FLOAT && rightType == INT)) return FLOAT;
                errors.add("Line " + op.getLine() + ": Type mismatch for operator '" + op.getTokenType() + "': " + leftType + " and " + rightType);
            case DIV:
                if (leftType == rightType) {
                    if (leftType == INT) return INT;
                    if (leftType == FLOAT) return FLOAT;
                }
                if ((leftType == INT && rightType == FLOAT) ||
                    (leftType == FLOAT && rightType == INT)) return FLOAT;
                if (leftType == MATRIX && (rightType == INT || rightType == FLOAT)) return MATRIX;
                errors.add("Line " + op.getLine() + ": Type mismatch for operator '" + DIV + "': " + leftType + " and " + rightType);
            case MUL:
                if (leftType == rightType) {
                    if (leftType == INT) return INT;
                    if (leftType == FLOAT) return FLOAT;
                    if (leftType == MATRIX) return MATRIX;
                }
                if ((leftType == INT && rightType == FLOAT) ||
                    (leftType == FLOAT && rightType == INT)) return FLOAT;
                if (((leftType == INT || leftType == FLOAT) && rightType == MATRIX) ||
                    (leftType == MATRIX && (rightType == INT || rightType == FLOAT))) return MATRIX;
                if ((leftType == INT && rightType == DataType.STRING) ||
                    (leftType == DataType.STRING && rightType == INT)) return FLOAT;
                errors.add("Line " + op.getLine() + ": Type mismatch for operator '" + MUL + "': " + leftType + " and " + rightType);
            case DOT_ADD:
            case DOT_SUB:
            case DOT_MUL:
            case DOT_DIV:
                if (leftType == MATRIX && rightType == MATRIX) return MATRIX;
                errors.add("Line " + op.getLine() + ": Type mismatch for operator '" + op.getTokenType() + "': " + leftType + " and " + rightType);
            default:
                errors.add("Line " + op.getLine() + ": Unknown operator '" + op.getTokenType() + "'");
                return UNKNOWN;
        }
    }

    public DataType visitUnaryOpNode(UnaryOpNode node) {
        Token op = node.getOp();
        DataType type = node.getUnaryExpression().accept(this);
        if (op.getTokenType() == NOT) {
            if (type == BOOL) return BOOL;
            errors.add("Line " + op.getLine() + ": Type mismatch for operator '" + NOT + "': " + type);
            return UNKNOWN;
        }
        if (op.getTokenType() == SUB) {
            if (type != DataType.STRING) return type;
            errors.add("Line " + op.getLine() + ": Type mismatch for operator '" + SUB + "': " + type);
            return UNKNOWN;
        }
        return UNKNOWN;
    }

    public DataType visitPostfixNode(PostfixNode node) {
        Token op = node.getOp();
        DataType type = node.getPrimary().accept(this);
        if (op == null) return type;
        if (op.getTokenType() == TRANSPOSE && type == MATRIX) return MATRIX;
        errors.add("Line " + op.getLine() + ": Type mismatch for operator '" + TRANSPOSE + "': " + type);
        return UNKNOWN;
    }

    public DataType visitMatrixNode(MatrixNode node) {
        int rowLength = -1;
        for (List<Node> row : node.getRows()) {
            if (rowLength == -1) rowLength = row.size();
            else if (rowLength != row.size()) {
                errors.add("Matrix rows have different lengths");
                return UNKNOWN;
            }
            for (Node element : row) {
                DataType type = element.accept(this);
                if (type == MATRIX || type == DataType.STRING) {
                    errors.add("Invalid element type in matrix: " + type);
                    return UNKNOWN;
                }
            }
        }
        return MATRIX;
    }

    public DataType visitFlatMatrixNode(FlatMatrixNode node) {
        for (Node element : node.getExpressionList()) {
            DataType type = element.accept(this);
            if (type == MATRIX || type == DataType.STRING) {
                errors.add("Invalid element type in flat matrix: " + type);
                return UNKNOWN;
            }
        }
        return MATRIX;
    }

    public DataType visitCreateMatrixNode(CreateMatrixNode node) {
        List<Node> expressionList = node.getExpressionList();
        Token fun = node.getFun();
        switch (fun.getTokenType()) {
            case EYE:
                if (expressionList.size() == 1) {
                    Node size = expressionList.get(0);
                    DataType type = size.accept(this);
                    if (type == INT || type == ANY) return MATRIX;
                    errors.add("Invalid eye size type: " + type);
                }
            case ZEROS:
            case ONES:
                if (expressionList.size() >= 1) {
                    Node size = expressionList.get(0);
                    DataType type = size.accept(this);
                    if (type == INT || type == ANY) return MATRIX;
                    errors.add("Invalid matrix size type: " + type);
                }
            default:
                return UNKNOWN;
        }
    }

    public DataType visitIfNode(IfNode node) {
        DataType type = node.getCondition().accept(this);
        if (type != BOOL) {
            errors.add("If condition must be BOOL, got: " + type);
        }
        node.getThenNode().accept(this);
        if (node.getElseNode() != null) node.getElseNode().accept(this);
        return null;
    }

    public DataType visitWhileNode(WhileNode node) {
        DataType type = node.getCondition().accept(this);
        if (type != BOOL) {
            errors.add("While condition must be BOOL, got: " + type);
        }
        table = table.pushScope();
        loopDepth++;
        node.getThenNode().accept(this);
        loopDepth--;
        table = table.popScope();
        return null;
    }

    public DataType visitForNode(ForNode node) {
        DataType rangeStartType = node.getRangeStart().accept(this);
        DataType rangeEndType = node.getRangeEnd().accept(this);
        if (rangeStartType == INT && rangeEndType == INT) {
            Token id = node.getId();
            table = table.pushScope();
            table.put(id.getLexeme(), new VariableSymbol(id.getLexeme(), INT));
            loopDepth++;
            node.getInstruction().accept(this);
            loopDepth--;
            table = table.popScope();
        } else if (rangeStartType != ANY || rangeEndType != ANY) {
            errors.add("For loop range values must be INT, got: " + rangeStartType + " and " + rangeEndType);
        }
        return null;
    }

    public DataType visitBreakNode(BreakNode node) {
        if (loopDepth <= 0) {
            errors.add("Break statement outside of loop");
        }
        return null;
    }

    public DataType visitContinueNode(ContinueNode node) {
        if (loopDepth <= 0) {
            errors.add("Continue statement outside of loop");
        }
        return null;
    }

    public DataType visitPrintNode(PrintNode node) {
        List<Node> expressionList = node.getExpressionList();
        for (Node element : expressionList) {
            element.accept(this);
        }
        return null;
    }

    public DataType visitReturnNode(ReturnNode node) {
        Node expression = node.getExpression();
        if (expression != null) {
            expression.accept(this);
        }
        return null;
    }

    public DataType visitBlockNode(BlockNode node) {
        Node instructions = node.getInstructions();
        table = table.pushScope();
        instructions.accept(this);
        table = table.popScope();
        return null;
    }

    public DataType visitProgramNode(ProgramNode node) {
        List<Node> instructions = node.getInstructions();
        for (Node instruction : instructions) {
            instruction.accept(this);
        }
        return null;
    }

    public DataType visitFunctionNode(FunctionNode node) {
        String name = node.getId().getLexeme();
        table.put(name, new VariableSymbol(name, DataType.FUNCTION));
        table = table.pushScope();
        for (Token parameter : node.getParameterList()) {
            table.put(parameter.getLexeme(), new VariableSymbol(parameter.getLexeme(), ANY));
        }
        node.getInstruction().accept(this);
        table = table.popScope();
        return null;
    }

    public DataType visitFunctionCallNode(FunctionCallNode node) {
        Token id = node.getId();
        VariableSymbol symbol = table.get(id.getLexeme());
        if (symbol == null) {
            errors.add("Line " + node.getId().getLine() + ": Undefined function '" + id.getLexeme() + "'");
            return UNKNOWN;
        }
        if (symbol.getType() != DataType.FUNCTION) {
            errors.add("Line " + node.getId().getLine() + ": '" + id.getLexeme() + "' is not a function");
            return UNKNOWN;
        }
        for (Node argument : node.getExpressionList()) {
            argument.accept(this);
        }
        return UNKNOWN;
    }


    public DataType visitExpressionNode(ExpressionNode node) {
        return node.getOrExpression().accept(this);
    }

    public List<String> getErrors() {
        return errors;
    }
}
