package compiladores.minipar.ast.expr;

import compiladores.minipar.interpreter.InterpreterVisitor;
import compiladores.minipar.semantic.ASTVisitor;
import compiladores.minipar.semantic.ExprType;
import compiladores.minipar.semantic.SemanticException;

public class UnaryOpNode extends ExprNode {
    private final String op;
    private final ExprNode expr;
    public UnaryOpNode(String op, ExprNode expr, int line) {
        super(line);
        this.op = op;
        this.expr = expr;
    }

    public String getOp() {
        return op;
    }

    public ExprNode getExpr() {
        return expr;
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
        return op + expr.toString();
    }
}