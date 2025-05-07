package compiladores.minipar.ast.stmt;

import compiladores.minipar.ast.expr.ExprNode;
import compiladores.minipar.interpreter.InterpreterVisitor;
import compiladores.minipar.semantic.ASTVisitor;
import compiladores.minipar.semantic.SemanticException;

public class DeclarationNode extends StmtNode {
    private final String id, typeName;
    private final ExprNode init;
    public DeclarationNode(String id, String typeName, ExprNode init, int line) {
        super(line);
        this.id = id;
        this.typeName = typeName;
        this.init = init;
    }

    public String getId() {
        return id;
    }

    public String getTypeName() {
        return typeName;
    }

    public ExprNode getInit() {
        return init;
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
        return "var " + id + ": " + typeName + (init != null ? " = " + init.toString() : "");
    }
}
