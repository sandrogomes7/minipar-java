package compiladores.minipar.lexer;

public class Token {
    public final TokenType type;
    public final String lexeme;
    public final int line;

    public Token(TokenType type, String lexeme, int line) {
        this.type = type;
        this.lexeme = lexeme;
        this.line = line;
    }

    @Override
    public String toString() {
        return "Token{" + type + " '" + lexeme + "' line=" + line + "}";
    }
}

