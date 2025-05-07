package compiladores.minipar.parser;

import compiladores.minipar.ast.core.ProgramNode;
import compiladores.minipar.ast.expr.*;
import compiladores.minipar.ast.stmt.*;
import compiladores.minipar.lexer.ILexer;
import compiladores.minipar.lexer.Token;
import compiladores.minipar.lexer.TokenType;
import compiladores.minipar.semantic.ExprType;
import compiladores.minipar.utils.Pair;

import java.util.*;


public class CParser implements IParser {
    private final ILexer lexer;
    private Token lookahead;

    public CParser(ILexer lexer) {
        this.lexer = lexer;
        this.lookahead = lexer.nextToken();
    }

    @Override
    public ProgramNode parse() {
        List<StmtNode> stmts = parseStmts();
        consume(TokenType.EOF);
        return new ProgramNode(stmts, lookahead.line);
    }

    private List<StmtNode> parseStmts() {
        List<StmtNode> list = new ArrayList<>();
        while (lookahead.type != TokenType.RBRACE && lookahead.type != TokenType.EOF) {
            list.add(parseStmt());
        }
        return list;
    }

    private StmtNode parseStmt() {
        return switch (lookahead.type) {
            case FUNC -> parseFunction();
            case IF -> parseIf();
            case WHILE -> parseWhile();
            case FOR -> parseFor();
            case SEQ -> parseSeq();
            case PAR -> parsePar();
            case S_CHANNEL -> parseSChannel();
            case C_CHANNEL -> parseCChannel();
            default -> parseSimple();
        };
    }

    private StmtNode parseSimple() {
        // statements curtos: return, break, continue
        if (lookahead.type == TokenType.RETURN) {
            Token t = lookahead; consume(TokenType.RETURN);
            ExprNode expr = parseDisjunction();
            return new ReturnNode(expr, t.line);
        }
        if (lookahead.type == TokenType.BREAK) {
            Token t = lookahead; consume(TokenType.BREAK);
            return new BreakNode(t.line);
        }
        if (lookahead.type == TokenType.CONTINUE) {
            Token t = lookahead; consume(TokenType.CONTINUE);
            return new ContinueNode(t.line);
        }

        // começa com um ID
        Token id = lookahead;
        consume(TokenType.ID);

        // construímos um "l-value" que pode ser um simples IdentifierNode
        // ou um IndexNode encadeado (array[expr])
        ExprNode left = new IdentifierNode(id.lexeme, id.line);
        // enquanto vier '[', empilha mais níveis de indexação
        while (consumeIf(TokenType.LBRACKET)) {
            ExprNode idx = parseDisjunction();
            consume(TokenType.RBRACKET);
            left = new IndexNode(left, idx, id.line);
        }

        // agora decidimos se é declaração, atribuição ou chamada
        if (consumeIf(TokenType.COLON)) {
            // declaração: pode ser array-typed também
            String typeName = parseType();
            consume(TokenType.ASSIGN);
            ExprNode expr = parseDisjunction();
            return new DeclarationNode(id.lexeme, typeName, expr, id.line);
        }
        else if (consumeIf(TokenType.ASSIGN)) {
            // atribuição — AQUI aceitamos left genérico, não só nome simples
            ExprNode expr = parseDisjunction();
            // ASSUMINDO que você ajustou AssignmentNode pra receber ExprNode no lugar de String:
            return new AssignmentNode(left, expr, id.line);
        }
        else if (lookahead.type == TokenType.LPAREN) {
            // chamada de função
            ExprNode call = parseCall(id);
            return new ExpressionStmt(call, id.line);
        }
        else {
            error("Expected ':', '=', '[', or '(' after identifier, found: " + lookahead.lexeme);
            return null;
        }
    }


    private String parseType() {
        if (consumeIf(TokenType.LBRACKET)) {
            String inner = parseType();
            consume(TokenType.RBRACKET);
            return "[" + inner + "]";
        }
        if (lookahead.type == TokenType.TYPE) {
            String t = lookahead.lexeme;
            consume(TokenType.TYPE);
            return t;
        }
        error("Expected type, found: " + lookahead.lexeme);
        return null;
    }

    private ExprNode parseCall(Token idToken) {
        consume(TokenType.LPAREN);
        List<ExprNode> args = new ArrayList<>();
        if (!consumeIf(TokenType.RPAREN)) {
            do { args.add(parseDisjunction()); }
            while (consumeIf(TokenType.COMMA));
            consume(TokenType.RPAREN);
        }
        return new CallNode(idToken.lexeme, args, idToken.line);
    }

    private FuncDefNode parseFunction() {
        consume(TokenType.FUNC);
        String name = lookahead.lexeme;
        consume(TokenType.ID);
        consume(TokenType.LPAREN);
        Map<String, Pair<String, ExprNode>> params = parseParams();
        consume(TokenType.RPAREN);
        consume(TokenType.ARROW);
        String returnType = parseType();
        BlockNode body = parseBlock();
        return new FuncDefNode(name, returnType, params, body, lookahead.line);
    }

    private Map<String, Pair<String, ExprNode>> parseParams() {
        Map<String, Pair<String, ExprNode>> map = new LinkedHashMap<>();
        if (lookahead.type == TokenType.RPAREN) return map;
        do {
            String pname = lookahead.lexeme; consume(TokenType.ID);
            consume(TokenType.COLON);
            String ptype = parseType();
            ExprNode defaultVal = null;
            if (consumeIf(TokenType.ASSIGN)) {
                defaultVal = parseDisjunction();
            }
            map.put(pname, Pair.of(ptype, defaultVal));
        } while (consumeIf(TokenType.COMMA));
        return map;
    }

    private IfNode parseIf() {
        consume(TokenType.IF);
        consume(TokenType.LPAREN);
        ExprNode cond = parseDisjunction();
        consume(TokenType.RPAREN);
        BlockNode thenB = parseBlock();
        BlockNode elseB = null;
        if (consumeIf(TokenType.ELSE)) {
            elseB = parseBlock();
        }
        return new IfNode(cond, thenB, elseB, lookahead.line);
    }

    private WhileNode parseWhile() {
        consume(TokenType.WHILE);
        consume(TokenType.LPAREN);
        ExprNode cond = parseDisjunction();
        consume(TokenType.RPAREN);
        BlockNode body = parseBlock();
        return new WhileNode(cond, body, lookahead.line);
    }

    private ForNode parseFor() {
        consume(TokenType.FOR);
        consume(TokenType.LPAREN);
        StmtNode init = parseSimple();
        consume(TokenType.SEMICOLON);
        ExprNode cond = parseDisjunction();
        consume(TokenType.SEMICOLON);
        AssignmentNode update = (AssignmentNode) parseSimple();
        consume(TokenType.RPAREN);
        BlockNode body = parseBlock();
        return new ForNode(init, cond, update, body, lookahead.line);
    }

    private SeqNode parseSeq() {
        consume(TokenType.SEQ);
        BlockNode body = parseBlock();
        return new SeqNode(body, lookahead.line);
    }

    private ParNode parsePar() {
        consume(TokenType.PAR);
        BlockNode body = parseBlock();
        return new ParNode(body, lookahead.line);
    }

    private SChannelNode parseSChannel() {
        consume(TokenType.S_CHANNEL);
        // nome do canal
        String name = lookahead.lexeme;
        consume(TokenType.ID);

        consume(TokenType.LBRACE);
        // 1) funcName: deve ser um ID (nome da função que vai atender)
        String funcName = lookahead.lexeme;
        consume(TokenType.ID);

        consume(TokenType.COMMA);
        // 2) description: pode ser literal ou variável que contenha string
        ExprNode description = parsePrimary();

        consume(TokenType.COMMA);
        // 3) host: normalmente uma string literal
        ExprNode host = parsePrimary();

        consume(TokenType.COMMA);
        // 4) port: normalmente um número
        ExprNode port = parsePrimary();

        consume(TokenType.RBRACE);

        // Ordem: name, funcName, host, port, description, line
        return new SChannelNode(
                name,
                funcName,
                host,
                port,
                description,
                lookahead.line
        );
    }

    private CChannelNode parseCChannel() {
        consume(TokenType.C_CHANNEL);
        String name = lookahead.lexeme; consume(TokenType.ID);
        consume(TokenType.LBRACE);
        ExprNode host = parsePrimary();
        consume(TokenType.COMMA);
        ExprNode port = parsePrimary();
        consume(TokenType.RBRACE);
        return new CChannelNode(name, host, port, lookahead.line);
    }

    private BlockNode parseBlock() {
        consume(TokenType.LBRACE);
        List<StmtNode> body = parseStmts();
        consume(TokenType.RBRACE);
        return new BlockNode(body, lookahead.line);
    }

    // EXPRESSÕES

    private ExprNode parseDisjunction() {
        ExprNode left = parseConjunction();
        while (consumeIf(TokenType.OR)) {
            ExprNode right = parseConjunction();
            left = new BinaryOpNode("||", left, right, lookahead.line);
        }
        return left;
    }

    private ExprNode parseConjunction() {
        ExprNode left = parseEquality();
        while (consumeIf(TokenType.AND)) {
            ExprNode right = parseEquality();
            left = new BinaryOpNode("&&", left, right, lookahead.line);
        }
        return left;
    }

    private ExprNode parseEquality() {
        ExprNode left = parseComparison();
        while (lookahead.type == TokenType.EQ || lookahead.type == TokenType.NEQ) {
            String op = lookahead.lexeme;
            consume(lookahead.type);
            ExprNode right = parseComparison();
            left = new BinaryOpNode(op, left, right, lookahead.line);
        }
        return left;
    }

    private ExprNode parseComparison() {
        ExprNode left = parseSum();
        while (EnumSet.of(TokenType.GT, TokenType.LT, TokenType.GTE, TokenType.LTE).contains(lookahead.type)) {
            String op = lookahead.lexeme;
            consume(lookahead.type);
            ExprNode right = parseSum();
            left = new BinaryOpNode(op, left, right, lookahead.line);
        }
        return left;
    }

    private ExprNode parseSum() {
        ExprNode left = parseTerm();
        while (lookahead.type == TokenType.PLUS || lookahead.type == TokenType.MINUS) {
            String op = lookahead.lexeme;
            consume(lookahead.type);
            ExprNode right = parseTerm();
            left = new BinaryOpNode(op, left, right, lookahead.line);
        }
        return left;
    }

    private ExprNode parseTerm() {
        ExprNode left = parseUnary();
        while (lookahead.type == TokenType.STAR
                || lookahead.type == TokenType.SLASH
                || lookahead.type == TokenType.MOD) {
            String op = lookahead.lexeme;
            consume(lookahead.type);
            ExprNode right = parseUnary();
            left = new BinaryOpNode(op, left, right, lookahead.line);
        }
        return left;
    }

    private ExprNode parseUnary() {
        // se for NOT ou MINUS, guarda antes de consumir
        if (lookahead.type == TokenType.NOT || lookahead.type == TokenType.MINUS) {
            String op = lookahead.lexeme;           // guarda "-" ou "!"
            int line = lookahead.line;
            consume(lookahead.type);                // só agora avança
            ExprNode expr = parseUnary();
            return new UnaryOpNode(op, expr, line);
        }
        return parsePrimary();
    }


    private ExprNode parsePrimary() {
        Token t = lookahead;
        switch (t.type) {
            case NUMBER:
                // Primeiro, consome a parte inteira
                consume(TokenType.NUMBER);
                String lex = t.lexeme;
                // Se houver um ponto, é um literal float
                if (consumeIf(TokenType.DOT)) {
                    Token frac = lookahead;
                    if (frac.type != TokenType.NUMBER) {
                        error("Expected digits after '.', got: " + frac.lexeme);
                    }
                    consume(TokenType.NUMBER);
                    lex += "." + frac.lexeme;
                }
                return new LiteralNode(Double.parseDouble(lex), ExprType.NUMBER, t.line);
            case STRING:
                consume(TokenType.STRING);
                // Retorna um LiteralNode com o tipo ExprType.STRING
                return new LiteralNode(t.lexeme, ExprType.STRING, t.line);
            case TRUE: case FALSE:
                consume(t.type);
                // Retorna um LiteralNode com o tipo ExprType.BOOL
                return new LiteralNode(Boolean.parseBoolean(t.lexeme), ExprType.BOOL, t.line);
            case ID:
                consume(TokenType.ID);
                // chamada de função ou indexação?
                if (consumeIf(TokenType.LPAREN)) {
                    List<ExprNode> args = new ArrayList<>();
                    if (!consumeIf(TokenType.RPAREN)) {
                        do { args.add(parseDisjunction()); }
                        while (consumeIf(TokenType.COMMA));
                        consume(TokenType.RPAREN);
                    }
                    return new CallNode(t.lexeme, args, t.line);
                }
                if (consumeIf(TokenType.LBRACKET)) {
                    ExprNode idx = parseDisjunction();
                    consume(TokenType.RBRACKET);
                    return new IndexNode(new IdentifierNode(t.lexeme, t.line), idx, t.line);
                }
                return new IdentifierNode(t.lexeme, t.line);
            case LBRACKET:
                // literal de array simples
                consume(TokenType.LBRACKET);
                // array vazio
                if (consumeIf(TokenType.RBRACKET)) {
                    return new ArrayLiteralNode(Collections.emptyList(), t.line);
                }
                // elementos do array
                List<ExprNode> elems = new ArrayList<>();
                elems.add(parseDisjunction());
                while (consumeIf(TokenType.COMMA)) {
                    elems.add(parseDisjunction());
                }
                consume(TokenType.RBRACKET);
                return new ArrayLiteralNode(elems, t.line);

            case LPAREN:
                consume(TokenType.LPAREN);
                ExprNode expr = parseDisjunction();
                consume(TokenType.RPAREN);
                return expr;
            default:
                error("Expected expression, got: " + t.lexeme);
                return null;
        }
    }

    // Auxiliares
    private void consume(TokenType type) {
        if (lookahead.type == type) {
            lookahead = lexer.nextToken();
        } else {
            error("Expected '" + type + "', got '" + lookahead.type + "'");
        }
    }

    private boolean consumeIf(TokenType type) {
        if (lookahead.type == type) {
            lookahead = lexer.nextToken();
            return true;
        }
        return false;
    }

    private void error(String msg) {
        throw new ParseException("Line " + lookahead.line + ": " + msg);
    }
}
