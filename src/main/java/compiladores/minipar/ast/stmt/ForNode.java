package compiladores.minipar.ast.stmt;

import compiladores.minipar.ast.expr.ExprNode;
import compiladores.minipar.interpreter.InterpreterVisitor;
import compiladores.minipar.semantic.ASTVisitor;
import compiladores.minipar.semantic.SemanticException;

public class ForNode extends StmtNode {
    private final StmtNode init;          // Ex: i := 0 ou i = 0
    private final ExprNode condition;     // Ex: i < 10
    private final StmtNode update;        // Ex: i = i + 1
    private final BlockNode body;         // Corpo do laço

    public ForNode(StmtNode init, ExprNode condition, StmtNode update, BlockNode body, int line) {
        super(line);
        this.init = init;
        this.condition = condition;
        this.update = update;
        this.body = body;
    }

    public StmtNode getInit() {
        return init;
    }

    public ExprNode getCondition() {
        return condition;
    }

    public StmtNode getUpdate() {
        return update;
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
        return String.format("(for %s; %s; %s %s)", init, condition, update, body);
    }
}
