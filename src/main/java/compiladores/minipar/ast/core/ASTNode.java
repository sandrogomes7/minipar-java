package compiladores.minipar.ast.core;

public abstract class ASTNode {
    private final int line;

    protected ASTNode(int line) {
        this.line = line;
    }

    /**
     * @return o número da linha onde este nó foi gerado
     */
    public int getLine() {
        return line;
    }

    @Override
    public String toString() {
        return "ASTNode(line=" + line + ")";
    }
}
