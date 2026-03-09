package mrix;

import mrix.nodes.*;

import static mrix.TokenType.*;
import static mrix.DataType.*;

import java.util.ArrayList;
import java.util.List;

public class TypeChecker implements NodeVisitor {
    private SymbolTable table = new SymbolTable(null);
    private List<String> errors = new ArrayList<String>();

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

    
}
