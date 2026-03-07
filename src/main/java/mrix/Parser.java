package mrix;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Node;

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
        while (!check(EOF)) {
            Node instruction = parseInstruction();
            instructions.add(instruction);
        }
        return new ProgramNode(instructions);
    }

    private Node parseInstruction() {
        if (check(IF)) return parseIfStatement();
        if (check(WHILE)) return parseWhileStatement();
        if (check(FOR)) return parseForStatement();
        if (check(RETURN)) return parseReturnStatement();
        if (check(PRINT)) return parsePrintStatement();
        if (check(FUNCTION)) return parseFunction();
        if (check(LEFT_PAREN)) return parseBlock();
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
