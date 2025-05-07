package compiladores.minipar.semantic;

import java.util.Map;

public enum ExprType {
    NUMBER,        // corresponde a literal numérico e expressões aritméticas
    BOOL,          // corresponde a true/false e expressões lógicas/relacionais
    STRING,        // corresponde a literais de texto e concatenação
    VOID,          // para funções que não retornam valor
    ARRAY_NUMBER,  // array de números ([number])
    ARRAY_STRING,  // array de strings ([string])
    ARRAY_BOOL,    // array de booleanos ([bool])
    FUNC,          // tipo para funções
    C_CHANNEL,     // tipo para canais cliente
    S_CHANNEL;     // tipo para canais servidor

    private Map<String, ExprType> properties;
    private Map<String, MethodSignature> methods;

    public MethodSignature getMethodSignature(String methodName) {
        return methods.get(methodName);
    }

    public ExprType getPropertyType(String propertyName) {
        return properties.get(propertyName);
    }


    /** Converte nome de tipo (que vem do token TYPE) para o enum */
    public static ExprType fromString(String typeName) {
        typeName = typeName.strip();

        // Suporte a tipos de array [T]
        if (typeName.startsWith("[") && typeName.endsWith("]")) {
            String inner = typeName.substring(1, typeName.length() - 1);
            return switch (inner.toLowerCase()) {
                case "number" -> ARRAY_NUMBER;
                case "string" -> ARRAY_STRING;
                case "bool" -> ARRAY_BOOL;
                default -> throw new IllegalArgumentException("Tipo de array desconhecido: " + typeName);
            };
        }

        // Tipos simples
        return switch (typeName.toLowerCase()) {
            case "number" -> NUMBER;
            case "bool" -> BOOL;
            case "string" -> STRING;
            case "void" -> VOID;
            case "func" -> FUNC;
            case "c_channel" -> C_CHANNEL;
            case "s_channel" -> S_CHANNEL;
            default -> throw new IllegalArgumentException("Tipo desconhecido: " + typeName);
        };
    }
}