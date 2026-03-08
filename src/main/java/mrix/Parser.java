package mrix;

import java.util.ArrayList;
import java.util.List;

import mrix.nodes.*;

import static mrix.TokenType.*;

public class Parser {
    private List<Token> tokens;
    private int position;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }
    public Node parseProgram() {
        Node result = parseInstructionsOpt();
        expect(EOF);
        return result;
    }

    private Node parseInstructionsOpt() {
        if (check(EOF)) return null;
        else return parseInstructions();
    }

    private Node parseInstructions() {
        List<Node> instructions = new ArrayList<Node>();
        while (!check(EOF) && !check(RIGHT_BRACE)) {
            Node instruction = parseInstruction();
            instructions.add(instruction);
        }
        return new ProgramNode(instructions);
    }

    private Node parseInstruction() {
        if (check(IF)) return parseIfStatement();
        if (check(WHILE)) return parseWhileStatement();
        if (check(FOR)) return parseForStatement();
        if (check(RETURN)) {
            Node returnNode = parseReturnStatement();
            expect(SEMICOLON);
            return returnNode;
        }
        if (check(PRINT)) {
            Node printNode = parsePrintStatement();
            expect(SEMICOLON);
            return printNode;
        }
        if (check(FUNCTION)) return parseFunction();
        if (check(LEFT_BRACE)) return parseBlock();
        if (check(BREAK)) {
            consume();
            expect(SEMICOLON);
            return new BreakNode();
        }
        if (check(CONTINUE)) {
            consume();
            expect(SEMICOLON);
            return new ContinueNode();
        }
        Node assignNode = parseAssignStatement();
        expect(SEMICOLON);
        return assignNode;
    }

    private Node parseIfStatement() {
        Node instruction2 = null;
        consume();
        expect(LEFT_PAREN);
        Node expression = parseExpression();
        expect(RIGHT_PAREN);
        Node instruction1 = parseInstruction();
        if (check(ELSE)) {
            consume();
            instruction2 = parseInstruction();
        }
        return new IfNode(expression, instruction1, instruction2);
    }

    private Node parseWhileStatement() {
        consume();
        expect(LEFT_PAREN);
        Node expression = parseExpression();
        expect(RIGHT_PAREN);
        Node instruction = parseInstruction();
        return new WhileNode(expression, instruction);
    }

    private Node parseForStatement() {
        consume();
        Token id = consume();
        expect(EQ);
        Node expression1 = parseExpression();
        expect(COLON);
        Node expression2 = parseExpression();
        Node instruction = parseInstruction();
        return new ForNode(expression1, expression2, instruction);
    }

    private Node parseReturnStatement() {
        consume();
        if (check(SEMICOLON)) return new ReturnNode(null);
        Node expression = parseExpression();
        return new ReturnNode(expression);
    }

    private Node parsePrintStatement() {
        consume();
        List<Node> expressionList = parseExpressionList();
        return new PrintNode(expressionList);
    }

    private Node parseFunction() {
        consume();
        Token id = consume();
        expect(LEFT_PAREN);
        List<Token> parameterList = parseParameterList();
        expect(RIGHT_PAREN);
        Node instruction = parseInstruction();
        return new FunctionNode(id, parameterList, instruction);
    }

    private Node parseBlock() {
        consume();
        Node instructions = parseInstructions();
        expect(RIGHT_BRACE);
        return new BlockNode(instructions);
    }

    private Node parseAssignStatement() {
        Node variable = parseVariable();
        Token op = consume();
        if (op.getTokenType() == ASSIGN) return new AssignNode(variable, ASSIGN, parseExpression());
        if (op.getTokenType() == ADD_ASSIGN) return new AssignNode(variable, ADD_ASSIGN, parseExpression());
        if (op.getTokenType() == SUB_ASSIGN) return new AssignNode(variable, SUB_ASSIGN, parseExpression());
        if (op.getTokenType() == MUL_ASSIGN) return new AssignNode(variable, MUL_ASSIGN, parseExpression());
        if (op.getTokenType() == DIV_ASSIGN) return new AssignNode(variable, DIV_ASSIGN, parseExpression());
        throw new RuntimeException("Expected assignment operator");
    }

    private Node parseVariable() {
        Node expressionList = null;
        Token id = consume();
        if (check(LEFT_BRACK)) {
            consume();
            expressionList = parseExpressionList();
            expect(RIGHT_BRACK);
        }
        return new VariableNode(id, expressionList);
    }

    private Node parseExpression() {
        Node orExpression = parseOrExpression();
        return new ExpressionNode(orExpression);
    }

    private Node parseOrExpression() {
        Node left = parseAndExpression();
        while (check(OR)) {
            consume();
            Node right = parseAndExpression();
            left = new BinaryOpNode(left, OR, right);
        }
        return left;
    }

    private Node parseAndExpression() {
        Node left = parseComparisonExpression();
        while (check(AND)) {
            consume();
            Node right = parseComparisonExpression();
            left = new BinaryOpNode(left, AND, right);
        }
        return left;
    }

    private Node parseComparisonExpression() {
        Node left = parseAdditiveExpression();
        while (check(EQ) || check(NOT_EQ) || check(GREATER) || check(LESS) || check(GREATER_EQ) || check(LESS_EQ)) {
            Token op = consume();
            Node right = parseAdditiveExpression();
            left = new BinaryOpNode(left, op.getTokenType(), right);
        }
        return left;
    }

    private Node parseAdditiveExpression() {
        Node left = parseMultiplicativeExpression();
        while (check(ADD) || check(SUB) || check(DOT_ADD) || check(DOT_SUB)) {
            Token op = consume();
            Node right = parseMultiplicativeExpression();
            left = new BinaryOpNode(left, op.getTokenType(), right);
        }
        return left;
    }

    private Node parseMultiplicativeExpression() {
        Node left = parseUnaryExpression();
        while (check(MUL) || check(DIV) || check(DOT_MUL) || check(DOT_DIV)) {
            Token op = consume();
            Node right = parseUnaryExpression();
            left = new BinaryOpNode(left, op.getTokenType(), right);
        }
        return left;
    }

    private Node parseUnaryExpression() {
        if (check(SUB)) {
            consume();
            return new UnaryOpNode(SUB, parseUnaryExpression());
        }
        if (check(NOT)) {
            consume();
            return new UnaryOpNode(NOT, parseUnaryExpression());
        }
        return parsePostfix();
    }

    private Node parsePostfix() {
        Node primary = parsePrimary();
        if (check(TRANSPOSE)) {
            consume();
            return new PostfixNode(primary, TRANSPOSE);
        }
        return new PostfixNode(primary, null);
    }

    private Node parsePrimary() {
        if (check(INT_NUM) || check(FLOAT_NUM) || check(STRING) || check(TRUE) || check(FALSE)) {
            Token type = consume();
            return new PrimaryNode(type);
        }
        if (check(LEFT_PAREN)) {
            consume();
            Node expression = parseExpression();
            expect(RIGHT_PAREN);
            return expression;
        }
        if (check(LEFT_BRACK)) return parseMatrix();
        if (check(EYE) || check(ZEROS) || check(ONES)) return parseCreateMatrix();
        if (check(ID)) return parseVariable();
        throw new RuntimeException("Unexpected token: " + tokens.get(position).getTokenType());
    }

    private List<Node> parseExpressionList() {
        List<Node> expressions = new ArrayList<Node>();
        expressions.add(parseExpression());
        while (check(COMMA)) {
            consume();
            expressions.add(parseExpression());
        }
        return expressions;
    }

    private Node parseMatrix() {
        expect(LEFT_BRACK);
        Node result;
        if (check(LEFT_BRACK)) {
            List<List<Node>> rows = new ArrayList<>();
            while (check(LEFT_BRACK)) {
                rows.add(parseMatrixRow());
            }
            result = new MatrixNode(rows);
        } else {
            result = new MatrixNode(parseExpressionList());
        }
        expect(RIGHT_BRACK);
        return result;
    }

    private List<Node> parseMatrixRow() {
        expect(LEFT_BRACK);
        List<Node> row = parseExpressionList();
        expect(RIGHT_BRACK);
        return row;
    }

    private Node parseCreateMatrix() {
        Token fun = consume();
        expect(LEFT_PAREN);
        List<Node> expressionList = parseExpressionList();
        expect(RIGHT_PAREN);
        return new CreateMatrixNode(fun, expressionList);
    }

    private List<Token> parseParameterList() {
        List<Token> parameters = new ArrayList<Token>();
        if (check(ID)) {
            parameters.add(consume());
        }
        while (check(COMMA)) {
            consume();
            if (check(ID)) {
                parameters.add(consume());
            }
        }
        return parameters;
    }

    private void expect(TokenType type) {
        Token token = tokens.get(position++);
        if (token.getTokenType() != type) {
            throw new RuntimeException("Expected " + type + ", but got " + token.getTokenType());
        }
    }

    private boolean check(TokenType type) {
        Token token = tokens.get(position);
        return token.getTokenType() == type;
    }

    private Token consume() {
        if (position >= tokens.size()) return tokens.get(tokens.size()-1);
        return tokens.get(position++);
    }
}
