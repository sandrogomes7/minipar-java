package compiladores.minipar.ast.expr;

import compiladores.minipar.interpreter.InterpreterVisitor;
import compiladores.minipar.semantic.ASTVisitor;
import compiladores.minipar.semantic.ExprType;
import compiladores.minipar.semantic.SemanticException;

public class IndexNode extends ExprNode {
    private final ExprNode target, index;
    public IndexNode(ExprNode target, ExprNode index, int line) {
        super(line);
        this.target = target;
        this.index = index;
    }

    public ExprNode getTarget() {
        return target;
    }

    public ExprNode getIndex() {
        return index;
    }

    @Override
    public ExprType accept(ASTVisitor visitor) throws SemanticException {
        return visitor.visit(this);
    }

    @Override
    public Object acceptExecution(InterpreterVisitor visitor) {
        return visitor.visit(this);
    }

    @Override
    public String toString() {
        return target.toString() + "[" + index.toString() + "]";
    }
}