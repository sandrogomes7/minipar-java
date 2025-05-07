package compiladores.minipar.interpreter;

import compiladores.minipar.ast.core.ProgramNode;
import compiladores.minipar.ast.expr.*;
import compiladores.minipar.ast.stmt.*;
import compiladores.minipar.semantic.SemanticException;

public interface InterpreterVisitor {
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
    Object visit(BinaryOpNode node);
    Object visit(UnaryOpNode node);
    Object visit(LiteralNode node);
    Object visit(IdentifierNode node);
    Object visit(CallNode node);
    Object visit(IndexNode node);
    Object visit(ArrayLiteralNode node);

    void visit(ExpressionStmt node) throws SemanticException;

    void visit(BlockNode node) throws SemanticException;

}