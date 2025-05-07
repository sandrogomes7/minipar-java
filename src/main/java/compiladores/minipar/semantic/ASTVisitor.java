package compiladores.minipar.semantic;


import compiladores.minipar.ast.core.*;
import compiladores.minipar.ast.expr.*;
import compiladores.minipar.ast.stmt.*;

import java.util.List;

public interface ASTVisitor {
    // Módulo (programa)
    void visit(ProgramNode node) throws SemanticException;

    // Statements
    void visit(DeclarationNode node);
    void visit(AssignmentNode node);
    void visit(FuncDefNode node);
    void visit(ReturnNode node);
    void visit(IfNode node);
    void visit(WhileNode node);
    void visit(ForNode node);
    void visit(BreakNode node);
    void visit(ContinueNode node);
    void visit(SeqNode node);
    void visit(ParNode node);
    void visit(SChannelNode node);
    void visit(CChannelNode node);

    // Expressões (retornam tipo)
    ExprType visit(BinaryOpNode node);
    ExprType visit(UnaryOpNode node);
    ExprType visit(LiteralNode node);
    ExprType visit(IdentifierNode node);
    ExprType visit(CallNode node);
    ExprType visit(IndexNode node);
    ExprType visit(ArrayLiteralNode node);

    void visit(ExpressionStmt node) throws SemanticException;

    void visit(BlockNode node) throws SemanticException;

    // … outros nós conforme sua AST
}
