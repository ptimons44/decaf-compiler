package decaf;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.ArrayList;

public class Parse {
    private List<LexicalToken> tokens;
    private List<String> errors = new ArrayList<>();
    private List<String> warnings = new ArrayList<>();
    public Parse(Scan scan) {
        tokens = scan.getTokens();
    }   
    public boolean getIsValidProgram() {
        try {
            Program program = new Program();
        } catch (Program.ProgramParseException e) {
            System.out.println("Parse Error: " + e.getMessage());
        }
        return errors.isEmpty();
    }

    abstract class ParseNode {
        // Default implementations that can be overridden
        public List<ParseNode> getChildren() {
            return new ArrayList<>();
        }
        
        public boolean isTerminal() {
            return getChildren().isEmpty();
        }
        
        public String toString() {
            String className = this.getClass().getSimpleName();
            List<ParseNode> children = getChildren();
            
            if (children.isEmpty()) {
                return className + "{}";
            } else {
                String childrenStr = children.stream()
                    .map(ParseNode::toString)
                    .collect(Collectors.joining(", "));
                return className + "{" + childrenStr + "}";
            }
        }
    }

    class Program extends ParseNode {
        class ProgramParseException extends Exception {
            public ProgramParseException(String message) {
                super(message);
            }
        }
        private List<ImportDeclaration> importDeclarations;
        private List<FieldDeclaration> fieldDeclarations;
        private List<MethodDeclaration> methodDeclarations;

        public Program() throws ProgramParseException{
            this(0);
        }
        public Program(Integer start) throws ProgramParseException{
        }
        
        @Override
        public List<ParseNode> getChildren() {
            List<ParseNode> children = new ArrayList<>();
            if (importDeclarations != null) children.addAll(importDeclarations);
            if (fieldDeclarations != null) children.addAll(fieldDeclarations);
            if (methodDeclarations != null) children.addAll(methodDeclarations);
            return children;
        }
    }

    class ImportDeclaration extends ParseNode {
        class ImportDeclarationParseException extends Exception {
            public ImportDeclarationParseException(String message) {
                super(message);
            }
        }
        private String identifier;
        
        public ImportDeclaration(Integer start) throws ImportDeclarationParseException {
        }
    }

    class FieldDeclaration extends ParseNode {
        class FieldDeclarationParseException extends Exception {
            public FieldDeclarationParseException(String message) {
                super(message);
            }
        }
        private PrimitiveType type;
        private String identifier;
        
        public FieldDeclaration(Integer start) throws FieldDeclarationParseException {
        }
        
        @Override
        public List<ParseNode> getChildren() {
            List<ParseNode> children = new ArrayList<>(); 
            if (type != null) children.add(type);
            return children;
        }
    }

    class MethodDeclaration extends ParseNode {
        class MethodDeclarationParseException extends Exception {
            public MethodDeclarationParseException(String message) {
                super(message);
            }
        }
        private ReturnType returnType;
        private String methodName;
        private List<Parameter> parameters;
        private Block block;
        
        public MethodDeclaration(Integer start) throws MethodDeclarationParseException {
        }
        
        @Override
        public List<ParseNode> getChildren() {
            List<ParseNode> children = new ArrayList<>();
            if (returnType != null) children.add(returnType);
            if (parameters != null) children.addAll(parameters);
            if (block != null) children.add(block);
            return children;
        }
    }

    class ReturnType extends ParseNode {
        class ReturnTypeParseException extends Exception {
            public ReturnTypeParseException(String message) {
                super(message);
            }
        }
        private PrimitiveType primitiveType;
        private boolean isVoid;
        
        public ReturnType(Integer start) throws ReturnTypeParseException {
        }
        
        @Override
        public List<ParseNode> getChildren() {
            List<ParseNode> children = new ArrayList<>();
            if (primitiveType != null) children.add(primitiveType);
            return children;
        }
    }

    class Parameter extends ParseNode {
        class ParameterParseException extends Exception {
            public ParameterParseException(String message) {
                super(message);
            }
        }
        private PrimitiveType type;
        private String identifier;
        
        public Parameter(Integer start) throws ParameterParseException {
        }
        
        @Override
        public List<ParseNode> getChildren() {
            List<ParseNode> children = new ArrayList<>();
            if (type != null) children.add(type);
            return children;
        }
    }

    class Block extends ParseNode {
        class BlockParseException extends Exception {
            public BlockParseException(String message) {
                super(message);
            }
        }
        private List<FieldDeclaration> fieldDeclarations;
        private List<ParseNode> statements;
        
        public Block(Integer start) throws BlockParseException {
        }
        
        @Override
        public List<ParseNode> getChildren() {
            List<ParseNode> children = new ArrayList<>();
            if (fieldDeclarations != null) children.addAll(fieldDeclarations);
            if (statements != null) children.addAll(statements);
            return children;
        }
    }

    class PrimitiveType extends ParseNode {
        class PrimitiveTypeParseException extends Exception {
            public PrimitiveTypeParseException(String message) {
                super(message);
            }
        }
        private String typeName;
        
        public PrimitiveType(Integer start) throws PrimitiveTypeParseException {
        }
    }

    abstract class Statement extends ParseNode {
        class StatementParseException extends Exception {
            public StatementParseException(String message) {
                super(message);
            }
        }
    }

    class AssignmentStatement extends Statement {
        class AssignmentStatementParseException extends Exception {
            public AssignmentStatementParseException(String message) {
                super(message);
            }
        }
        private Location location;
        private AssignmentExpression assignmentExpression;
        
        public AssignmentStatement(Integer start) throws AssignmentStatementParseException {
        }
        
        @Override
        public List<ParseNode> getChildren() {
            List<ParseNode> children = new ArrayList<>();
            if (location != null) children.add(location);
            if (assignmentExpression != null) children.add(assignmentExpression);
            return children;
        }
    }

    class MethodCallStatement extends Statement {
        class MethodCallStatementParseException extends Exception {
            public MethodCallStatementParseException(String message) {
                super(message);
            }
        }
        private MethodCall methodCall;
        
        public MethodCallStatement(Integer start) throws MethodCallStatementParseException {
        }
        
        @Override
        public List<ParseNode> getChildren() {
            List<ParseNode> children = new ArrayList<>();
            if (methodCall != null) children.add(methodCall);
            return children;
        }
    }

    class BranchStatement extends Statement {
        class BranchStatementParseException extends Exception {
            public BranchStatementParseException(String message) {
                super(message);
            }
        }
        private String condition;
        private Block thenBlock;
        private Block elseBlock;
        
        public BranchStatement(Integer start) throws BranchStatementParseException {
        }
        
        @Override
        public List<ParseNode> getChildren() {
            List<ParseNode> children = new ArrayList<>();
            if (thenBlock != null) children.add(thenBlock);
            if (elseBlock != null) children.add(elseBlock);
            return children;
        }
    }

    class ForLoopStatement extends Statement {
        class ForLoopStatementParseException extends Exception {
            public ForLoopStatementParseException(String message) {
                super(message);
            }
        }
        private String initialization;
        private String condition;
        private String update;
        private Block body;
        
        public ForLoopStatement(Integer start) throws ForLoopStatementParseException {
        }
        
        @Override
        public List<ParseNode> getChildren() {
            List<ParseNode> children = new ArrayList<>();
            if (body != null) children.add(body);
            return children;
        }
    }

    class WhileLoopStatement extends Statement {
        class WhileLoopStatementParseException extends Exception {
            public WhileLoopStatementParseException(String message) {
                super(message);
            }
        }
        private String condition;
        private Block body;
        
        public WhileLoopStatement(Integer start) throws WhileLoopStatementParseException {
        }
        
        @Override
        public List<ParseNode> getChildren() {
            List<ParseNode> children = new ArrayList<>();
            if (body != null) children.add(body);
            return children;
        }
    }

    class ReturnStatement extends Statement {
        class ReturnStatementParseException extends Exception {
            public ReturnStatementParseException(String message) {
                super(message);
            }
        }
        private String returnValue;
        
        public ReturnStatement(Integer start) throws ReturnStatementParseException {
        }
    }

    class BreakStatement extends Statement {
        class BreakStatementParseException extends Exception {
            public BreakStatementParseException(String message) {
                super(message);
            }
        }
        
        public BreakStatement(Integer start) throws BreakStatementParseException {
        }
    }

    class ContinueStatement extends Statement {
        class ContinueStatementParseException extends Exception {
            public ContinueStatementParseException(String message) {
                super(message);
            }
        }
        
        public ContinueStatement(Integer start) throws ContinueStatementParseException {
        }
    }

    class ForUpdate extends ParseNode {
        class ForUpdateParseException extends Exception {
            public ForUpdateParseException(String message) {
                super(message);
            }
        }
        private Location location;
        private AssignmentExpression assignmentExpression;
        
        public ForUpdate(Integer start) throws ForUpdateParseException {
        }
        
        @Override
        public List<ParseNode> getChildren() {
            List<ParseNode> children = new ArrayList<>();
            if (location != null) children.add(location);
            if (assignmentExpression != null) children.add(assignmentExpression);
            return children;
        }
    }

    class AssignmentExpression extends ParseNode {
        class AssignmentExpressionParseException extends Exception {
            public AssignmentExpressionParseException(String message) {
                super(message);
            }
        }
        private Optional<IncrementOrDecrement> incrementOrDecrement;
        private Optional<AssignmentOp> assignmentOp;
        private Optional<Expression> expression;
        private boolean isIncrementOrDecrement;
        
        public AssignmentExpression(Integer start) throws AssignmentExpressionParseException {
        }
        
        @Override
        public List<ParseNode> getChildren() {
            List<ParseNode> children = new ArrayList<>();
            if (incrementOrDecrement != null && incrementOrDecrement.isPresent()) children.add(incrementOrDecrement.get());
            if (assignmentOp != null && assignmentOp.isPresent()) children.add(assignmentOp.get());
            if (expression != null && expression.isPresent()) children.add(expression.get());
            return children;
        }
    }

    class AssignmentOp extends ParseNode {
        class AssignmentOpParseException extends Exception {
            public AssignmentOpParseException(String message) {
                super(message);
            }
        }
        private String operator;
        
        public AssignmentOp(Integer start) throws AssignmentOpParseException {
        }
    }

    class IncrementOrDecrement extends ParseNode {
        class IncrementOrDecrementParseException extends Exception {
            public IncrementOrDecrementParseException(String message) {
                super(message);
            }
        }
        private String operator;
        
        public IncrementOrDecrement(Integer start) throws IncrementOrDecrementParseException {
        }
    }

    class MethodCall extends Statement {
        class MethodCallParseException extends Exception {
            public MethodCallParseException(String message) {
                super(message);
            }
        }
        private MethodName methodName;
        private List<MethodArgument> methodArguments;
        
        public MethodCall(Integer start) throws MethodCallParseException {
        }
        
        @Override
        public List<ParseNode> getChildren() {
            List<ParseNode> children = new ArrayList<>();
            if (methodName != null) children.add(methodName);
            if (methodArguments != null) children.addAll(methodArguments);
            return children;
        }
    }

    class MethodName extends ParseNode {
        class MethodNameParseException extends Exception {
            public MethodNameParseException(String message) {
                super(message);
            }
        }
        private String name;
        
        public MethodName(Integer start) throws MethodNameParseException {
        }
    }

    class Identifier extends ParseNode {
        class IdentifierParseException extends Exception {
            public IdentifierParseException(String message) {
                super(message);
            }
        }
        private Alpha alpha;
        private List<AlphaNumeric> alphaNumerics;
        
        public Identifier(Integer start) throws IdentifierParseException {
        }
        
        @Override
        public List<ParseNode> getChildren() {
            List<ParseNode> children = new ArrayList<>();
            if (alpha != null) children.add(alpha);
            if (alphaNumerics != null) children.addAll(alphaNumerics);
            return children;
        }
    }

    class MethodArgument extends ParseNode {
        class MethodArgumentParseException extends Exception {
            public MethodArgumentParseException(String message) {
                super(message);
            }
        }
        private Expression expression;
        private StringLiteral stringLiteral;
        private boolean isExpression;
        
        public MethodArgument(Integer start) throws MethodArgumentParseException {
        }
        
        @Override
        public List<ParseNode> getChildren() {
            List<ParseNode> children = new ArrayList<>();
            if (expression != null) children.add(expression);
            if (stringLiteral != null) children.add(stringLiteral);
            return children;
        }
    }

    abstract class AbstractLocation extends ParseNode {
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
        
        public Location(Integer start) throws LocationParseException {
        }
        
        @Override
        public List<ParseNode> getChildren() {
            List<ParseNode> children = new ArrayList<>();
            if (identifier != null) children.add(identifier);
            return children;
        }
    }

    abstract class Expression extends ParseNode {
        class ExpressionParseException extends Exception {
            public ExpressionParseException(String message) {
                super(message);
            }
        }
    }

    class LocationExpression extends Expression {
        class LocationExpressionParseException extends Exception {
            public LocationExpressionParseException(String message) {
                super(message);
            }
        }
        private Location location;
        
        public LocationExpression(Integer start) throws LocationExpressionParseException {
        }
        
        @Override
        public List<ParseNode> getChildren() {
            List<ParseNode> children = new ArrayList<>();
            if (location != null) children.add(location);
            return children;
        }
    }

    abstract class Literal extends Statement {
        class LiteralParseException extends Exception {
            public LiteralParseException(String message) {
                super(message);
            }
        }
    }

    class StringLiteral extends Literal {
        class StringLiteralParseException extends Exception {
            public StringLiteralParseException(String message) {
                super(message);
            }
        }
        private List<CharLiteral> charLiterals;
        
        public StringLiteral(Integer start) throws StringLiteralParseException {
        }
        
        @Override
        public List<ParseNode> getChildren() {
            List<ParseNode> children = new ArrayList<>();
            if (charLiterals != null) children.addAll(charLiterals);
            return children;
        }
    }

    abstract class IntegerLiteral extends Literal {
        class IntegerLiteralParseException extends Exception {
            public IntegerLiteralParseException(String message) {
                super(message);
            }
        }
    }

    class DecimalLiteral extends IntegerLiteral {
        class DecimalLiteralParseException extends Exception {
            public DecimalLiteralParseException(String message) {
                super(message);
            }
        }
        private Integer value;
        
        public DecimalLiteral(Integer start) throws DecimalLiteralParseException {
        }
    }

    class HexLiteral extends IntegerLiteral {
        class HexLiteralParseException extends Exception {
            public HexLiteralParseException(String message) {
                super(message);
            }
        }
        private Integer value;
        
        public HexLiteral(Integer start) throws HexLiteralParseException {
        }
    }

    class LongLiteral extends Literal {
        class LongLiteralParseException extends Exception {
            public LongLiteralParseException(String message) {
                super(message);
            }
        }
        private IntegerLiteral value;
        
        public LongLiteral(Integer start) throws LongLiteralParseException {
        }
        
        @Override
        public List<ParseNode> getChildren() {
            List<ParseNode> children = new ArrayList<>();
            if (value != null) children.add(value);
            return children;
        }
    }

    class BooleanLiteral extends Literal {
        class BooleanLiteralParseException extends Exception {
            public BooleanLiteralParseException(String message) {
                super(message);
            }
        }
        private Boolean value;
        
        public BooleanLiteral(Integer start) throws BooleanLiteralParseException {
        }
    }

    abstract class Operator extends ParseNode {
        class OperatorParseException extends Exception {
            public OperatorParseException(String message) {
                super(message);
            }
        }
    }

    class BinaryOperator extends Operator {
        class BinaryOperatorParseException extends Exception {
            public BinaryOperatorParseException(String message) {
                super(message);
            }
        }
        private String operator;
        
        public BinaryOperator(Integer start) throws BinaryOperatorParseException {
        }
    }

    class RightAssociativeUnaryOperator extends Operator {
        class RightAssociativeUnaryOperatorParseException extends Exception {
            public RightAssociativeUnaryOperatorParseException(String message) {
                super(message);
            }
        }
        private String operator;
        
        public RightAssociativeUnaryOperator(Integer start) throws RightAssociativeUnaryOperatorParseException {
        }
    }

    class Alpha extends ParseNode {
        class AlphaParseException extends Exception {
            public AlphaParseException(String message) {
                super(message);
            }
        }
        private Character character;
        
        public Alpha(Integer start) throws AlphaParseException {
        }
    }

    class AlphaNumeric extends ParseNode {
        class AlphaNumericParseException extends Exception {
            public AlphaNumericParseException(String message) {
                super(message);
            }
        }
        private Character character;
        
        public AlphaNumeric(Integer start) throws AlphaNumericParseException {
        }
    }

    class Digit extends ParseNode {
        class DigitParseException extends Exception {
            public DigitParseException(String message) {
                super(message);
            }
        }
        private Character character;
        
        public Digit(Integer start) throws DigitParseException {
        }
    }

    class HexDigit extends ParseNode {
        class HexDigitParseException extends Exception {
            public HexDigitParseException(String message) {
                super(message);
            }
        }
        private Character character;
        
        public HexDigit(Integer start) throws HexDigitParseException {
        }
    }

    class CharLiteral extends ParseNode {
        class CharLiteralParseException extends Exception {
            public CharLiteralParseException(String message) {
                super(message);
            }
        }
        private Character character;
        
        public CharLiteral(Integer start) throws CharLiteralParseException {
        }
    }
}
