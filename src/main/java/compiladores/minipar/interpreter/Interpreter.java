package compiladores.minipar.interpreter;

import compiladores.minipar.ast.core.ProgramNode;
import compiladores.minipar.ast.stmt.*;
import compiladores.minipar.ast.expr.*;
import compiladores.minipar.utils.FuncoesEmbutidas;

import java.net.ServerSocket;
import java.net.Socket;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;

public class Interpreter implements InterpreterVisitor {
    private final Deque<Map<String, Object>> env = new ArrayDeque<>();
    private final Map<String, FuncDefNode> functions = new HashMap<>();
    private final Map<String, Socket> channels = new HashMap<>();

    public Interpreter() {
        env.push(new HashMap<>());
    }

    public void run(ProgramNode program) {
        for (StmtNode stmt : program.getStatements()) {
            execute(stmt);
        }
    }

    public void execute(StmtNode stmt) {
        stmt.acceptExecution(this);
    }

    public Object eval(ExprNode expr) {
        return expr.acceptExecution(this);
    }

    private void enterScope() {
        env.push(new HashMap<>());
    }

    private void exitScope() {
        env.pop();
    }

    @Override
    public void visit(ProgramNode node) {
        for (StmtNode stmt : node.getStatements()) {
            execute(stmt);
        }
    }

    @Override
    public void visit(DeclarationNode node) {
        Object val = eval(node.getInit());
        env.peek().put(node.getId(), val);
    }

    @Override
    public void visit(AssignmentNode node) {
        // 1) Avalia o valor a ser atribuído
        Object val = eval(node.getValue());

        ExprNode target = node.getTarget();
        if (target instanceof IdentifierNode idNode) {
            // variável simples: mesma lógica de antes
            String name = idNode.getName();
            for (Map<String, Object> scope : env) {
                if (scope.containsKey(name)) {
                    scope.put(name, val);
                    return;
                }
            }
            throw new RuntimeException("Variável não declarada: " + name);
        }
        else if (target instanceof IndexNode idxNode) {
            // array[index] = val
            // 2.a) resolve o array
            Object arrayObj = eval(idxNode.getTarget());
            if (!(arrayObj instanceof List<?>)) {
                throw new RuntimeException(
                        "Linha " + node.getLine() + ": tentativa de indexar valor não-array"
                );
            }
            @SuppressWarnings("unchecked")
            List<Object> array = (List<Object>) arrayObj;

            // 2.b) resolve o índice
            Object idxVal = eval(idxNode.getIndex());
            if (!(idxVal instanceof Number)) {
                throw new RuntimeException(
                        "Linha " + node.getLine() + ": índice de array não é número"
                );
            }
            int index = ((Number) idxVal).intValue();

            // 2.c) faz o set no array
            if (index < 0 || index >= array.size()) {
                throw new RuntimeException(
                        "Linha " + node.getLine() + ": índice fora dos limites: " + index
                );
            }
            array.set(index, val);
            return;
        }
        else {
            throw new RuntimeException(
                    "Linha " + node.getLine() + ": target inesperado em atribuição: "
                            + target.getClass().getSimpleName()
            );
        }
    }


    @Override
    public void visit(FuncDefNode node) {
        functions.put(node.getName(), node);
    }

    @Override
    public void visit(ReturnNode node) {
        throw new ReturnSignal(eval(node.getExpr()));
    }

    @Override
    public void visit(IfNode node) {
        Object condObj = eval(node.getCond());
        if (!(condObj instanceof Boolean)) {
            throw new RuntimeException("Condição deve ser booleana");
        }
        Boolean cond = (Boolean) condObj;
        if (cond) {
            visitBlock(node.getThenBranch());
        } else if (node.getElseBranch() != null) {
            visitBlock(node.getElseBranch());
        }
    }

    @Override
    public void visit(WhileNode node) {
        while (true) {
            Object condObj = eval(node.getCond());
            if (!(condObj instanceof Boolean)) {
                throw new RuntimeException("Condição deve ser booleana");
            }
            Boolean cond = (Boolean) condObj;
            if (!cond) {
                break;
            }
            visitBlock(node.getBody());
        }
    }

    @Override
    public void visit(ForNode node) {
        execute(node.getInit());
        while (true) {
            Object condObj = eval(node.getCondition());
            if (!(condObj instanceof Boolean)) {
                throw new RuntimeException("Condição deve ser booleana");
            }
            Boolean cond = (Boolean) condObj;
            if (!cond) {
                break;
            }
            visitBlock(node.getBody());
            execute(node.getUpdate());
        }
    }

    @Override
    public void visit(BreakNode node) {
        throw new BreakSignal();
    }

    @Override
    public void visit(ContinueNode node) {
        throw new ContinueSignal();
    }

    @Override
    public void visit(SeqNode node) {
        for (StmtNode stmt : node.getBody().getStmts()) {
            execute(stmt);
        }
    }

    @Override
    public void visit(ParNode node) {
        ExecutorService svc = Executors.newFixedThreadPool(node.getBody().getStmts().size());
        List<Callable<Void>> tasks = new ArrayList<>();
        for (StmtNode stmt : node.getBody().getStmts()) {
            tasks.add(() -> {
                Interpreter e = new Interpreter();
                e.env.addAll(this.env);
                e.functions.putAll(this.functions);
                e.execute(stmt);
                return null;
            });
        }
        try {
            svc.invokeAll(tasks);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            svc.shutdown();
        }
    }

    @Override
    public void visit(SChannelNode node) {
        try {
            int port = ((Number) eval(node.getPort())).intValue();
            String name = node.getName();

            ServerSocket serverSocket = new ServerSocket(port);
            System.out.println("Servidor '" + name + "' escutando na porta " + port);

            // Thread para aceitar conexões sem travar o interpretador
            new Thread(() -> {
                while (true) {
                    try {
                        Socket clientSocket = serverSocket.accept();
                        System.out.println("Conexão aceita de: " + clientSocket.getInetAddress());
                        // Aqui você pode chamar a função 'calc' com os dados recebidos, etc.
                    } catch (IOException e) {
                        e.printStackTrace();
                        break;
                    }
                }
            }).start();

        } catch (IOException ex) {
            throw new RuntimeException("Erro ao iniciar servidor: " + ex.getMessage());
        }
    }


    @Override
    public void visit(CChannelNode node) {
        try {
            String host = (String) eval(node.getHost());
            int port = ((Number) eval(node.getPort())).intValue();
            Socket client = new Socket(host, port);
            channels.put(node.getName(), client);
            env.peek().put(node.getName(), node.getName());
        } catch (IOException ex) {
            throw new RuntimeException(ex.getMessage());
        }
    }

    @Override
    public void visit(ExpressionStmt node) {
        eval(node.getExpr());
    }

    @Override
    public Object visit(BinaryOpNode node) {
        Object left = eval(node.getLeft());
        Object right = eval(node.getRight());
        String op = node.getOp();

        switch (op) {
            case "+":
                if (left instanceof List && right instanceof List) {
                    List<Object> result = new ArrayList<>((List<?>) left);
                    result.addAll((List<?>) right);
                    return result;
                } else if (left instanceof List && !(right instanceof List)) {
                    List<Object> result = new ArrayList<>((List<?>) left);
                    result.add(right);
                    return result;
                } else if (left instanceof Number && right instanceof Number) {
                    return ((Number) left).doubleValue() + ((Number) right).doubleValue();
                }
                break;
            case "-":
                if (left instanceof Number && right instanceof Number) {
                    return ((Number) left).doubleValue() - ((Number) right).doubleValue();
                }
                break;
            case "*":
                if (left instanceof Number && right instanceof Number) {
                    return ((Number) left).doubleValue() * ((Number) right).doubleValue();
                }
                break;
            case "/":
                if (left instanceof Number && right instanceof Number) {
                    return ((Number) left).doubleValue() / ((Number) right).doubleValue();
                }
                break;
            case "==":
                return left != null && left.equals(right);
            case "!=":
                return left == null ? right != null : !left.equals(right);
            case ">":
                if (left instanceof Number && right instanceof Number) {
                    return ((Number) left).doubleValue() > ((Number) right).doubleValue();
                }
                break;
            case "<":
                if (left instanceof Number && right instanceof Number) {
                    return ((Number) left).doubleValue() < ((Number) right).doubleValue();
                }
                break;
            case ">=":
                if (left instanceof Number && right instanceof Number) {
                    return ((Number) left).doubleValue() >= ((Number) right).doubleValue();
                }
                break;
            case "<=":
                if (left instanceof Number && right instanceof Number) {
                    return ((Number) left).doubleValue() <= ((Number) right).doubleValue();
                }
                break;
            case "&&":
                if (left instanceof Boolean && right instanceof Boolean) {
                    return (Boolean) left && (Boolean) right;
                }
                break;
            case "||":
                if (left instanceof Boolean && right instanceof Boolean) {
                    return (Boolean) left || (Boolean) right;
                }
                break;
            case "%":
                if (left instanceof Number && right instanceof Number) {
                    double l = ((Number) left).doubleValue();
                    double r = ((Number) right).doubleValue();
                    return l % r;
                }
                break;
            default:
                throw new RuntimeException("Operador inválido: " + op);
        }
        throw new RuntimeException("Tipos incompatíveis para operação: " + op);
    }

    @Override
    public Object visit(UnaryOpNode node) {
        Object value = eval(node.getExpr());
        switch (node.getOp()) {
            case "-":
                if (value instanceof Number) {
                    return -((Number) value).doubleValue();
                }
                break;
            case "!":
                if (value instanceof Boolean) {
                    return !(Boolean) value;
                }
                break;
            default:
                throw new RuntimeException("Operador inválido: " + node.getOp());
        }
        throw new RuntimeException("Tipo inválido para operação: " + node.getOp());
    }

    @Override
    public Object visit(LiteralNode node) {
        return node.getValue();
    }

    @Override
    public Object visit(IdentifierNode node) {
        for (Map<String, Object> scope : env) {
            if (scope.containsKey(node.getName())) {
                return scope.get(node.getName());
            }
        }
        throw new RuntimeException("Variável não definida: " + node.getName());
    }

    private String formatValue(Object value) {
        if (value instanceof Number) {
            Number num = (Number) value;
            double d = num.doubleValue();
            if (d == Math.floor(d) && !Double.isInfinite(d)) {
                return String.valueOf(num.longValue());
            } else {
                return String.valueOf(d);
            }
        } else if (value instanceof List) {
            List<?> list = (List<?>) value;
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < list.size(); i++) {
                sb.append(formatValue(list.get(i)));
                if (i < list.size() - 1) {
                    sb.append(", ");
                }
            }
            sb.append("]");
            return sb.toString();
        } else if (value instanceof Boolean || value instanceof String) {
            return value.toString();
        } else {
            return String.valueOf(value);
        }
    }

    @Override
    public Object visit(CallNode node) {
        String funcName = node.getFuncName();
        List<ExprNode> args = node.getArgs();

        if (FuncoesEmbutidas.isFuncaoEmbutida(funcName)) {
            if ("print".equals(funcName)) {
                StringBuilder output = new StringBuilder();
                for (int i = 0; i < args.size(); i++) {
                    output.append(formatValue(eval(args.get(i))));
                    if (i < args.size() - 1) {
                        output.append(" ");
                    }
                }
                System.out.println(output.toString());
                return null;
            } else if ("input".equals(funcName)) {
                Scanner scanner = new Scanner(System.in);
                return scanner.nextLine();
            } else if ("sleep".equals(funcName)) {
                try {
                    Thread.sleep(((Number) eval(args.get(0))).longValue());
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                return null;
            } else if ("split".equals(funcName)) {
                Object strObj = eval(args.get(0));
                Object delimObj = eval(args.get(1));
                if (!(strObj instanceof String) || !(delimObj instanceof String)) {
                    throw new RuntimeException("split requer duas strings como argumentos");
                }
                String str = (String) strObj;
                String delim = (String) delimObj;
                String[] parts = str.split(delim);
                return Arrays.asList(parts);
            } else if ("isnum".equals(funcName)) {
                Object strObj = eval(args.get(0));
                if (!(strObj instanceof String)) {
                    throw new RuntimeException("isnum requer uma string como argumento");
                }
                String str = (String) strObj;
                try {
                    Double.parseDouble(str);
                    return true;
                } catch (NumberFormatException e) {
                    return false;
                }
            } else if ("isalpha".equals(funcName)) {
                Object strObj = eval(args.get(0));
                if (!(strObj instanceof String)) {
                    throw new RuntimeException("isalpha requer uma string como argumento");
                }
                String str = (String) strObj;
                return str.chars().allMatch(Character::isLetter);
            } else if ("to_number".equals(funcName)) {
                Object strObj = eval(args.get(0));
                if (!(strObj instanceof String)) {
                    throw new RuntimeException("to_number requer uma string como argumento");
                }
                String str = (String) strObj;
                try {
                    return Double.parseDouble(str);
                } catch (NumberFormatException e) {
                    throw new RuntimeException("Não é possível converter para número: " + str);
                }
            } else if ("len".equals(funcName)) {
                Object obj = eval(args.get(0));
                if (obj instanceof List) {
                    return (double) ((List<?>) obj).size();
                } else if (obj instanceof String) {
                    return (double) ((String) obj).length();
                }
                throw new RuntimeException("len requer uma lista ou string como argumento");
            }
            else if ("send".equals(funcName)) {
                if (args.size() != 2) {
                    throw new RuntimeException("send requer exatamente 2 argumentos");
                }
                Object channelObj = eval(args.get(0));
                Object messageObj = eval(args.get(1));
                if (!(channelObj instanceof String) || !(messageObj instanceof String)) {
                    throw new RuntimeException("send requer c_channel e string como argumentos");
                }
                String channelName = (String) channelObj;
                String message = (String) messageObj;
                Socket socket = channels.get(channelName);
                if (socket == null) {
                    throw new RuntimeException("Canal não encontrado: " + channelName);
                }
                try {
                    PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    out.println(message); // Envia a mensagem
                    return in.readLine(); // Recebe a resposta
                } catch (IOException e) {
                    throw new RuntimeException("Erro ao enviar mensagem: " + e.getMessage());
                }
            } else if ("close".equals(funcName)) {
                if (args.size() != 1) {
                    throw new RuntimeException("close requer exatamente 1 argumento");
                }
                Object channelObj = eval(args.get(0));
                if (!(channelObj instanceof String)) {
                    throw new RuntimeException("close requer c_channel como argumento");
                }
                String channelName = (String) channelObj;
                Socket socket = channels.get(channelName);
                if (socket != null) {
                    try {
                        socket.close();
                        channels.remove(channelName);
                    } catch (IOException e) {
                        throw new RuntimeException("Erro ao fechar canal: " + e.getMessage());
                    }
                }
                return null;
            }
        }

        FuncDefNode func = functions.get(funcName);
        if (func == null) {
            throw new RuntimeException("Função não definida: " + funcName);
        }
        enterScope();
        List<String> params = new ArrayList<>(func.getParams().keySet());
        for (int i = 0; i < params.size(); i++) {
            Object argValue = eval(args.get(i));
            env.peek().put(params.get(i), argValue);
        }
        try {
            visitBlock(func.getBody());
        } catch (ReturnSignal ret) {
            return ret.getValue();
        } finally {
            exitScope();
        }
        return null;
    }

    @Override
    public Object visit(IndexNode node) {
        Object base = eval(node.getTarget());
        Object index = eval(node.getIndex());
        if (base instanceof List) {
            return ((List<?>) base).get(((Number) index).intValue());
        } else if (base instanceof String) {
            return ((String) base).charAt(((Number) index).intValue());
        }
        throw new RuntimeException("Tipo inválido para indexação");
    }

    @Override
    public Object visit(ArrayLiteralNode node) {
        List<Object> elements = new ArrayList<>();
        for (ExprNode elem : node.getElements()) {
            elements.add(eval(elem));
        }
        return elements;
    }

    @Override
    public void visit(BlockNode node) {
        for (StmtNode stmt : node.getStmts()) {
            execute(stmt);
        }
    }

    private void visitBlock(BlockNode block) {
        for (StmtNode stmt : block.getStmts()) {
            execute(stmt);
        }
    }

    private static class BreakSignal extends RuntimeException {}
    private static class ContinueSignal extends RuntimeException {}
    private static class ReturnSignal extends RuntimeException {
        private final Object value;

        public ReturnSignal(Object value) {
            this.value = value;
        }

        public Object getValue() {
            return value;
        }
    }
}