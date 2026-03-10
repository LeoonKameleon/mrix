package mrix;

import static mrix.tokens.TokenType.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mrix.tokens.Token;
import mrix.tokens.TokenType;

public class Scanner {
    private List<Token> tokens = new ArrayList<Token>();
    private final String source;
    private int start = 0;
    private int position = 0;
    private int line = 1;
    private static final Map<String, TokenType> keywords = new HashMap<String, TokenType>();
    static {
        keywords.put("if", IF);
        keywords.put("else", ELSE);
        keywords.put("for", FOR);
        keywords.put("while", WHILE);
        keywords.put("break", BREAK);
        keywords.put("return", RETURN);
        keywords.put("and", AND);
        keywords.put("or", OR);
        keywords.put("not", NOT);
        keywords.put("eye", EYE);
        keywords.put("zeros", ZEROS);
        keywords.put("ones", ONES);
        keywords.put("print", PRINT);
        keywords.put("funct", FUNCTION);
        keywords.put("true", TRUE);
        keywords.put("false", FALSE);
    }

    public Scanner(String source) {
        this.source = source;
    }

    public List<Token> tokenize() {
        while (position < source.length()) {
            start = position;
            nextToken();
        }
        tokens.add(new Token(EOF, "", null, line));
        return tokens;
    }
    private void nextToken() {
        char c = source.charAt(position++);
        switch (c) {
            case '\n': line++; break;
            case '(': addToken(LEFT_PAREN); break;
            case ')': addToken(RIGHT_PAREN); break;
            case '{': addToken(LEFT_BRACE); break;
            case '}': addToken(RIGHT_BRACE); break;
            case '[': addToken(LEFT_BRACK); break;
            case ']': addToken(RIGHT_BRACK); break;
            case ':': addToken(COLON); break;
            case ';': addToken(SEMICOLON); break;
            case ',': addToken(COMMA); break;
            case '\'': addToken(TRANSPOSE); break;
            case '=': addToken(check('=') ? EQ : ASSIGN); break;
            case '+': addToken(check('=') ? ADD_ASSIGN : ADD); break;
            case '-': addToken(check('=') ? SUB_ASSIGN : SUB); break;
            case '*': addToken(check('=') ? MUL_ASSIGN : MUL); break;
            case '>': addToken(check('=') ? GREATER_EQ : GREATER); break;
            case '<': addToken(check('=') ? LESS_EQ : LESS); break;
            case '!': addToken(check('=') ? NOT_EQ : NOT); break;
            case '/':
                if (check('/')) {
                    while (peek() != '\n' && !(position >= source.length())) {
                        position++;
                    }
                } else addToken(check('=') ? DIV : DIV_ASSIGN); break;
            case '"': addStringToken(); break;
            case ' ':
            case '\r':
            case '\t': break;
            default:
                if (isDigit(c)) {
                    addNumberToken();
                    break;
                } else if (isAlpha(c)) {
                    addIdToken();
                    break;
                }
                Mrix.error(line, "Unexpected character: " + c); break;
        }
    }

    private void addIdToken() {
        while (isAlphaNumeric(peek()) && !(position >= source.length())) position++;
        String id = source.substring(start, position);
        TokenType type = keywords.get(id);
        if (type == null) addToken(ID);
        else addToken(type);
    }

    private boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }

    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z');
    }

    private void addNumberToken() {
        boolean integer = true;
        while (isDigit(peek()) && !(position >= source.length())) position++;
        if (peek() == '.') {
            integer = false;
            position++;
        }
        while (isDigit(peek()) && !(position >= source.length())) position++;
        if (integer) {
            addToken(INT_NUM, Integer.parseInt(source.substring(start, position)));
        } else {
            addToken(FLOAT_NUM, Float.parseFloat(source.substring(start, position)));
        }
    }

    private void addStringToken() {
        while (peek() != '"' && !(position >= source.length())) {
            if (peek() == '\n') line++;
            position++;
        }
        if (position >= source.length()) {
            Mrix.error(line, "Unexpected end of string");
        }
        position++;
        addToken(STRING, source.substring(start+1, position-1));
    }

    private void addToken(TokenType type) {
        addToken(type, null);
    }
    private void addToken(TokenType type, Object literal) {
        String text = source.substring(start, position);
        tokens.add(new Token(type, text, literal, line));
    }
    private boolean check(char c) {
        if (position >= source.length()) return false;
        if (c != source.charAt(position)) return false;
        position++;
        return true;
    }
    private char peek() {
        if (position >= source.length()) return '\0';
        return source.charAt(position);
    }
    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }
}
