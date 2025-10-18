package decaf;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class Parse {
    public Parse(Scan scan) {
    }   
    public void parse() {
    }
    public void printErrors() {}
    public void printWarnings() {}
}

class Token {
    PrimitiveType type;
    String value;
}

interface ParseNode {
    public List<ParseNode> getChildren();
    public boolean isTerminal();
    public String toString();
}


class Program implements ParseNode {
    class ProgramParseException extends Exception {
        public ProgramParseException(String message) {
            super(message);
        }
    }
    private List<ImportDeclaration> importDeclarations;
    private List<FieldDeclaration> fieldDeclarations;
    private List<MethodDeclaration> methodDeclarations;

    public Program(List<Token> tokens) throws ProgramParseException{
    }
    public List<ParseNode> getChildren() {
        return null;
    }
    public boolean isTerminal() {
        return false;
    }
    public String toString() {
        return "Program{" +
                "importDeclarations=" + importDeclarations.stream().map(ImportDeclaration::toString).collect(Collectors.joining(", ")) +
                ", fieldDeclarations=" + fieldDeclarations.stream().map(FieldDeclaration::toString).collect(Collectors.joining(", ")) +
                ", methodDeclarations=" + methodDeclarations.stream().map(MethodDeclaration::toString).collect(Collectors.joining(", ")) +
                '}';
    }
}

class ImportDeclaration implements ParseNode {
    class ImportDeclarationParseException extends Exception {
        public ImportDeclarationParseException(String message) {
            super(message);
        }
    }
    private String identifier;
    public ImportDeclaration(List<Token> tokens, Integer start) throws ImportDeclarationParseException {
    }
    public List<ParseNode> getChildren() {
        return null;
    }
    public boolean isTerminal() {
        return false;
    }
    public String toString() {
        return "ImportDeclaration{" +
                "identifier='" + identifier + '\'' +
                '}';
    }
}

class FieldDeclaration implements ParseNode {
    class FieldDeclarationParseException extends Exception {
        public FieldDeclarationParseException(String message) {
            super(message);
        }
    }
    private PrimitiveType type;
    private String identifier;
    public FieldDeclaration(List<Token> tokens, Integer start) throws FieldDeclarationParseException {
    }
    public List<ParseNode> getChildren() {
        return null;
    }
    public boolean isTerminal() {
        return false;
    }
    public String toString() {
        return "FieldDeclaration{" +
                "type='" + type + '\'' +
                ", identifier='" + identifier + '\'' +
                '}';
    }
}

class MethodDeclaration implements ParseNode {
    class MethodDeclarationParseException extends Exception {
        public MethodDeclarationParseException(String message) {
            super(message);
        }
    }
    private ReturnType returnType;
    private String methodName;
    private List<Parameter> parameters;
    private Block block;
    public MethodDeclaration(List<Token> tokens, Integer start) throws MethodDeclarationParseException {
    }
    public List<ParseNode> getChildren() {
        return null;
    }
    public boolean isTerminal() {
        return false;
    }
    public String toString() {
        return "MethodDeclaration{" +
                "returnType='" + returnType + '\'' +
                ", methodName='" + methodName + '\'' +
                '}';
    }
}

class ReturnType implements ParseNode {
    class ReturnTypeParseException extends Exception {
        public ReturnTypeParseException(String message) {
            super(message);
        }
    }
    private PrimitiveType primitiveType;
    private boolean isVoid;
    public ReturnType(List<Token> tokens, Integer start) throws ReturnTypeParseException {
    }
    public List<ParseNode> getChildren() {
        return null;
    }
    public boolean isTerminal() {
        return false;
    }
    public String toString() {
        if (isVoid) {
            return "ReturnType{void}";
        } else {
            return "ReturnType{" +
                    "primitiveType='" + primitiveType + '\'' +
                    '}';
        }
    }
}

class Parameter implements ParseNode {
    class ParameterParseException extends Exception {
        public ParameterParseException(String message) {
            super(message);
        }
    }
    private PrimitiveType type;
    private String identifier;
    public Parameter(List<Token> tokens, Integer start) throws ParameterParseException {
    }
    public List<ParseNode> getChildren() {
        return null;
    }
    public boolean isTerminal() {
        return false;
    }
    public String toString() {
        return "Parameter{" +
                "type='" + type + '\'' +
                ", identifier='" + identifier + '\'' +
                '}';
    }
}

class Block implements ParseNode {
    class BlockParseException extends Exception {
        public BlockParseException(String message) {
            super(message);
        }
    }
    private List<FieldDeclaration> fieldDeclarations;
    private List<ParseNode> statements;
    public Block(List<Token> tokens, Integer start) throws BlockParseException {
    }
    public List<ParseNode> getChildren() {
        return null;
    }
    public boolean isTerminal() {
        return false;
    }
    public String toString() {
        return "Block{" +
                "fieldDeclarations=" + fieldDeclarations.stream().map(FieldDeclaration::toString).collect(Collectors.joining(", ")) +
                ", statements=" + statements.stream().map(ParseNode::toString).collect(Collectors.joining(", ")) +
                '}';
    }
}

class PrimitiveType implements ParseNode {
    class PrimitiveTypeParseException extends Exception {
        public PrimitiveTypeParseException(String message) {
            super(message);
        }
    }
    private String typeName;
    public PrimitiveType(List<Token> tokens, Integer start) throws PrimitiveTypeParseException {
    }
    public List<ParseNode> getChildren() {
        return null;
    }
    public boolean isTerminal() {
        return false;
    }
    public String toString() {
        return "PrimitiveType{" +
                "typeName='" + typeName + '\'' +
                '}';
    }
}

interface Statement extends ParseNode {
    class StatementParseException extends Exception {
        public StatementParseException(String message) {
            super(message);
        }
    }
}

class AssignmentStatement implements Statement {
    class AssignmentStatementParseException extends Exception {
        public AssignmentStatementParseException(String message) {
            super(message);
        }
    }
    private Location location;
    private AssignmentExpression assignmentExpression;
    public AssignmentStatement(List<Token> tokens, Integer start) throws AssignmentStatementParseException {
    }
    public List<ParseNode> getChildren() {
        return null;
    }
    public boolean isTerminal() {
        return false;
    }
    public String toString() {
        return "AssignmentStatement{" +
                "location=" + location +
                ", assignmentExpression=" + assignmentExpression +
                '}';
    }
}

class MethodCallStatement implements Statement {
    class MethodCallStatementParseException extends Exception {
        public MethodCallStatementParseException(String message) {
            super(message);
        }
    }
    private MethodCall methodCall;
    public MethodCallStatement(List<Token> tokens, Integer start) throws MethodCallStatementParseException {
    }
    public List<ParseNode> getChildren() {
        return null;
    }
    public boolean isTerminal() {
        return false;
    }
    public String toString() {
        return "MethodCallStatement{" +
                "methodCall=" + methodCall +
                '}';
    }
}

class BranchStatement implements Statement {
    class BranchStatementParseException extends Exception {
        public BranchStatementParseException(String message) {
            super(message);
        }
    }
    private String condition;
    private Block thenBlock;
    private Block elseBlock;
    public BranchStatement(List<Token> tokens, Integer start) throws BranchStatementParseException {
    }
    public List<ParseNode> getChildren() {
        return null;
    }
    public boolean isTerminal() {
        return false;
    }
    public String toString() {
        return "BranchStatement{" +
                "condition='" + condition + '\'' +
                '}';
    }
}

class ForLoopStatement implements Statement {
    class ForLoopStatementParseException extends Exception {
        public ForLoopStatementParseException(String message) {
            super(message);
        }
    }
    private String initialization;
    private String condition;
    private String update;
    private Block body;
    public ForLoopStatement(List<Token> tokens, Integer start) throws ForLoopStatementParseException {
    }
    public List<ParseNode> getChildren() {
        return null;
    }
    public boolean isTerminal() {
        return false;
    }
    public String toString() {
        return "ForLoopStatement{" +
                "initialization='" + initialization + '\'' +
                ", condition='" + condition + '\'' +
                ", update='" + update + '\'' +
                '}';
    }
}

class WhileLoopStatement implements Statement {
    class WhileLoopStatementParseException extends Exception {
        public WhileLoopStatementParseException(String message) {
            super(message);
        }
    }
    private String condition;
    private Block body;
    public WhileLoopStatement(List<Token> tokens, Integer start) throws WhileLoopStatementParseException {
    }
    public List<ParseNode> getChildren() {
        return null;
    }
    public boolean isTerminal() {
        return false;
    }
    public String toString() {
        return "WhileLoopStatement{" +
                "condition='" + condition + '\'' +
                '}';
    }
}

class ReturnStatement implements Statement {
    class ReturnStatementParseException extends Exception {
        public ReturnStatementParseException(String message) {
            super(message);
        }
    }
    private String returnValue;
    public ReturnStatement(List<Token> tokens, Integer start) throws ReturnStatementParseException {
    }
    public List<ParseNode> getChildren() {
        return null;
    }
    public boolean isTerminal() {
        return false;
    }
    public String toString() {
        return "ReturnStatement{" +
                "returnValue='" + returnValue + '\'' +
                '}';
    }
}

class BreakStatement implements Statement {
    class BreakStatementParseException extends Exception {
        public BreakStatementParseException(String message) {
            super(message);
        }
    }
    public BreakStatement(List<Token> tokens, Integer start) throws BreakStatementParseException {
    }
    public List<ParseNode> getChildren() {
        return null;
    }
    public boolean isTerminal() {
        return false;
    }
    public String toString() {
        return "BreakStatement{}";
    }
}

class ContinueStatement implements Statement {
    class ContinueStatementParseException extends Exception {
        public ContinueStatementParseException(String message) {
            super(message);
        }
    }
    public ContinueStatement(List<Token> tokens, Integer start) throws ContinueStatementParseException {
    }
    public List<ParseNode> getChildren() {
        return null;
    }
    public boolean isTerminal() {
        return false;
    }
    public String toString() {
        return "ContinueStatement{}";
    }
}

class ForUpdate implements ParseNode {
    class ForUpdateParseException extends Exception {
        public ForUpdateParseException(String message) {
            super(message);
        }
    }
    private Location location;
    private AssignmentExpression assignmentExpression;
    public ForUpdate(List<Token> tokens, Integer start) throws ForUpdateParseException {
    }
    public List<ParseNode> getChildren() {
        return null;
    }
    public boolean isTerminal() {
        return false;
    }
    public String toString() {
        return "ForUpdate{" +
                "location=" + location +
                ", assignmentExpression=" + assignmentExpression +
                '}';
    }
}

class AssignmentExpression implements ParseNode {
    class AssignmentExpressionParseException extends Exception {
        public AssignmentExpressionParseException(String message) {
            super(message);
        }
    }
    private Optional<IncrementOrDecrement> incrementOrDecrement;
    private Optional<AssignmentOp> assignmentOp;
    private Optional<Expression> expression;
    private boolean isIncrementOrDecrement;
    public AssignmentExpression(List<Token> tokens, Integer start) throws AssignmentExpressionParseException {
    }
    public List<ParseNode> getChildren() {
        return null;
    }
    public boolean isTerminal() {
        return false;
    }
    public String toString() {
        if (isIncrementOrDecrement) {
            return "AssignmentExpression{" +
                    "incrementOrDecrement=" + incrementOrDecrement +
                    '}';
        } else {
            return "AssignmentExpression{" +
                    "assignmentOp=" + assignmentOp +
                    ", expression=" + expression +
                    '}';
        }
    }
}

class AssignmentOp implements ParseNode {
    class AssignmentOpParseException extends Exception {
        public AssignmentOpParseException(String message) {
            super(message);
        }
    }
    private String operator;
    public AssignmentOp(List<Token> tokens, Integer start) throws AssignmentOpParseException {
    }
    public List<ParseNode> getChildren() {
        return null;
    }
    public boolean isTerminal() {
        return true;
    }
    public String toString() {
        return "AssignmentOp{" +
                "operator='" + operator + '\'' +
                '}';
    }
}

class IncrementOrDecrement implements ParseNode {
    class IncrementOrDecrementParseException extends Exception {
        public IncrementOrDecrementParseException(String message) {
            super(message);
        }
    }
    private String operator;
    public IncrementOrDecrement(List<Token> tokens, Integer start) throws IncrementOrDecrementParseException {
    }
    public List<ParseNode> getChildren() {
        return null;
    }
    public boolean isTerminal() {
        return true;
    }
    public String toString() {
        return "IncrementOrDecrement{" +
                "operator='" + operator + '\'' +
                '}';
    }
}

class MethodCall implements Statement {
    class MethodCallParseException extends Exception {
        public MethodCallParseException(String message) {
            super(message);
        }
    }
    private MethodName methodName;
    private List<MethodArgument> methodArguments;
    public MethodCall(List<Token> tokens, Integer start) throws MethodCallParseException {
    }
    public List<ParseNode> getChildren() {
        return null;
    }
    public boolean isTerminal() {
        return false;
    }
    public String toString() {
        return "MethodCall{" +
                "methodName='" + methodName + '\'' +
                '}';
    }
}

class MethodName implements ParseNode {
    class MethodNameParseException extends Exception {
        public MethodNameParseException(String message) {
            super(message);
        }
    }
    private String name;
    public MethodName(List<Token> tokens, Integer start) throws MethodNameParseException {
    }
    public List<ParseNode> getChildren() {
        return null;
    }
    public boolean isTerminal() {
        return false;
    }
    public String toString() {
        return "MethodName{" +
                "name='" + name + '\'' +
                '}';
    }
}

class Identifier implements ParseNode {
    class IdentifierParseException extends Exception {
        public IdentifierParseException(String message) {
            super(message);
        }
    }
    private Alpha alpha;
    private List<AlphaNumeric> alphaNumerics;
    public Identifier(List<Token> tokens, Integer start) throws IdentifierParseException {
    }
    public List<ParseNode> getChildren() {
        return null;
    }
    public boolean isTerminal() {
        return false;
    }
    public String toString() {
        return "Identifier{" +
                alpha + alphaNumerics +
                '}';
    }
}

class MethodArgument implements ParseNode {
    class MethodArgumentParseException extends Exception {
        public MethodArgumentParseException(String message) {
            super(message);
        }
    }
    private Expression expression;
    private StringLiteral stringLiteral;
    private boolean isExpression;
    public MethodArgument(List<Token> tokens, Integer start) throws MethodArgumentParseException {
    }
    public List<ParseNode> getChildren() {
        return null;
    }
    public boolean isTerminal() {
        return false;
    }
    public String toString() {
        return "MethodArgument{" +
                "expression=" + expression +
                '}';
    }
}

abstract class AbstractLocation implements ParseNode {
    class LocationParseException extends Exception {
        public LocationParseException(String message) {
            super(message);
        }
    }
}

class Location extends AbstractLocation {
    class LocationParseException extends Exception {
        public LocationParseException(String message) {
            super(message);
        }
    }
    private Identifier identifier;
    public Location(List<Token> tokens, Integer start) throws LocationParseException {
    }
    public List<ParseNode> getChildren() {
        return null;
    }
    public boolean isTerminal() {
        return false;
    }
    public String toString() {
        return "Location{" +
                "identifier='" + identifier + '\'' +
                '}';
    }
}

interface Expression extends ParseNode {
    class ExpressionParseException extends Exception {
        public ExpressionParseException(String message) {
            super(message);
        }
    }
}
class LocationExpression implements Expression {
    class LocationExpressionParseException extends Exception {
        public LocationExpressionParseException(String message) {
            super(message);
        }
    }
    private Location location;
    public LocationExpression(List<Token> tokens, Integer start) throws LocationExpressionParseException {
    }
    public List<ParseNode> getChildren() {
        return null;
    }
    public boolean isTerminal() {
        return false;
    }
    public String toString() {
        return "LocationExpression{" +
                "location=" + location +
                '}';
    }
}

interface Literal extends Statement {
    class LiteralParseException extends Exception {
        public LiteralParseException(String message) {
            super(message);
        }
    }
}

class StringLiteral implements Literal {
    class StringLiteralParseException extends Exception {
        public StringLiteralParseException(String message) {
            super(message);
        }
    }
    private List<CharLiteral> charLiterals;
    public StringLiteral(List<Token> tokens, Integer start) throws StringLiteralParseException {
    }
    public List<ParseNode> getChildren() {
        return null;
    }
    public boolean isTerminal() {
        return true;
    }
    public String toString() {
        return "StringLiteral{" +
                "charLiterals=" + charLiterals +
                '}';
    }
}
interface IntegerLiteral extends Literal {
    class IntegerLiteralParseException extends Exception {
        public IntegerLiteralParseException(String message) {
            super(message);
        }
    }
}

class DecimalLiteral implements IntegerLiteral {
    class DecimalLiteralParseException extends Exception {
        public DecimalLiteralParseException(String message) {
            super(message);
        }
    }
    private Integer value;
    public DecimalLiteral(List<Token> tokens, Integer start) throws DecimalLiteralParseException {
    }
    public List<ParseNode> getChildren() {
        return null;
    }
    public boolean isTerminal() {
        return true;
    }
    public String toString() {
        return "DecimalLiteral{" +
                "value=" + value +
                '}';
    }
}

class HexLiteral implements IntegerLiteral {
    class HexLiteralParseException extends Exception {
        public HexLiteralParseException(String message) {
            super(message);
        }
    }
    private Integer value;
    public HexLiteral(List<Token> tokens, Integer start) throws HexLiteralParseException {
    }
    public List<ParseNode> getChildren() {
        return null;
    }
    public boolean isTerminal() {
        return true;
    }
    public String toString() {
        return "HexLiteral{" +
                "value=" + value +
                '}';
    }
}

class LongLiteral implements Literal {
    class LongLiteralParseException extends Exception {
        public LongLiteralParseException(String message) {
            super(message);
        }
    }
    private IntegerLiteral value;
    public LongLiteral(List<Token> tokens, Integer start) throws LongLiteralParseException {
    }
    public List<ParseNode> getChildren() {
        return null;
    }
    public boolean isTerminal() {
        return true;
    }
    public String toString() {
        return "LongLiteral{" +
                "value=" + value +
                '}';
    }
}

class BooleanLiteral implements Literal {
    class BooleanLiteralParseException extends Exception {
        public BooleanLiteralParseException(String message) {
            super(message);
        }
    }
    private Boolean value;
    public BooleanLiteral(List<Token> tokens, Integer start) throws BooleanLiteralParseException {
    }
    public List<ParseNode> getChildren() {
        return null;
    }
    public boolean isTerminal() {
        return true;
    }
    public String toString() {
        return "BooleanLiteral{" +
                "value=" + value +
                '}';
    }
}

interface Operator extends ParseNode {
    class OperatorParseException extends Exception {
        public OperatorParseException(String message) {
            super(message);
        }
    }
}

class BinaryOperator implements Operator {
    class BinaryOperatorParseException extends Exception {
        public BinaryOperatorParseException(String message) {
            super(message);
        }
    }
    private String operator;
    public BinaryOperator(List<Token> tokens, Integer start) throws BinaryOperatorParseException {
    }
    public List<ParseNode> getChildren() {
        return null;
    }
    public boolean isTerminal() {
        return true;
    }
    public String toString() {
        return "BinaryOperator{" +
                "operator='" + operator + '\'' +
                '}';
    }
}

class RightAssociativeUnaryOperator implements Operator {
    class RightAssociativeUnaryOperatorParseException extends Exception {
        public RightAssociativeUnaryOperatorParseException(String message) {
            super(message);
        }
    }
    private String operator;
    public RightAssociativeUnaryOperator(List<Token> tokens, Integer start) throws RightAssociativeUnaryOperatorParseException {
    }
    public List<ParseNode> getChildren() {
        return null;
    }
    public boolean isTerminal() {
        return true;
    }
    public String toString() {
        return "RightAssociativeUnaryOperator{" +
                "operator='" + operator + '\'' +
                '}';
    }
}

class Alpha implements ParseNode {
    class AlphaParseException extends Exception {
        public AlphaParseException(String message) {
            super(message);
        }
    }
    private Character character;
    public Alpha(List<Token> tokens, Integer start) throws AlphaParseException {
    }
    public List<ParseNode> getChildren() {
        return null;
    }
    public boolean isTerminal() {
        return true;
    }
    public String toString() {
        return "Alpha{" +
                "character=" + character +
                '}';
    }
}

class AlphaNumeric implements ParseNode {
    class AlphaNumericParseException extends Exception {
        public AlphaNumericParseException(String message) {
            super(message);
        }
    }
    private Character character;
    public AlphaNumeric(List<Token> tokens, Integer start) throws AlphaNumericParseException {
    }
    public List<ParseNode> getChildren() {
        return null;
    }
    public boolean isTerminal() {
        return true;
    }
    public String toString() {
        return "AlphaNumeric{" +
                "character=" + character +
                '}';
    }
}

class Digit implements ParseNode {
    class DigitParseException extends Exception {
        public DigitParseException(String message) {
            super(message);
        }
    }
    private Character character;
    public Digit(List<Token> tokens, Integer start) throws DigitParseException {
    }
    public List<ParseNode> getChildren() {
        return null;
    }
    public boolean isTerminal() {
        return true;
    }
    public String toString() {
        return "Digit{" +
                "character=" + character +
                '}';
    }
}

class HexDigit implements ParseNode {
    class HexDigitParseException extends Exception {
        public HexDigitParseException(String message) {
            super(message);
        }
    }
    private Character character;
    public HexDigit(List<Token> tokens, Integer start) throws HexDigitParseException {
    }
    public List<ParseNode> getChildren() {
        return null;
    }
    public boolean isTerminal() {
        return true;
    }
    public String toString() {
        return "HexDigit{" +
                "character=" + character +
                '}';
    }
}

class CharLiteral implements ParseNode {
    class CharLiteralParseException extends Exception {
        public CharLiteralParseException(String message) {
            super(message);
        }
    }
    private Character character;
    public CharLiteral(List<Token> tokens, Integer start) throws CharLiteralParseException {
    }
    public List<ParseNode> getChildren() {
        return null;
    }
    public boolean isTerminal() {
        return true;
    }
    public String toString() {
        return "CharLiteral{" +
                "character=" + character +
                '}';
    }
}