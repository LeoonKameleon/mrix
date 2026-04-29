package mrix.nodes;

import mrix.typechecker.DataType;

public interface NodeVisitor {
    DataType visitProgramNode(ProgramNode node);
    DataType visitAssignNode(AssignNode node);
    DataType visitBinaryOpNode(BinaryOpNode node);
    DataType visitBlockNode(BlockNode node);
    DataType visitBreakNode(BreakNode node);
    DataType visitContinueNode(ContinueNode node);
    DataType visitCreateMatrixNode(CreateMatrixNode node);
    DataType visitExpressionNode(ExpressionNode node);
    DataType visitFlatMatrixNode(FlatMatrixNode node);
    DataType visitForNode(ForNode node);
    DataType visitIterNode(IterNode node);
    DataType visitFunctionCallNode(FunctionCallNode node);
    DataType visitFunctionNode(FunctionNode node);
    DataType visitIfNode(IfNode node);
    DataType visitMatrixNode(MatrixNode node);
    DataType visitPostfixNode(PostfixNode node);
    DataType visitPrimaryNode(PrimaryNode node);
    DataType visitPrintNode(PrintNode node);
    DataType visitReturnNode(ReturnNode node);
    DataType visitUnaryOpNode(UnaryOpNode node);
    DataType visitVariableNode(VariableNode node);
    DataType visitWhileNode(WhileNode node);
    DataType visitImportNode(ImportNode node);
    DataType visitTupleNode(TupleNode node);
    DataType visitTuplePatternNode(TuplePatternNode node);
}
