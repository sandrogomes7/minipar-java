package compiladores.minipar.semantic;

import compiladores.minipar.ast.core.ProgramNode;
import compiladores.minipar.ast.stmt.*;
import compiladores.minipar.ast.expr.*;
import compiladores.minipar.utils.FuncoesEmbutidas;
import compiladores.minipar.utils.Pair;

import java.util.*;

/**
 * Implementação do analisador semântico via Visitor Pattern
 */
public class SemanticAnalyzer implements ASTVisitor {
    private final Map<String, FuncDefNode> functions = new HashMap<>();
    private FuncDefNode currentFunction;

    private final Stack<Map<String, ExprType>> scopes = new Stack<>();

    public SemanticAnalyzer() {
        // Escopo global inicial
        scopes.push(new HashMap<>());
    }

    /**
     * Inicia análise semântica no nível do programa
     */
    public void analyze(ProgramNode program) throws SemanticException {
        // escopo global
        scopes.push(new HashMap<>());
        visit(program);
        scopes.pop();
    }

    @Override
    public void visit(ProgramNode node) throws SemanticException {
        for (StmtNode stmt : node.getStatements()) {
            stmt.accept(this);
        }
    }

    @Override
    public void visit(DeclarationNode node) throws SemanticException {
        ExprType declared = ExprType.fromString(node.getTypeName());
        ExprType initType = node.getInit().accept(this);

        if (!isAssignable(declared, initType)) {
            throw new SemanticException(
                    "Linha " + node.getLine() + ": declaração espera " + declared +
                            ", mas inicializador é " + initType
            );
        }

        Map<String, ExprType> scope = scopes.peek();
        if (scope.containsKey(node.getId())) {
            throw new SemanticException(
                    "Linha " + node.getLine() + ": variável '" + node.getId() + "' já declarada"
            );
        }

        scope.put(node.getId(), declared);
    }


    @Override
    public void visit(AssignmentNode node) throws SemanticException {
        // 1) Descobrir o tipo do alvo de atribuição
        ExprNode target = node.getTarget();
        ExprType targetType;

        if (target instanceof IdentifierNode idNode) {
            // variáveis simples
            targetType = lookupVar(idNode.getName(), node.getLine());
        }
        else if (target instanceof IndexNode idxNode) {
            // 2.a) Valida que o lado esquerdo é mesmo um array
            ExprType arrayType = idxNode.getTarget().accept(this);
            if (arrayType != ExprType.ARRAY_NUMBER
                    && arrayType != ExprType.ARRAY_STRING
                    && arrayType != ExprType.ARRAY_BOOL) {
                throw new SemanticException(
                        "Linha " + node.getLine() +
                                ": tentando indexar algo que não é array (tipo " + arrayType + ")"
                );
            }

            // 2.b) Valida que o índice é número
            ExprType indexType = idxNode.getIndex().accept(this);
            if (indexType != ExprType.NUMBER) {
                throw new SemanticException(
                        "Linha " + node.getLine() +
                                ": índice de array deve ser NUMBER, veio " + indexType
                );
            }

            // 2.c) Extrai o tipo do elemento
            targetType = switch (arrayType) {
                case ARRAY_NUMBER -> ExprType.NUMBER;
                case ARRAY_STRING -> ExprType.STRING;
                case ARRAY_BOOL   -> ExprType.BOOL;
                default -> throw new IllegalStateException("impossível: " + arrayType);
            };
        }
        else {
            throw new SemanticException(
                    "Linha " + node.getLine() +
                            ": não pode atribuir a expressão de tipo " + target.getClass().getSimpleName()
            );
        }

        // 3) Verificar a expressão à direita
        ExprType exprType = node.getValue().accept(this);

        // 4) Verificar se é atribuível
        if (!isAssignable(targetType, exprType)) {
            throw new SemanticException(
                    "Linha " + node.getLine() +
                            ": não pode atribuir " + exprType + " a alvo de tipo " + targetType
            );
        }
    }


    // Novo método auxiliar para verificar compatibilidade
    private boolean isAssignable(ExprType varType, ExprType exprType) {
        if (varType == exprType) return true;
        // Permitir atribuição de array vazio a qualquer tipo de array
        if (varType == ExprType.ARRAY_NUMBER || varType == ExprType.ARRAY_STRING || varType == ExprType.ARRAY_BOOL) {
            return exprType == ExprType.ARRAY_NUMBER || exprType == ExprType.ARRAY_STRING || exprType == ExprType.ARRAY_BOOL;
        }
        return false;
    }

    @Override
    public void visit(FuncDefNode node) throws SemanticException {
        if (functions.containsKey(node.getName())) {
            throw new SemanticException(
                    "Linha " + node.getLine() + ": função '" + node.getName() + "' já declarada"
            );
        }
        functions.put(node.getName(), node);
        FuncDefNode prev = currentFunction;
        currentFunction = node;

        // novo escopo para parâmetros e corpo
        scopes.push(new HashMap<>());
        for (Map.Entry<String, Pair<String, ExprNode>> param : node.getParams().entrySet()) {
            ExprType ptype = ExprType.fromString(param.getValue().getFirst());
            scopes.peek().put(param.getKey(), ptype);
        }
        node.getBody().accept(this);
        scopes.pop();
        currentFunction = prev;
    }

    @Override
    public void visit(ReturnNode node) throws SemanticException {
        if (currentFunction == null) {
            throw new SemanticException("Linha " + node.getLine() + ": 'return' fora de função");
        }
        ExprType exprType = node.getExpr().accept(this);
        ExprType expected = ExprType.fromString(currentFunction.getReturnType());
        if (exprType != expected) {
            throw new SemanticException(
                    "Linha " + node.getLine() + ": retorno é " + exprType +
                            ", esperado " + expected
            );
        }
    }

    @Override
    public void visit(IfNode node) throws SemanticException {
        ExprType cond = node.getCond().accept(this);
        if (cond != ExprType.BOOL) {
            throw new SemanticException(
                    "Linha " + node.getLine() + ": condição de 'if' deve ser BOOL"
            );
        }
        node.getThenBranch().accept(this);
        if (node.getElseBranch() != null) {
            node.getElseBranch().accept(this);
        }
    }

    @Override
    public void visit(WhileNode node) throws SemanticException {
        ExprType cond = node.getCond().accept(this);
        if (cond != ExprType.BOOL) {
            throw new SemanticException(
                    "Linha " + node.getLine() + ": condição de 'while' deve ser BOOL"
            );
        }
        node.getBody().accept(this);
    }

    @Override
    public void visit(ForNode node) throws SemanticException {
        node.getInit().accept(this);
        ExprType cond = node.getCondition().accept(this);
        if (cond != ExprType.BOOL) {
            throw new SemanticException(
                    "Linha " + node.getLine() + ": condição de 'for' deve ser BOOL"
            );
        }
        node.getUpdate().accept(this);
        node.getBody().accept(this);
    }

    @Override
    public void visit(BreakNode node) throws SemanticException {
        // opcional: checar contexto de loop
    }

    @Override
    public void visit(ContinueNode node) throws SemanticException {
        // opcional: checar contexto de loop
    }

    @Override
    public void visit(SeqNode node) throws SemanticException {
        node.getBody().accept(this);
    }

    @Override
    public void visit(ParNode node) throws SemanticException {
        // Novo escopo para o bloco PAR
        scopes.push(new HashMap<>());

        for (StmtNode s : node.getBody().getStmts()) {
            // Verifica instruções que não fazem sentido em PAR
            if (s instanceof BreakNode) {
                throw new SemanticException(
                        "Linha " + node.getLine() + ": 'break' não é permitido em bloco 'par'"
                );
            }
            if (s instanceof ContinueNode) {
                throw new SemanticException(
                        "Linha " + node.getLine() + ": 'continue' não é permitido em bloco 'par'"
                );
            }
            if (s instanceof ReturnNode) {
                throw new SemanticException(
                        "Linha " + node.getLine() + ": 'return' não é permitido diretamente em bloco 'par'"
                );
            }

            // Processa a instrução normalmente
            s.accept(this);
        }

        // Sai do escopo
        scopes.pop();
    }

    @Override
    public void visit(SChannelNode node) throws SemanticException {
        ExprType host = node.getHost().accept(this);
        if (host != ExprType.STRING) {
            throw new SemanticException(
                    "Linha " + node.getLine() + ": localhost deve ser STRING"
            );
        }
        ExprType port = node.getPort().accept(this);
        if (port != ExprType.NUMBER) {
            throw new SemanticException(
                    "Linha " + node.getLine() + ": port deve ser NUMBER"
            );
        }
        FuncDefNode f = functions.get(node.getFuncName());
        if (f == null || !f.getReturnType().equalsIgnoreCase("string")) {
            throw new SemanticException(
                    "Linha " + node.getLine() + ": função base deve retornar STRING"
            );
        }
    }

    @Override
    public void visit(CChannelNode node) throws SemanticException {
        ExprType host = node.getHost().accept(this);
        if (host != ExprType.STRING) {
            throw new SemanticException(
                    "Linha " + node.getLine() + ": localhost deve ser STRING"
            );
        }
        ExprType port = node.getPort().accept(this);
        if (port != ExprType.NUMBER) {
            throw new SemanticException(
                    "Linha " + node.getLine() + ": port deve ser NUMBER"
            );
        }
        // Registrar o nome do canal como uma variável do tipo STRING
        Map<String, ExprType> scope = scopes.peek();
        if (scope.containsKey(node.getName())) {
            throw new SemanticException(
                    "Linha " + node.getLine() + ": canal '" + node.getName() + "' já declarado"
            );
        }
        scope.put(node.getName(), ExprType.STRING);
    }

    @Override
    public ExprType visit(BinaryOpNode node) throws SemanticException {
        ExprType lt = node.getLeft().accept(this);
        ExprType rt = node.getRight().accept(this);
        String op = node.getOp();
        switch (op) {
            case "||": case "&&":
                if (lt != ExprType.BOOL || rt != ExprType.BOOL)
                    throw new SemanticException("Linha " + node.getLine() + ": lógica requer BOOL");
                return ExprType.BOOL;
            case "==": case "!=":
                if (lt != rt)
                    throw new SemanticException("Linha " + node.getLine() + ": ==/!= requerem tipos iguais");
                return ExprType.BOOL;
            case ">": case "<": case ">=": case "<=":
                if (lt != ExprType.NUMBER || rt != ExprType.NUMBER)
                    throw new SemanticException("Linha " + node.getLine() + ": comparação requer NUMBER");
                return ExprType.BOOL;
            case "+":
                if (lt == ExprType.NUMBER && rt == ExprType.NUMBER) return ExprType.NUMBER;
                if (lt == ExprType.STRING && rt == ExprType.STRING) return ExprType.STRING; // Permitir concatenação de strings
                if (lt == ExprType.ARRAY_NUMBER && rt == ExprType.ARRAY_NUMBER) return ExprType.ARRAY_NUMBER;
                if (lt == ExprType.ARRAY_STRING && rt == ExprType.ARRAY_STRING) return ExprType.ARRAY_STRING;
                if (lt == ExprType.ARRAY_BOOL && rt == ExprType.ARRAY_BOOL) return ExprType.ARRAY_BOOL;
                throw new SemanticException("Linha " + node.getLine() + ": + requer NUMBER, STRING ou arrays compatíveis");
            default:
                if (lt != ExprType.NUMBER || rt != ExprType.NUMBER)
                    throw new SemanticException("Linha " + node.getLine() + ": aritmética requer NUMBER");
                return ExprType.NUMBER;
        }
    }

    @Override
    public ExprType visit(UnaryOpNode node) throws SemanticException {
        ExprType t = node.getExpr().accept(this); // Avalia o tipo da expressão
        String op = node.getOp();

        if ("!".equals(op)) {
            if (t != ExprType.BOOL) {
                throw new SemanticException("Linha " + node.getLine() + ": ! requer BOOL");
            }
            return ExprType.BOOL; // Retorna o tipo semântico BOOL
        } else if ("-".equals(op)) {
            if (t != ExprType.NUMBER) {
                throw new SemanticException("Linha " + node.getLine() + ": - requer NUMBER");
            }
            return ExprType.NUMBER; // Retorna o tipo semântico NUMBER
        } else {
            throw new SemanticException("Linha " + node.getLine() + ": operador unário desconhecido '" + op + "'");
        }
    }

    @Override
    public ExprType visit(LiteralNode node) {
        Object v = node.getValue();
        if (v instanceof Number) return ExprType.NUMBER;
        if (v instanceof Boolean) return ExprType.BOOL;
        if (v instanceof String) return ExprType.STRING;
        throw new SemanticException("Linha " + node.getLine() + ": literal tipo desconhecido");
    }

    @Override
    public ExprType visit(IdentifierNode node) throws SemanticException {
        return lookupVar(node.getName(), node.getLine());
    }

    @Override
    public ExprType visit(CallNode node) throws SemanticException {
        String funcName = node.getFuncName();
        List<ExprNode> args = node.getArgs();

        // Verifica se é função embutida (ex: print, input)
        if (FuncoesEmbutidas.isFuncaoEmbutida(funcName)) {
            ExprType returnType = FuncoesEmbutidas.getTipoRetorno(funcName);

            // Validações específicas para cada função embutida
            switch (funcName) {
                case "print":
                    if (args.isEmpty()) {
                        throw new SemanticException("Linha " + node.getLine() + ": 'print' requer pelo menos 1 argumento");
                    }
                    for (ExprNode arg : args) {
                        ExprType printType = arg.accept(this);
                        if (printType != ExprType.STRING && printType != ExprType.NUMBER &&
                                printType != ExprType.BOOL && printType != ExprType.ARRAY_NUMBER &&
                                printType != ExprType.ARRAY_STRING && printType != ExprType.ARRAY_BOOL) {
                            throw new SemanticException("Linha " + node.getLine() + ": 'print' só aceita STRING, NUMBER, BOOL ou arrays");
                        }
                    }
                    break;

                case "input":
                    if (!args.isEmpty()) {
                        throw new SemanticException("Linha " + node.getLine() + ": 'input' não aceita argumentos");
                    }
                    break;

                case "len":
                    if (args.size() != 1) {
                        throw new SemanticException("Linha " + node.getLine() + ": 'len' requer exatamente 1 argumento");
                    }
                    ExprType lenType = args.get(0).accept(this);
                    if (lenType != ExprType.STRING && lenType != ExprType.ARRAY_NUMBER &&
                            lenType != ExprType.ARRAY_STRING && lenType != ExprType.ARRAY_BOOL) {
                        throw new SemanticException("Linha " + node.getLine() + ": 'len' só aceita STRING ou arrays");
                    }
                    break;

                case "to_number":
                case "to_string":
                case "to_bool":
                    if (args.size() != 1) {
                        throw new SemanticException("Linha " + node.getLine() + ": '" + funcName + "' requer exatamente 1 argumento");
                    }
                    args.get(0).accept(this); // Aceita qualquer tipo
                    break;

                case "sleep":
                    if (args.size() != 1) {
                        throw new SemanticException("Linha " + node.getLine() + ": 'sleep' requer exatamente 1 argumento");
                    }
                    ExprType sleepType = args.get(0).accept(this);
                    if (sleepType != ExprType.NUMBER) {
                        throw new SemanticException("Linha " + node.getLine() + ": 'sleep' só aceita NUMBER");
                    }
                    break;

                case "split":
                    if (args.size() != 2) {
                        throw new SemanticException("Linha " + node.getLine() + ": 'split' requer exatamente 2 argumentos");
                    }
                    ExprType strType = args.get(0).accept(this);
                    ExprType delimType = args.get(1).accept(this);
                    if (strType != ExprType.STRING || delimType != ExprType.STRING) {
                        throw new SemanticException("Linha " + node.getLine() + ": 'split' requer STRING como argumentos");
                    }
                    break;

                case "isnum":
                    if (args.size() != 1) {
                        throw new SemanticException("Linha " + node.getLine() + ": 'isnum' requer exatamente 1 argumento");
                    }
                    ExprType argType = args.get(0).accept(this);
                    if (argType != ExprType.STRING) {
                        throw new SemanticException("Linha " + node.getLine() + ": 'isnum' requer STRING como argumento");
                    }
                    break;
                case "isalpha":
                    if (args.size() != 1) {
                        throw new SemanticException("Linha " + node.getLine() + ": 'isalpha' requer exatamente 1 argumento");
                    }
                    ExprType isAlphaType = args.get(0).accept(this);
                    if (isAlphaType != ExprType.STRING) {
                        throw new SemanticException("Linha " + node.getLine() + ": 'isalpha' requer STRING como argumento");
                    }
                    break;
                case "send":
                    if (args.size() != 2) {
                        throw new SemanticException("Linha " + node.getLine() + ": 'send' requer exatamente 2 argumentos");
                    }
                    ExprType channelType = args.get(0).accept(this);
                    ExprType messageType = args.get(1).accept(this);
                    // Supondo que c_channel seja representado como STRING ou outro tipo específico
                    if (channelType != ExprType.STRING && messageType != ExprType.STRING) {
                        throw new SemanticException("Linha " + node.getLine() + ": 'send' requer c_channel e STRING como argumentos");
                    }
                    break;
                case "close":
                    if (args.size() != 1) {
                        throw new SemanticException("Linha " + node.getLine() + ": 'close' requer exatamente 1 argumento");
                    }
                    ExprType closeChannelType = args.get(0).accept(this);
                    if (closeChannelType != ExprType.STRING) {
                        throw new SemanticException("Linha " + node.getLine() + ": 'close' requer c_channel como argumento");
                    }
                    break;
                default:
                    throw new SemanticException("Linha " + node.getLine() + ": Função embutida '" + funcName + "' não implementada");
            }

            return returnType;
        }

        // Funções declaradas pelo usuário
        FuncDefNode fn = functions.get(funcName);
        if (fn == null) {
            throw new SemanticException("Linha " + node.getLine() + ": função '" + funcName + "' não declarada");
        }

        List<Pair<String, String>> params = fn.getParams().entrySet().stream()
                .map(entry -> new Pair<>(entry.getKey(), entry.getValue().getFirst()))
                .toList();

        // Calcula parâmetros obrigatórios (sem valor padrão)
        int requiredParams = 0;
        for (Pair<String, String> param : params) {
            requiredParams++;
        }

        if (args.size() < requiredParams) {
            throw new SemanticException("Linha " + node.getLine() + ": faltam argumentos para '" + funcName + "'");
        }

        if (args.size() > params.size()) {
            throw new SemanticException("Linha " + node.getLine() + ": excesso de argumentos para '" + funcName + "'");
        }

        // Verificação de tipos
        for (int i = 0; i < args.size(); i++) {
            ExprType argType = args.get(i).accept(this); // Corrigido: usa args.get(i)
            ExprType paramType = ExprType.fromString(params.get(i).getSecond());
            if (!isAssignable(paramType, argType)) {
                throw new SemanticException("Linha " + node.getLine() + ": tipo inválido para argumento " + (i+1) + ": esperado " + paramType + ", recebido " + argType);
            }
        }

        return ExprType.fromString(fn.getReturnType());
    }

    @Override
    public ExprType visit(IndexNode node) throws SemanticException {
        ExprType base = node.getTarget().accept(this);
        ExprType idx = node.getIndex().accept(this);

        // Valida o índice
        if (idx != ExprType.NUMBER) {
            throw new SemanticException("Linha " + node.getLine() + ": índice deve ser NUMBER");
        }

        // Valida o tipo base e retorna o tipo do elemento
        return switch (base) {
            case STRING -> ExprType.STRING; // Indexação em string retorna um caractere (STRING)
            case ARRAY_NUMBER -> ExprType.NUMBER; // Indexação em [number] retorna NUMBER
            case ARRAY_STRING -> ExprType.STRING; // Indexação em [string] retorna STRING
            case ARRAY_BOOL -> ExprType.BOOL; // Indexação em [bool] retorna BOOL
            default -> throw new SemanticException("Linha " + node.getLine() + ": indexação só em STRING ou arrays");
        };
    }

    @Override
    public void visit(ExpressionStmt node) throws SemanticException {
        node.getExpr().accept(this);
    }

    @Override
    public void visit(BlockNode node) throws SemanticException {
        // Entra em novo escopo
        scopes.push(new HashMap<>());

        for (StmtNode stmt : node.getStmts()) {
            stmt.accept(this);
        }

        // Sai do escopo
        scopes.pop();
    }

    @Override
    public ExprType visit(ArrayLiteralNode node) throws SemanticException {
        List<ExprNode> elems = node.getElements();
        if (elems.isEmpty()) {
            // Array vazio pode ser atribuído a qualquer tipo de array
            return ExprType.ARRAY_NUMBER; // Padrão, pode ser ajustado conforme contexto
        }

        ExprType firstType = elems.get(0).accept(this);
        for (int i = 1; i < elems.size(); i++) {
            ExprType elemType = elems.get(i).accept(this);
            if (firstType != elemType) {
                throw new SemanticException("Linha " + node.getLine() +
                        ": elementos do array têm tipos diferentes: " +
                        firstType + " vs " + elemType);
            }
        }

        return switch (firstType) {
            case NUMBER -> ExprType.ARRAY_NUMBER;
            case STRING -> ExprType.ARRAY_STRING;
            case BOOL -> ExprType.ARRAY_BOOL;
            default -> throw new SemanticException("Linha " + node.getLine() + ": tipo de array inválido: " + firstType);
        };
    }

    // *** Auxiliar ***
    private ExprType lookupVar(String name, int line) throws SemanticException {
        for (Map<String, ExprType> scope : scopes) {
            if (scope.containsKey(name)) return scope.get(name);
        }
        throw new SemanticException("Linha " + line + ": variável '" + name + "' não declarada");
    }
}
