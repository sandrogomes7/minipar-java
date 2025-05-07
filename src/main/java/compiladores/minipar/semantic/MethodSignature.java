package compiladores.minipar.semantic;

import compiladores.minipar.semantic.ExprType;

import java.util.List;

public class MethodSignature {
    private List<ExprType> parameterTypes;
    private ExprType returnType;

    public MethodSignature(List<ExprType> parameterTypes, ExprType returnType) {
        this.parameterTypes = parameterTypes;
        this.returnType = returnType;
    }

    public List<ExprType> getParameterTypes() {
        return parameterTypes;
    }

    public ExprType getReturnType() {
        return returnType;
    }
}
