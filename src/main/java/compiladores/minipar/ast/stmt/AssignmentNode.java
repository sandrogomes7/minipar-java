package compiladores.minipar.ast.stmt;

import compiladores.minipar.ast.expr.ExprNode;
import compiladores.minipar.interpreter.InterpreterVisitor;
import compiladores.minipar.semantic.ASTVisitor;
import compiladores.minipar.semantic.SemanticException;

public class AssignmentNode extends StmtNode {
    private final ExprNode target;       // antes String id
    private final ExprNode value;

    public AssignmentNode(ExprNode target, ExprNode value, int line) {
        super(line);
        this.target = target;
        this.value = value;
    }

    public ExprNode getTarget() {
        return target;
    }

    public ExprNode getValue() {
        return value;
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
        return target.toString() + " = " + value.toString();
    }
}
