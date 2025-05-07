package compiladores.minipar.lexer;

public enum TokenType {
    // Palavras-chave
    FUNC, WHILE, IF, FOR, ELSE, RETURN, BREAK, CONTINUE, PAR, SEQ, C_CHANNEL, S_CHANNEL,

    // Tipos
    TYPE,

    // Literais
    STRING, NUMBER, TRUE, FALSE,

    // Operadores
    PLUS, MINUS, STAR, SLASH, MOD,
    ASSIGN, EQ, NEQ, GT, LT, GTE, LTE,
    AND, OR, NOT,

    // Pontuação
    COLON, COMMA, ARROW, DOT,
    LPAREN, RPAREN, LBRACE, RBRACE, LBRACKET, RBRACKET,
    SEMICOLON,

    // Identificadores e especiais
    ID, EOF,

}