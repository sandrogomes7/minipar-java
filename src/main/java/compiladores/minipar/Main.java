package compiladores.minipar;

import compiladores.minipar.interpreter.Interpreter;
import compiladores.minipar.lexer.CLexer;
import compiladores.minipar.lexer.ILexer;
import compiladores.minipar.parser.CParser;
import compiladores.minipar.parser.IParser;
import compiladores.minipar.parser.ParseException;
import compiladores.minipar.ast.core.ProgramNode;
import compiladores.minipar.semantic.SemanticAnalyzer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {
    public static void main(String[] args) throws IOException {
        if (args.length == 0) {
            System.out.println("Uso: java Main <arquivo.minipar>");
            return;
        }

        Path caminho = Paths.get(args[0]);

        if (!Files.exists(caminho)) {
            System.err.println("Arquivo não encontrado: " + caminho);
            return;
        }

        String codigo = Files.readString(caminho);

        try {
            ILexer lexer = new CLexer(codigo);
            IParser parser = new CParser(lexer);
            ProgramNode ast = parser.parse();

            new SemanticAnalyzer().analyze(ast);

            Interpreter exec = new Interpreter();
            exec.run(ast);

        } catch (ParseException e) {
            System.err.println("Erro sintático: " + e.getMessage());
        } catch (RuntimeException e) {
            System.err.println("Erro: " + e.getMessage());
        }
    }
}
