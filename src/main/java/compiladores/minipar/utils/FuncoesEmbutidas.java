package compiladores.minipar.utils;

import compiladores.minipar.ast.expr.ExprNode;
import compiladores.minipar.semantic.ASTVisitor;
import compiladores.minipar.semantic.ExprType;
import compiladores.minipar.semantic.SemanticException;

import java.util.*;

public class FuncoesEmbutidas {
    private static final Map<String, ExprType> funcoes = new HashMap<>();

    static {
        funcoes.put("print", ExprType.VOID);
        funcoes.put("input", ExprType.STRING);
        funcoes.put("len", ExprType.NUMBER);
        funcoes.put("to_number", ExprType.NUMBER);
        funcoes.put("to_string", ExprType.STRING);
        funcoes.put("to_bool", ExprType.BOOL);
        funcoes.put("sleep", ExprType.VOID);
        funcoes.put("split", ExprType.ARRAY_STRING);
        funcoes.put("isnum", ExprType.BOOL);
        funcoes.put("isalpha", ExprType.BOOL);
        funcoes.put("send", ExprType.STRING);
        funcoes.put("close", ExprType.VOID);
    }

    public static boolean isFuncaoEmbutida(String nome) {
        return funcoes.containsKey(nome);
    }

    public static ExprType getTipoRetorno(String nome) {
        return funcoes.get(nome);
    }

    public static void validarFuncao(String nome, List<ExprNode> args, ASTVisitor visitor, int linha) throws SemanticException {
        switch (nome) {
            case "print":
                if (args.size() != 1) {
                    throw new SemanticException("Linha " + linha + ": 'print' requer exatamente 1 argumento");
                }
                ExprType printType = args.get(0).accept(visitor);
                if (printType != ExprType.STRING && printType != ExprType.NUMBER &&
                        printType != ExprType.BOOL && printType != ExprType.ARRAY_NUMBER &&
                        printType != ExprType.ARRAY_STRING && printType != ExprType.ARRAY_BOOL) {
                    throw new SemanticException("Linha " + linha + ": 'print' só aceita STRING, NUMBER, BOOL ou arrays");
                }
                break;

            case "input":
                if (!args.isEmpty()) {
                    throw new SemanticException("Linha " + linha + ": 'input' não aceita argumentos");
                }
                break;

            case "len":
                if (args.size() != 1) {
                    throw new SemanticException("Linha " + linha + ": 'len' requer exatamente 1 argumento");
                }
                ExprType lenType = args.get(0).accept(visitor);
                if (lenType != ExprType.STRING && lenType != ExprType.ARRAY_NUMBER &&
                        lenType != ExprType.ARRAY_STRING && lenType != ExprType.ARRAY_BOOL) {
                    throw new SemanticException("Linha " + linha + ": 'len' só aceita STRING ou arrays");
                }
                break;

            case "to_number":
            case "to_string":
            case "to_bool":
                if (args.size() != 1) {
                    throw new SemanticException("Linha " + linha + ": '" + nome + "' requer exatamente 1 argumento");
                }
                args.get(0).accept(visitor); // Aceita qualquer tipo
                break;

            case "sleep":
                if (args.size() != 1) {
                    throw new SemanticException("Linha " + linha + ": 'sleep' requer exatamente 1 argumento");
                }
                ExprType sleepType = args.get(0).accept(visitor);
                if (sleepType != ExprType.NUMBER) {
                    throw new SemanticException("Linha " + linha + ": 'sleep' só aceita NUMBER");
                }
                break;

            case "split":
                if (args.size() != 2) {
                    throw new SemanticException("Linha " + linha + ": 'split' requer exatamente 2 argumentos");
                }
                ExprType strType = args.get(0).accept(visitor);
                ExprType delimType = args.get(1).accept(visitor);
                if (strType != ExprType.STRING || delimType != ExprType.STRING) {
                    throw new SemanticException("Linha " + linha + ": 'split' requer STRING como argumentos");
                }
                break;

            case "isnum":
                if (args.size() != 1) {
                    throw new SemanticException("Linha " + linha + ": 'isnum' requer exatamente 1 argumento");
                }
                ExprType argType = args.get(0).accept(visitor);
                if (argType != ExprType.STRING) {
                    throw new SemanticException("Linha " + linha + ": 'isnum' requer STRING como argumento");
                }
                break;

            default:
                throw new SemanticException("Linha " + linha + ": Função embutida '" + nome + "' não implementada");
        }
    }
}