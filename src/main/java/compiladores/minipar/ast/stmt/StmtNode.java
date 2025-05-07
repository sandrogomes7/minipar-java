package compiladores.minipar.ast.stmt;

import compiladores.minipar.ast.core.ASTNode;
import compiladores.minipar.interpreter.InterpreterVisitor;
import compiladores.minipar.semantic.ASTVisitor;
import compiladores.minipar.semantic.SemanticException;

public abstract class StmtNode extends ASTNode {
    protected StmtNode(int line) { super(line); }

    public abstract void accept(ASTVisitor visitor) throws SemanticException;
    public abstract void acceptExecution(InterpreterVisitor visitor);

    @Override
    public String toString() {
        return "stmt";
    }

}

