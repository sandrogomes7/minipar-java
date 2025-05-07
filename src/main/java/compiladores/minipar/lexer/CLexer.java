package compiladores.minipar.lexer;

import java.util.HashMap;
import java.util.Map;

public class CLexer implements ILexer {

    private final String source;
    private int current = 0;
    private int line = 1;

    private static final Map<String, TokenType> keywords = new HashMap<>();

    static {
        keywords.put("func", TokenType.FUNC);
        keywords.put("while", TokenType.WHILE);
        keywords.put("if", TokenType.IF);
        keywords.put("for", TokenType.FOR);
        keywords.put("else", TokenType.ELSE);
        keywords.put("return", TokenType.RETURN);
        keywords.put("break", TokenType.BREAK);
        keywords.put("continue", TokenType.CONTINUE);
        keywords.put("par", TokenType.PAR);
        keywords.put("seq", TokenType.SEQ);
        keywords.put("s_channel", TokenType.S_CHANNEL);
        keywords.put("c_channel", TokenType.C_CHANNEL);
        keywords.put("true", TokenType.TRUE);
        keywords.put("false", TokenType.FALSE);
        keywords.put("number", TokenType.TYPE);
        keywords.put("bool", TokenType.TYPE);
        keywords.put("string", TokenType.TYPE);
        keywords.put("void", TokenType.TYPE);
    }

    public CLexer(String source) {
        this.source = source;
    }

    @Override
    public Token nextToken() {
        skipWhitespace();

        if (isAtEnd()) return new Token(TokenType.EOF, "", line);

        char c = advance();

        if (Character.isLetter(c)) return identifier(c);
        if (Character.isDigit(c)) return number(c);
        if (c == '"') return string();

        switch (c) {
            case '+': return new Token(TokenType.PLUS, "+", line);
            case '-':
                if (match('>')) return new Token(TokenType.ARROW, "->", line);
                return new Token(TokenType.MINUS, "-", line);
            case '*': return new Token(TokenType.STAR, "*", line);
            case '/': return new Token(TokenType.SLASH, "/", line);
            case '%': return new Token(TokenType.MOD, "%", line);
            case '=':
                if (match('=')) return new Token(TokenType.EQ, "==", line);
                return new Token(TokenType.ASSIGN, "=", line);
            case '!':
                if (match('=')) return new Token(TokenType.NEQ, "!=", line);
                return new Token(TokenType.NOT, "!", line);
            case '>':
                if (match('=')) return new Token(TokenType.GTE, ">=", line);
                return new Token(TokenType.GT, ">", line);
            case '<':
                if (match('=')) return new Token(TokenType.LTE, "<=", line);
                return new Token(TokenType.LT, "<", line);
            case '(' : return new Token(TokenType.LPAREN, "(", line);
            case ')' : return new Token(TokenType.RPAREN, ")", line);
            case '{' : return new Token(TokenType.LBRACE, "{", line);
            case '}' : return new Token(TokenType.RBRACE, "}", line);
            case '[' : return new Token(TokenType.LBRACKET, "[", line);
            case ']' : return new Token(TokenType.RBRACKET, "]", line);
            case ':': return new Token(TokenType.COLON, ":", line);
            case ',': return new Token(TokenType.COMMA, ",", line);
            case '.': return new Token(TokenType.DOT, ".", line);
            case ';': return new Token(TokenType.SEMICOLON, ";", line);
            case '&':
                if (match('&')) return new Token(TokenType.AND, "&&", line);
                break;
            case '|':
                if (match('|')) return new Token(TokenType.OR, "||", line);
                break;
            default:
                throw new LexicalException("Símbolo inválido '" + c + "' na linha " + line);
        }
        throw new LexicalException("Símbolo inválido no final do switch na linha " + line);
    }

    private void skipWhitespace() {
        while (!isAtEnd()) {
            char c = peek();
            switch (c) {
                case ' ':
                case '\r':
                case '\t':
                    advance();
                    break;
                case '\n':
                    line++;
                    advance();
                    break;
                case '#':
                    // Comentário de linha única
                    while (!isAtEnd() && peek() != '\n') {
                        advance();
                    }
                    break;
                case '/':
                    if (peekNext() == '*') {
                        advance(); // consome '/'
                        advance(); // consome '*'
                        skipBlockComment();
                        break;
                    } else {
                        return; // é operador / (não comentário)
                    }
                default:
                    return;
            }
        }
    }

    private Token identifier(char firstChar) {
        StringBuilder sb = new StringBuilder();
        sb.append(firstChar);

        while (!isAtEnd() && (Character.isLetterOrDigit(peek()) || peek() == '_')) {
            sb.append(advance());
        }

        String lexeme = sb.toString();
        TokenType type = keywords.getOrDefault(lexeme, TokenType.ID);
        return new Token(type, lexeme, line);
    }


    private Token number(char start) {
        StringBuilder sb = new StringBuilder();
        sb.append(start);
        while (!isAtEnd() && Character.isDigit(peek())) {
            sb.append(advance());
        }
        return new Token(TokenType.NUMBER, sb.toString(), line);
    }

    private Token string() {
        StringBuilder sb = new StringBuilder();
        while (!isAtEnd() && peek() != '"') {
            if (peek() == '\n') line++;
            sb.append(advance());
        }
        if (isAtEnd()) {
            throw new LexicalException("Cadeia de caracteres nao fechada na linha " + line);
        }
        advance(); // consumir '"'
        return new Token(TokenType.STRING, sb.toString(), line);
    }

    private boolean match(char expected) {
        if (isAtEnd() || source.charAt(current) != expected) return false;
        current++;
        return true;
    }

    private char advance() {
        return source.charAt(current++);
    }

    private char peek() {
        return isAtEnd() ? '\0' : source.charAt(current);
    }

    private boolean isAtEnd() {
        return current >= source.length();
    }

    private void skipBlockComment() {
        while (!isAtEnd()) {
            if (peek() == '\n') {
                line++;
            }
            if (peek() == '*' && peekNext() == '/') {
                advance(); // consome '*'
                advance(); // consome '/'
                return;
            }
            advance();
        }
        throw new LexicalException("Comentário de bloco não fechado antes do fim do arquivo (linha " + line + ")");
    }

    private char peekNext() {
        return (current + 1 < source.length()) ? source.charAt(current + 1) : '\0';
    }
}
