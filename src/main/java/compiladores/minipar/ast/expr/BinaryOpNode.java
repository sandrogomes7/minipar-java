package compiladores.minipar.ast.expr;

import compiladores.minipar.interpreter.InterpreterVisitor;
import compiladores.minipar.semantic.ASTVisitor;
import compiladores.minipar.semantic.ExprType;
import compiladores.minipar.semantic.SemanticException;

public class BinaryOpNode extends ExprNode {
    private final String op;
    private final ExprNode left, right;
    public BinaryOpNode(String op, ExprNode left, ExprNode right, int line) {
        super(line);
        this.op = op;
        this.left = left;
        this.right = right;
    }

    public String getOp() {
        return op;
    }

    public ExprNode getLeft() {
        return left;
    }

    public ExprNode getRight() {
        return right;
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
        return "(" + left.toString() + " " + op + " " + right.toString() + ")";
    }
}