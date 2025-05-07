package compiladores.minipar.ast.stmt;

import compiladores.minipar.ast.expr.ExprNode;
import compiladores.minipar.interpreter.InterpreterVisitor;
import compiladores.minipar.semantic.ASTVisitor;
import compiladores.minipar.semantic.SemanticException;

public class SChannelNode extends ChannelNode {
    private final String funcName;
    private final ExprNode description;
    public SChannelNode(String name, String funcName,
                        ExprNode host, ExprNode port,
                        ExprNode description, int line) {
        super(name, host, port, line);
        this.funcName = funcName;
        this.description = description;
    }

    public String getFuncName() {
        return funcName;
    }

    public ExprNode getDescription() {
        return description;
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
        return "channel " + getName() + " " + funcName + " " +
               getHost().toString() + ":" + getPort().toString() + " " +
               description.toString();
    }
}