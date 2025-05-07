package compiladores.minipar.parser;

import compiladores.minipar.ast.core.ProgramNode;

public interface IParser {
    ProgramNode parse() throws ParseException;
}
