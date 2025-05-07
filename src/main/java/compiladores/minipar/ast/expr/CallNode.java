package compiladores.minipar.ast.expr;

import compiladores.minipar.interpreter.InterpreterVisitor;
import compiladores.minipar.semantic.ASTVisitor;
import compiladores.minipar.semantic.ExprType;
import compiladores.minipar.semantic.SemanticException;

import java.util.List;

public class CallNode extends ExprNode {
    private final String funcName;
    private final List<ExprNode> args;
    public CallNode(String funcName, List<ExprNode> args, int line) {
        super(line);
        this.funcName = funcName;
        this.args = args;
    }

    public String getFuncName() {
        return funcName;
    }

    public List<ExprNode> getArgs() {
        return args;
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
        StringBuilder sb = new StringBuilder();
        sb.append(funcName).append("(");
        for (int i = 0; i < args.size(); i++) {
            sb.append(args.get(i).toString());
            if (i < args.size() - 1) {
                sb.append(", ");
            }
        }
        sb.append(")");
        return sb.toString();
    }
}