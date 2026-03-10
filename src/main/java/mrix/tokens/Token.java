package mrix.tokens;

public class Token {
    final TokenType type;
    final String lexeme;
    final Object literal;
    final int line;

    public Token(TokenType type, String lexeme, Object literal, int line) {
        this.type = type;
        this.lexeme = lexeme;
        this.literal = literal;
        this.line = line;
    }

    public TokenType getTokenType() {
        return type;
    }

    public int getLine() {
        return line;
    }

    public String getLexeme() {
        return lexeme;
    }

    @Override
    public String toString() {
        return type + " " + lexeme + " " + literal;
    }
}
