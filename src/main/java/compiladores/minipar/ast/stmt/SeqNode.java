package compiladores.minipar.ast.stmt;

import compiladores.minipar.interpreter.InterpreterVisitor;
import compiladores.minipar.semantic.ASTVisitor;
import compiladores.minipar.semantic.SemanticException;

public class SeqNode extends StmtNode {
    private final BlockNode body;
    public SeqNode(BlockNode body, int line) {
        super(line);
        this.body = body;
    }

    public BlockNode getBody() {
        return body;
    }

    @Override
    public void accept(ASTVisitor visitor) throws SemanticException {
        visitor.visit(this);
    }

    @Override
    public void acceptExecution(InterpreterVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return "seq " + body.toString();
    }
}
