package compiladores.minipar.ast.expr;

import compiladores.minipar.interpreter.InterpreterVisitor;
import compiladores.minipar.semantic.ASTVisitor;
import compiladores.minipar.semantic.ExprType;
import compiladores.minipar.semantic.SemanticException;

public class LiteralNode extends ExprNode {
    private final Object value; // Integer, String ou Boolean
    private final ExprType type; // Tipo associado ao literal

    public LiteralNode(Object value, ExprType type, int line) {
        super(line);
        this.value = value;
        this.type = type;
    }

    public Object getValue() {
        return value;
    }

    public ExprType getType() {
        return type;
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
        if (value instanceof String) {
            return "\"" + value + "\"";
        } else if (value instanceof Boolean) {
            return value.toString();
        } else {
            return value.toString();
        }
    }
}