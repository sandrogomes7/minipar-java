package compiladores.minipar.ast.stmt;

import compiladores.minipar.ast.expr.ExprNode;
import compiladores.minipar.interpreter.InterpreterVisitor;
import compiladores.minipar.semantic.ASTVisitor;
import compiladores.minipar.semantic.SemanticException;
import compiladores.minipar.utils.Pair;

import java.util.Map;


public class FuncDefNode extends StmtNode {
    private final String name, returnType;
    private final Map<String, Pair<String, ExprNode>> params;
    private final BlockNode body;
    public FuncDefNode(String name, String returnType,
                       Map<String, Pair<String, ExprNode>> params,
                       BlockNode body, int line) {
        super(line);
        this.name = name;
        this.returnType = returnType;
        this.params = params;
        this.body = body;
    }

    public String getName() {
        return name;
    }

    public String getReturnType() {
        return returnType;
    }

    public Map<String, Pair<String, ExprNode>> getParams() {
        return params;
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
        StringBuilder paramStr = new StringBuilder();
        for (Map.Entry<String, Pair<String, ExprNode>> entry : params.entrySet()) {
            paramStr.append(entry.getKey()).append(": ").append(entry.getValue().getFirst()).append(", ");
        }
        if (paramStr.length() > 0) {
            paramStr.setLength(paramStr.length() - 2); // Remove last comma and space
        }
        return "func " + name + "(" + paramStr.toString() + ") -> " + returnType + " " + body.toString();
    }
}