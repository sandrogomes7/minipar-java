package compiladores.minipar.semantic;

/**
 * Exceção para erros semânticos detectados durante a análise
 */
public class SemanticException extends RuntimeException {
    public SemanticException(String message) {
        super(message);
    }

    public SemanticException(String message, int line) {
        super(message + " (linha: " + line + ")");
    }
}
