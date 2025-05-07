package compiladores.minipar.ast.expr;

import compiladores.minipar.interpreter.InterpreterVisitor;
import compiladores.minipar.semantic.ASTVisitor;
import compiladores.minipar.semantic.ExprType;
import compiladores.minipar.semantic.SemanticException;

public class IdentifierNode extends ExprNode {
    private final String name;
    public IdentifierNode(String name, int line) {
        super(line);
        this.name = name;
    }

    public String getName() {
        return name;
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
        return name;
    }
}
