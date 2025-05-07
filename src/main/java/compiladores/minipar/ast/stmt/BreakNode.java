package compiladores.minipar.ast.stmt;

import compiladores.minipar.interpreter.InterpreterVisitor;
import compiladores.minipar.semantic.ASTVisitor;
import compiladores.minipar.semantic.SemanticException;

public class BreakNode extends StmtNode {
    public BreakNode(int line) { super(line); }

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
        return "break";
    }
}
