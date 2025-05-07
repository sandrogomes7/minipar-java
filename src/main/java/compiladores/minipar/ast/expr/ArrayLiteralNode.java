package compiladores.minipar.ast.expr;

import compiladores.minipar.interpreter.InterpreterVisitor;
import compiladores.minipar.semantic.ASTVisitor;
import compiladores.minipar.semantic.ExprType;
import compiladores.minipar.semantic.SemanticException;

import java.util.List;

public class ArrayLiteralNode extends ExprNode {
    private final List<ExprNode> elements;

    public ArrayLiteralNode(List<ExprNode> elements, int line) {
        super(line);
        this.elements = elements;
    }

    public List<ExprNode> getElements() {
        return elements;
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
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < elements.size(); i++) {
            sb.append(elements.get(i).toString());
            if (i < elements.size() - 1) sb.append(", ");
        }
        sb.append("]");
        return sb.toString();
    }
}