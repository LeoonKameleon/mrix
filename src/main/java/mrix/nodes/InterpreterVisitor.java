package mrix.nodes;

import mrix.interpreter.Value;

public interface InterpreterVisitor {
    Value visitProgramNode(ProgramNode node);
    Value visitAssignNode(AssignNode node);
    Value visitBinaryOpNode(BinaryOpNode node);
    Value visitBlockNode(BlockNode node);
    Value visitBreakNode(BreakNode node);
    Value visitContinueNode(ContinueNode node);
    Value visitCreateMatrixNode(CreateMatrixNode node);
    Value visitExpressionNode(ExpressionNode node);
    Value visitFlatMatrixNode(FlatMatrixNode node);
    Value visitForNode(ForNode node);
    Value visitIterNode(IterNode node);
    Value visitFunctionCallNode(FunctionCallNode node);
    Value visitFunctionNode(FunctionNode node);
    Value visitIfNode(IfNode node);
    Value visitMatrixNode(MatrixNode node);
    Value visitPostfixNode(PostfixNode node);
    Value visitPrimaryNode(PrimaryNode node);
    Value visitPrintNode(PrintNode node);
    Value visitReturnNode(ReturnNode node);
    Value visitUnaryOpNode(UnaryOpNode node);
    Value visitVariableNode(VariableNode node);
    Value visitWhileNode(WhileNode node);
    Value visitImportNode(ImportNode node);
    Value visitTupleNode(TupleNode node);
    Value visitTuplePatternNode(TuplePatternNode node);
}
