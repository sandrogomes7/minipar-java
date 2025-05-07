package compiladores.minipar.ast.stmt;

import compiladores.minipar.ast.expr.ExprNode;
import compiladores.minipar.interpreter.InterpreterVisitor;
import compiladores.minipar.semantic.ASTVisitor;
import compiladores.minipar.semantic.SemanticException;

public class IfNode extends StmtNode {
    private final ExprNode cond;
    private final BlockNode thenBranch, elseBranch; // elseBranch pode ser null
    public IfNode(ExprNode cond, BlockNode thenBranch, BlockNode elseBranch, int line) {
        super(line);
        this.cond = cond;
        this.thenBranch = thenBranch;
        this.elseBranch = elseBranch;
    }

    public ExprNode getCond() {
        return cond;
    }

    public BlockNode getThenBranch() {
        return thenBranch;
    }

    public BlockNode getElseBranch() {
        return elseBranch;
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
        StringBuilder sb = new StringBuilder();
        sb.append("if ").append(cond.toString()).append(" ").append(thenBranch.toString());
        if (elseBranch != null) {
            sb.append(" else ").append(elseBranch.toString());
        }
        return sb.toString();
    }
}