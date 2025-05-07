package compiladores.minipar.ast.expr;

import compiladores.minipar.ast.core.ASTNode;
import compiladores.minipar.interpreter.InterpreterVisitor;
import compiladores.minipar.semantic.ASTVisitor;
import compiladores.minipar.semantic.ExprType;
import compiladores.minipar.semantic.SemanticException;

public abstract class ExprNode extends ASTNode {
    protected ExprNode(int line) { super(line); }

    public abstract ExprType accept(ASTVisitor visitor) throws SemanticException;
    public abstract Object acceptExecution(InterpreterVisitor visitor);

    @Override
    public String toString() {
        return "expr";
    }
}
