package compiladores.minipar.lexer;

public class LexicalException extends RuntimeException {
    public LexicalException(String message) {
        super("Erro Léxico: " + message);
    }
}