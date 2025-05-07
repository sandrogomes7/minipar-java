package compiladores.minipar.ast.core;

import compiladores.minipar.ast.stmt.StmtNode;
import compiladores.minipar.semantic.ASTVisitor;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Raiz da AST: lista de statements
 */
public class ProgramNode extends ASTNode {
    private final List<StmtNode> statements;

    public ProgramNode(List<StmtNode> statements, int line) {
        super(line);
        this.statements = statements;
    }

    /**
     * Retorna a lista de statements do programa
     */
    public List<StmtNode> getStatements() {
        return statements;
    }

    @Override
    public String toString() {
        return statements.stream()
                .map(Object::toString)
                .collect(Collectors.joining("\n"));
    }
}

