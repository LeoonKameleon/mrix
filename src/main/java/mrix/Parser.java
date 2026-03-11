package mrix;

import static mrix.tokens.TokenType.*;

import java.util.ArrayList;
import java.util.List;

import mrix.nodes.*;
import mrix.tokens.Token;
import mrix.tokens.TokenType;

public class Parser {
    private List<Token> tokens;
    private int size;
    private int position;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
        this.size = tokens.size();
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
        return new ProgramNode(instructions, peek(0).getLine());
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
            Token t = consume();
            expect(SEMICOLON);
            return new BreakNode(t.getLine());
        }
        if (check(CONTINUE)) {
            Token t = consume();
            expect(SEMICOLON);
            return new ContinueNode(t.getLine());
        }
        if (check(ID) && peek(1).getTokenType() == LEFT_PAREN) {
            Node call = parseFunctionCall();
            expect(SEMICOLON);
            return call;
        }
        Node assignNode = parseAssignStatement();
        expect(SEMICOLON);
        return assignNode;
    }

    private Node parseIfStatement() {
        Node instruction2 = null;
        Token t = consume();
        expect(LEFT_PAREN);
        Node expression = parseExpression();
        expect(RIGHT_PAREN);
        Node instruction1 = parseInstruction();
        if (check(ELSE)) {
            consume();
            instruction2 = parseInstruction();
        }
        return new IfNode(expression, instruction1, instruction2, t.getLine());
    }

    private Node parseWhileStatement() {
        Token t = consume();
        expect(LEFT_PAREN);
        Node expression = parseExpression();
        expect(RIGHT_PAREN);
        Node instruction = parseInstruction();
        return new WhileNode(expression, instruction, t.getLine());
    }

    private Node parseForStatement() {
        Token t = consume();
        Token id = consume();
        expect(ASSIGN);
        Node expression1 = parseExpression();
        expect(COLON);
        Node expression2 = parseExpression();
        Node instruction = parseInstruction();
        return new ForNode(id, expression1, expression2, instruction, t.getLine());
    }

    private Node parseReturnStatement() {
        Token t = consume();
        if (check(SEMICOLON)) return new ReturnNode(null, t.getLine());
        Node expression = parseExpression();
        return new ReturnNode(expression, t.getLine());
    }

    private Node parsePrintStatement() {
        Token t = consume();
        List<Node> expressionList = parseExpressionList();
        return new PrintNode(expressionList, t.getLine());
    }

    private Node parseFunction() {
        consume();
        Token id = consume();
        expect(LEFT_PAREN);
        List<Token> parameterList = parseParameterList();
        expect(RIGHT_PAREN);
        Node instruction = parseInstruction();
        return new FunctionNode(id, parameterList, instruction, id.getLine());
    }

    private Node parseBlock() {
        Token t = consume();
        Node instructions = parseInstructions();
        expect(RIGHT_BRACE);
        return new BlockNode(instructions, t.getLine());
    }

    private Node parseFunctionCall() {
        Token id = consume();
        expect(LEFT_PAREN);
        List<Node> expressionList = parseExpressionList();
        expect(RIGHT_PAREN);
        return new FunctionCallNode(id, expressionList, id.getLine());
    }

    private Node parseAssignStatement() {
        Node variable = parseVariable();
        Token op = consume();
        if (op.getTokenType() == ASSIGN) return new AssignNode(variable, op, parseExpression(), op.getLine());
        if (op.getTokenType() == ADD_ASSIGN) return new AssignNode(variable, op, parseExpression(), op.getLine());
        if (op.getTokenType() == SUB_ASSIGN) return new AssignNode(variable, op, parseExpression(), op.getLine());
        if (op.getTokenType() == MUL_ASSIGN) return new AssignNode(variable, op, parseExpression(), op.getLine());
        if (op.getTokenType() == DIV_ASSIGN) return new AssignNode(variable, op, parseExpression(), op.getLine());
        if (op.getTokenType() == MOD_ASSIGN) return new AssignNode(variable, op, parseExpression(), op.getLine());
        throw new RuntimeException("Expected assignment operator");
    }

    private Node parseVariable() {
        List<Node> expressionList = null;
        Token id = consume();
        if (check(LEFT_BRACK)) {
            consume();
            expressionList = parseExpressionList();
            expect(RIGHT_BRACK);
        }
        return new VariableNode(id, expressionList, id.getLine());
    }

    private Node parseExpression() {
        Node orExpression = parseOrExpression();
        return new ExpressionNode(orExpression, orExpression.getLine());
    }

    private Node parseOrExpression() {
        Node left = parseAndExpression();
        while (check(OR)) {
            Token op = consume();
            Node right = parseAndExpression();
            left = new BinaryOpNode(left, op, right, op.getLine());
        }
        return left;
    }

    private Node parseAndExpression() {
        Node left = parseComparisonExpression();
        while (check(AND)) {
            Token op = consume();
            Node right = parseComparisonExpression();
            left = new BinaryOpNode(left, op, right, op.getLine());
        }
        return left;
    }

    private Node parseComparisonExpression() {
        Node left = parseAdditiveExpression();
        while (check(EQ) || check(NOT_EQ) || check(GREATER) || check(LESS) || check(GREATER_EQ) || check(LESS_EQ)) {
            Token op = consume();
            Node right = parseAdditiveExpression();
            left = new BinaryOpNode(left, op, right, op.getLine());
        }
        return left;
    }

    private Node parseAdditiveExpression() {
        Node left = parseMultiplicativeExpression();
        while (check(ADD) || check(SUB) || check(DOT_ADD) || check(DOT_SUB)) {
            Token op = consume();
            Node right = parseMultiplicativeExpression();
            left = new BinaryOpNode(left, op, right, op.getLine());
        }
        return left;
    }

    private Node parseMultiplicativeExpression() {
        Node left = parseUnaryExpression();
        while (check(MUL) || check(DIV) || check(MOD) || check(DOT_MUL) || check(DOT_DIV)) {
            Token op = consume();
            Node right = parseUnaryExpression();
            left = new BinaryOpNode(left, op, right, op.getLine());
        }
        return left;
    }

    private Node parseUnaryExpression() {
        if (check(SUB)) {
            Token op = consume();
            return new UnaryOpNode(op, parseUnaryExpression(), op.getLine());
        }
        if (check(NOT)) {
            Token op = consume();
            return new UnaryOpNode(op, parseUnaryExpression(), op.getLine());
        }
        return parsePostfix();
    }

    private Node parsePostfix() {
        Node primary = parsePrimary();
        if (check(TRANSPOSE)) {
            Token op = consume();
            return new PostfixNode(primary, op, op.getLine());
        }
        return primary;
    }

    private Node parsePrimary() {
        if (check(INT_NUM) || check(FLOAT_NUM) || check(STRING) || check(TRUE) || check(FALSE)) {
            Token value = consume();
            return new PrimaryNode(value, value.getLine());
        }
        if (check(LEFT_PAREN)) {
            consume();
            Node expression = parseExpression();
            expect(RIGHT_PAREN);
            return expression;
        }
        if (check(LEFT_BRACK)) return parseMatrix();
        if (check(EYE) || check(ZEROS) || check(ONES)) return parseCreateMatrix();
        if (check(ID)) {
            if (peek(1).getTokenType() == LEFT_PAREN) return parseFunctionCall();
            return parseVariable();
        }
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
        int line = peek(0).getLine();
        if (check(LEFT_BRACK)) {
            List<List<Node>> rows = new ArrayList<>();
            while (check(LEFT_BRACK)) {
                rows.add(parseMatrixRow());
                if (check(COMMA)) consume();
            }
            result = new MatrixNode(rows, line);
        } else {
            result = new FlatMatrixNode(parseExpressionList(), line);
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
        return new CreateMatrixNode(fun, expressionList, fun.getLine());
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
            throw new RuntimeException("Line " + token.getLine() + ": Expected " + type + ", but got " + token.getTokenType());
        }
    }

    private boolean check(TokenType type) {
        Token token = tokens.get(position);
        return token.getTokenType() == type;
    }

    private Token consume() {
        if (position >= size) return tokens.get(size-1);
        return tokens.get(position++);
    }

    private Token peek(int steps) {
        int index = position + steps;
        if (index >= size) return tokens.get(size-1);
        return tokens.get(index);
    }
}
