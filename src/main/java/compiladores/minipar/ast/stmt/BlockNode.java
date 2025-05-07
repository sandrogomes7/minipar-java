package compiladores.minipar.ast.stmt;

import compiladores.minipar.interpreter.InterpreterVisitor;
import compiladores.minipar.semantic.ASTVisitor;
import compiladores.minipar.semantic.SemanticException;

import java.util.List;

public class BlockNode extends StmtNode {
    private final List<StmtNode> stmts;
    public BlockNode(List<StmtNode> stmts, int line) {
        super(line);
        this.stmts = stmts;
    }

    public List<StmtNode> getStmts() {
        return stmts;
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
        sb.append("{\n");
        for (StmtNode stmt : stmts) {
            sb.append(stmt.toString()).append("\n");
        }
        sb.append("}");
        return sb.toString();
    }
}