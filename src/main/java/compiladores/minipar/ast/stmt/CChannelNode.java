package compiladores.minipar.ast.stmt;

import compiladores.minipar.ast.expr.ExprNode;
import compiladores.minipar.interpreter.InterpreterVisitor;
import compiladores.minipar.semantic.ASTVisitor;
import compiladores.minipar.semantic.SemanticException;

public class CChannelNode extends ChannelNode {
    public CChannelNode(String name, ExprNode host, ExprNode port, int line) {
        super(name, host, port, line);
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
        return "channel " + getName() + " " +
               getHost().toString() + ":" + getPort().toString();
    }
}