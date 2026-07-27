package mrix.typing;

import mrix.ast.*;
import mrix.parser.Parser;
import mrix.scanner.Scanner;
import mrix.scanner.token.Token;
import mrix.scanner.token.TokenType;
import mrix.stdlib.StandardLibrary;
import mrix.typing.symbol.SymbolTable;
import mrix.typing.symbol.VariableSymbol;
import mrix.typing.type.DataType;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static mrix.scanner.token.TokenType.*;
import static mrix.typing.type.DataType.*;
import static mrix.typing.type.DataType.HMAP;

public class TypeChecker implements NodeVisitor {
    private SymbolTable table = new SymbolTable(null);
    private final List<String> errors = new ArrayList<>();
    private final StandardLibrary stdlib;
    private int loopDepth;

    public TypeChecker(Path fileDir) {
        stdlib = new StandardLibrary(fileDir);
    }

    @Override
    public DataType visitPrimaryNode(PrimaryNode node) {
        return switch (node.getValue().getTokenType()) {
            case INT_NUM -> INT;
            case FLOAT_NUM -> FLOAT;
            case TokenType.STRING -> DataType.STRING;
            case TRUE, FALSE -> BOOL;
            case TokenType.NONE -> DataType.NONE;
            default -> UNKNOWN;
        };
    }

    @Override
    public DataType visitVariableNode(VariableNode node) {
        VariableSymbol symbol = table.get(node.getId().getLexeme());
        if (symbol == null) {
            errors.add("Line " + node.getId().getLine() + ": Undefined variable '" + node.getId().getLexeme() + "'");
            return UNKNOWN;
        }
        DataType type = symbol.getType();
        List<Node> indices = node.getExpressionList();

        if (indices == null || indices.isEmpty()) {
            return type;
        }

        for (Node indexExpr : indices) {
            DataType indexType = indexExpr.accept(this);
            if (type != HMAP && indexType != INT && indexType != ANY) {
                errors.add("Line " + node.getLine() + ": Index must be INT, but got " + indexType);
            }
        }

        if (type == TUPLE) {
            if (indices.size() > 1) {
                errors.add("Line " + node.getLine() + ": Tuples support only 1D indexing");
            }
            return ANY;
        }

        if (type == HMAP) {
            if (indices.size() > 1) {
                errors.add("Line " + node.getLine() + ": HMaps support only single key indexing");
            }
            return ANY;
        }

        if (type == MATRIX) {
            return FLOAT;
        }

        if (type == ANY) {
            return ANY;
        }

        errors.add("Line " + node.getLine() + ": Type " + type + " does not support indexing");
        return UNKNOWN;
    }

    @Override
    public DataType visitAssignNode(AssignNode node) {
        DataType rightSideType = node.getExpression().accept(this);

        if (node.getVariable() instanceof TuplePatternNode pattern) {
            List<Token> ids = pattern.getIds();

            if (rightSideType != TUPLE && rightSideType != ANY) {
                errors.add("Line " + node.getLine() + ": Cannot unpack non-tuple type: " + rightSideType);
                return UNKNOWN;
            }

            if (node.getExpression() instanceof TupleNode tupleNode) {
                List<Node> elements = tupleNode.getElements();
                if (ids.size() != elements.size()) {
                    errors.add("Line " + node.getLine() + ": Tuple size mismatch. Expected " +
                            elements.size() + ", got " + ids.size());
                }
                for (int i = 0; i < Math.min(ids.size(), elements.size()); i++) {
                    DataType elementType = elements.get(i).accept(this);
                    table.put(ids.get(i).getLexeme(), new VariableSymbol(ids.get(i).getLexeme(), elementType));
                }
            } else {
                for (Token id : ids) {
                    table.put(id.getLexeme(), new VariableSymbol(id.getLexeme(), ANY));
                }
            }
            return rightSideType;
        }

        if (node.getVariable() instanceof VariableNode varNode) {
            String name = varNode.getId().getLexeme();
            List<Node> indices = varNode.getExpressionList();

            if (indices == null || indices.isEmpty()) {
                table.put(name, new VariableSymbol(name, rightSideType));
            } else {
                VariableSymbol symbol = table.get(name);
                if (symbol == null) {
                    errors.add("Line " + node.getLine() + ": Undefined variable '" + name + "'");
                    return UNKNOWN;
                }

                DataType currentType = symbol.getType();

                if (currentType != TUPLE && currentType != MATRIX && currentType != HMAP && currentType != ANY) {
                    errors.add("Line " + node.getLine() + ": Cannot assign to element of type " + currentType);
                }

                for (Node indexExpr : indices) {
                    DataType indexType = indexExpr.accept(this);
                    if (currentType != HMAP && indexType != INT && indexType != ANY) {
                        errors.add("Line " + node.getLine() + ": Index must be INT");
                    }
                }

            }
            return rightSideType;
        }

        if (node.getVariable() instanceof VariableNode variable) {
            String name = variable.getId().getLexeme();
            table.put(name, new VariableSymbol(name, rightSideType));
            return rightSideType;
        }

        return UNKNOWN;
    }

    @Override
    public DataType visitBinaryOpNode(BinaryOpNode node) {
        DataType leftType = node.getLeft().accept(this);
        DataType rightType = node.getRight().accept(this);
        Token op = node.getOp();
        if (leftType == ANY || rightType == ANY) return ANY;
        if (leftType == UNKNOWN || rightType == UNKNOWN) return UNKNOWN;

        TokenType opType = op.getTokenType();
        if (opType != EQ && opType != NOT_EQ) {
            if (leftType == TUPLE || rightType == TUPLE) {
                errors.add("Line " + op.getLine() + ": Operator '" + opType + "' not supported for tuple");
                return UNKNOWN;
            }
        }
        switch (opType) {
            case AND:
            case OR:
                if (leftType == BOOL && rightType == BOOL) return BOOL;
                errors.add("Line " + op.getLine() + ": Type mismatch for operator '" + op.getTokenType() + "': " + leftType + " and " + rightType);
                return UNKNOWN;
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
                return UNKNOWN;
            case ADD:
            case SUB:
                if (leftType == rightType) {
                    if (leftType != BOOL) return leftType;
                }
                if ((leftType == INT && rightType == FLOAT) ||
                        (leftType == FLOAT && rightType == INT)) return FLOAT;
                errors.add("Line " + op.getLine() + ": Type mismatch for operator '" + op.getTokenType() + "': " + leftType + " and " + rightType);
                return UNKNOWN;
            case DIV:
                if (leftType == rightType) {
                    if (leftType == INT) return INT;
                    if (leftType == FLOAT) return FLOAT;
                    if (leftType == MATRIX) return MATRIX;
                }
                if ((leftType == INT && rightType == FLOAT) ||
                        (leftType == FLOAT && rightType == INT)) return FLOAT;
                if (leftType == MATRIX && (rightType == INT || rightType == FLOAT)) return MATRIX;
                errors.add("Line " + op.getLine() + ": Type mismatch for operator '" + DIV + "': " + leftType + " and " + rightType);
                return UNKNOWN;
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
                        (leftType == DataType.STRING && rightType == INT)) return DataType.STRING;
                errors.add("Line " + op.getLine() + ": Type mismatch for operator '" + MUL + "': " + leftType + " and " + rightType);
                return UNKNOWN;
            case MOD:
                if (leftType == rightType) {
                    if (leftType == INT) return INT;
                    if (leftType == FLOAT) return FLOAT;
                }
                if ((leftType == INT || leftType == FLOAT) && (rightType == INT || rightType == FLOAT)) return FLOAT;
                errors.add("Line " + op.getLine() + ": Type mismatch for operator '%': " + leftType + " and " + rightType);
                return UNKNOWN;
            case DOT_ADD:
            case DOT_SUB:
            case DOT_MUL:
            case DOT_DIV:
                if (leftType == MATRIX && rightType == MATRIX) return MATRIX;
                errors.add("Line " + op.getLine() + ": Type mismatch for operator '" + op.getTokenType() + "': " + leftType + " and " + rightType);
                return UNKNOWN;
            default:
                errors.add("Line " + op.getLine() + ": Unknown operator '" + op.getTokenType() + "'");
                return UNKNOWN;
        }
    }

    @Override
    public DataType visitUnaryOpNode(UnaryOpNode node) {
        Token op = node.getOp();
        DataType type = node.getUnaryExpression().accept(this);
        if (op.getTokenType() == NOT) {
            if (type == BOOL || type == DataType.NONE) return BOOL;
            errors.add("Line " + op.getLine() + ": Type mismatch for operator '" + NOT + "': " + type);
            return UNKNOWN;
        }
        if (op.getTokenType() == SUB) {
            if (type == TUPLE) {
                errors.add("Line " + op.getLine() + ": Unary minus not supported for tuple");
                return UNKNOWN;
            }
            if (type != DataType.STRING) return type;
            errors.add("Line " + op.getLine() + ": Type mismatch for operator '" + SUB + "': " + type);
            return UNKNOWN;
        }
        return UNKNOWN;
    }

    @Override
    public DataType visitPostfixNode(PostfixNode node) {
        Token op = node.getOp();
        DataType type = node.getPrimary().accept(this);
        if (op == null) return type;
        if (op.getTokenType() == TRANSPOSE) {
            if (type == MATRIX) return MATRIX;
            if (type == ANY) return ANY;
            errors.add("Line " + op.getLine() + ": Type mismatch for operator '" + TRANSPOSE + "': " + type);
            return UNKNOWN;
        }
        return type;
    }

    @Override
    public DataType visitMatrixNode(MatrixNode node) {
        int rowLength = -1;
        for (List<Node> row : node.getRows()) {
            if (rowLength == -1) rowLength = row.size();
            else if (rowLength != row.size()) {
                errors.add("Line " + node.getLine() + ": Matrix rows have different lengths");
                return UNKNOWN;
            }
            for (Node element : row) {
                DataType type = element.accept(this);
                if (type == MATRIX || type == DataType.STRING) {
                    errors.add("Line " + element.getLine() + "Invalid element type in matrix: " + type);
                    return UNKNOWN;
                }
            }
        }
        return MATRIX;
    }

    @Override
    public DataType visitFlatMatrixNode(FlatMatrixNode node) {
        for (Node element : node.getExpressionList()) {
            DataType type = element.accept(this);
            if (type == MATRIX || type == DataType.STRING) {
                errors.add("Line " + element.getLine() + ": Invalid element type in flat matrix: " + type);
                return UNKNOWN;
            }
        }
        return MATRIX;
    }

    @Override
    public DataType visitCreateMatrixNode(CreateMatrixNode node) {
        List<Node> expressionList = node.getExpressionList();
        Token fun = node.getFun();
        switch (fun.getTokenType()) {
            case EYE:
                if (expressionList.size() == 1) {
                    Node size = expressionList.getFirst();
                    DataType type = size.accept(this);
                    if (type == INT || type == ANY) return MATRIX;
                    errors.add("Line " + size.getLine() + ": Invalid eye size type: " + type);
                }
            case ZEROS:
            case ONES:
                if (!expressionList.isEmpty()) {
                    Node size = expressionList.getFirst();
                    DataType type = size.accept(this);
                    if (type == INT || type == ANY) return MATRIX;
                    errors.add("Line " + size.getLine() + ": Invalid matrix size type: " + type);
                }
            default:
                return UNKNOWN;
        }
    }

    @Override
    public DataType visitIfNode(IfNode node) {
        DataType type = node.getCondition().accept(this);
        if (type != BOOL && type != ANY) {
            errors.add("Line " + node.getLine() + ": If condition must be BOOL, but got: " + type);
        }
        node.getThenNode().accept(this);
        if (node.getElseNode() != null) node.getElseNode().accept(this);
        return null;
    }

    @Override
    public DataType visitWhileNode(WhileNode node) {
        DataType type = node.getCondition().accept(this);
        if (type != BOOL && type != ANY) {
            errors.add("Line " + node.getLine() + ": While condition must be BOOL, but got: " + type);
        }
        table = table.pushScope();
        loopDepth++;
        node.getThenNode().accept(this);
        loopDepth--;
        table = table.popScope();
        return null;
    }

    @Override
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
        } else if (rangeStartType != ANY && rangeEndType != ANY) {
            errors.add("Line " + node.getLine() + ": For loop range values must be INT, but got: " + rangeStartType + " and " + rangeEndType);
        }
        return null;
    }

    @Override
    public DataType visitIterNode(IterNode node) {
        DataType iterableType = node.getIterable().accept(this);
        if (iterableType == TUPLE || iterableType == DataType.STRING
                || iterableType == MATRIX || iterableType == HMAP || iterableType == ANY) {
            table = table.pushScope();

            if (node.getId() instanceof TuplePatternNode pattern) {
                for (Token id : pattern.getIds()) {
                    table.put(id.getLexeme(), new VariableSymbol(id.getLexeme(), ANY));
                }
            } else {
                VariableNode var = (VariableNode) node.getId();
                String name = var.getId().getLexeme();
                table.put(name, new VariableSymbol(name, getElementType(iterableType)));
            }

            loopDepth++;
            node.getInstruction().accept(this);
            loopDepth--;
            table = table.popScope();
        } else {
            errors.add("Line " + node.getLine()
                    + ": Expression is not iterable: " + iterableType);
        }
        return null;
    }

    @Override
    public DataType visitBreakNode(BreakNode node) {
        if (loopDepth <= 0) {
            errors.add("Line " + node.getLine() + ": Break statement outside of loop");
        }
        return null;
    }

    @Override
    public DataType visitContinueNode(ContinueNode node) {
        if (loopDepth <= 0) {
            errors.add("Line " + node.getLine() + ": Continue statement outside of loop");
        }
        return null;
    }

    @Override
    public DataType visitPrintNode(PrintNode node) {
        List<Node> expressionList = node.getExpressionList();
        for (Node element : expressionList) {
            element.accept(this);
        }
        return null;
    }

    @Override
    public DataType visitReturnNode(ReturnNode node) {
        Node expression = node.getExpression();
        if (expression == null) return DataType.NONE;
        return expression.accept(this);
    }

    @Override
    public DataType visitBlockNode(BlockNode node) {
        Node instructions = node.getInstructions();
        table = table.pushScope();
        instructions.accept(this);
        table = table.popScope();
        return null;
    }

    @Override
    public DataType visitProgramNode(ProgramNode node) {
        List<Node> instructions = node.getInstructions();
        for (Node instruction : instructions) {
            instruction.accept(this);
        }
        return null;
    }

    @Override
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

    @Override
    public DataType visitFunctionCallNode(FunctionCallNode node) {
        Token id = node.getId();
        if (stdlib.has(id.getLexeme())) return ANY;
        VariableSymbol symbol = table.get(id.getLexeme());
        if (symbol == null) {
            errors.add("Line " + node.getLine() + ": Undefined function '" + id.getLexeme() + "'");
            return UNKNOWN;
        }
        if (symbol.getType() != DataType.FUNCTION) {
            errors.add("Line " + node.getLine() + ": '" + id.getLexeme() + "' is not a function");
            return UNKNOWN;
        }
        for (Node argument : node.getExpressionList()) {
            argument.accept(this);
        }
        return ANY;
    }

    @Override
    public DataType visitImportNode(ImportNode node) {
        String path = node.getPath().getLiteral().toString();
        try {
            String content = java.nio.file.Files.readString(stdlib.resolvePath(path));
            Scanner scanner = new Scanner(content);
            List<Token> tokens = scanner.tokenize();
            Parser parser = new Parser(tokens);
            Node ast = parser.parseProgram();
            ast.accept(this);
        } catch (java.io.IOException e) {
            errors.add("Line " + node.getLine() + ": Cannot import file: '" + path + "'");
        }
        return null;
    }


    @Override
    public DataType visitExpressionNode(ExpressionNode node) {
        return node.getOrExpression().accept(this);
    }

    @Override
    public DataType visitTupleNode(TupleNode node) {
        for (Node element : node.getElements()) {
            element.accept(this);
        }
        return TUPLE;
    }

    @Override
    public DataType visitTuplePatternNode(TuplePatternNode node) {
        return UNKNOWN;
    }

    @Override
    public DataType visitHMapNode(HMapNode node) {
        for (int i = 0; i < node.getKeys().size(); i++) {
            DataType keyType = node.getKeys().get(i).accept(this);
            if (keyType == MATRIX || keyType == HMAP) {
                errors.add("Line " + node.getLine() + ": Invalid hmap key type: " + keyType);
            }
            node.getValues().get(i).accept(this);
        }
        return HMAP;
    }

    private DataType getElementType(DataType iterableType) {
        return switch (iterableType) {
            case MATRIX -> FLOAT;
            case STRING -> DataType.STRING;
            case TUPLE, ANY -> ANY;
            default -> UNKNOWN;
        };
    }

    public List<String> getErrors() {
        return errors;
    }
}
