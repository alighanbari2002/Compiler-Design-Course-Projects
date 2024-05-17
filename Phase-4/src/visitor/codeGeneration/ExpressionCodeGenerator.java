package visitor.codeGeneration;

import ast.node.expression.*;
import ast.node.expression.operators.BinaryOperator;
import ast.node.expression.operators.UnaryOperator;
import ast.node.expression.values.BooleanValue;
import ast.node.expression.values.FloatValue;
import ast.node.expression.values.IntValue;
import ast.type.NoType;
import ast.type.Type;
import ast.type.primitiveType.BooleanType;
import ast.type.primitiveType.FloatType;
import ast.type.primitiveType.IntType;
import compileError.CompileError;
//import compileError.Type.FunctionNotDeclared;
//import compileError.Type.UnsupportedOperandType;
//import compileError.Type.VarNotDeclared;
import compileError.Type.VarNotDeclared;
import symbolTable.SymbolTable;
import symbolTable.itemException.ItemAlreadyExistsException;
import symbolTable.itemException.ItemNotFoundException;
import symbolTable.symbolTableItems.*;
import visitor.Visitor;

import java.util.ArrayList;
import java.util.HashMap;

public class ExpressionCodeGenerator extends Visitor<Void>{
    public ArrayList<String> jasminCommands;
    public IntValue localVarInd = null;
    public HashMap<String, IntValue> localVarArray = null;
    public final String CODE_FILE = "Main";
    public ExpressionCodeGenerator(ArrayList<String> jCommands, IntValue localVarIndex, HashMap<String, IntValue> localArrVar) {
        this.jasminCommands = jCommands;
        this.localVarInd = localVarIndex;
        this.localVarArray = localArrVar;
    }
    @Override
    public Void visit(UnaryExpression unaryExpression) {

        Expression uExpr = unaryExpression.getOperand();
        uExpr.accept(this);
        UnaryOperator operator = unaryExpression.getUnaryOperator();

        if(operator.toString().equals(UnaryOperator.minus.toString())) {
            jasminCommands.add("    ineg");
        }
        else if(operator.toString().equals(UnaryOperator.not)) {
            System.out.println("not supported unary operand");
        }
        return null;
    }

    @Override
    public Void visit(BinaryExpression binaryExpression) {
        binaryExpression.getLeft().accept(this);
        binaryExpression.getRight().accept(this);
        BinaryOperator operator = binaryExpression.getBinaryOperator();

        if(operator.toString().equals(BinaryOperator.add.toString())) {
            jasminCommands.add("    iadd");
        }
        else if(operator.toString().equals(BinaryOperator.sub.toString())) {
            jasminCommands.add("    isub");
        }
        else if(operator.toString().equals(BinaryOperator.mult.toString())) {
            jasminCommands.add("    imul");
        }
        else if(operator.toString().equals(BinaryOperator.div.toString())) {
            jasminCommands.add("    idiv");
        }
        else if(operator.toString().equals(BinaryOperator.mod.toString())) {
            jasminCommands.add("    irem");
        }
        else {
            System.out.println("Not Supported operator in line: " + binaryExpression.getLine());
        }
        return null;
    }


    @Override
    public Void visit(Identifier identifier) {
        try {
            if(SymbolTable.top.get(VariableItem.STARTKEY + identifier.getName()) instanceof VariableItem) {
                VariableItem variableItem = (VariableItem) SymbolTable.top.get(VariableItem.STARTKEY + identifier.getName());
                if(localVarArray.containsKey(variableItem.getName())) {
                    if(localVarArray.get(variableItem.getName()).getConstant() > 3) {
                        jasminCommands.add("    iload " +
                                localVarArray.get(identifier.getName()).getConstant());
                    }
                    else {
                        jasminCommands.add("    iload_" +
                                localVarArray.get(identifier.getName()).getConstant());
                    }
                }
                else {
                    System.out.println("variable: " + identifier.getName() + "does not exist in localVarArray");
                }
            }
            else if (SymbolTable.top.get(VariableItem.STARTKEY + identifier.getName()) instanceof ArrayItem) {
                ArrayItem arrayItem = (ArrayItem) SymbolTable.top.get(VariableItem.STARTKEY + identifier.getName());
                // something for array
            }
        }catch (ItemNotFoundException e){

        }
        return null;
    }

    @Override
    public Void visit(FunctionCall functionCall) {
        try {
            FunctionItem functionItem = (FunctionItem) SymbolTable.root.get(FunctionItem.STARTKEY + functionCall.getUFuncName().getName());
            String I = "";
            for(var i : functionItem.getHandlerDeclaration().getArgs()) {
                I += "I";
            }
            String printRet = (
                    functionItem.getHandlerDeclaration().getType() instanceof IntType
                    ) ?
                    "I" : "";
            jasminCommands.add("    invokestatic " +
                    CODE_FILE + "/" +
                    functionItem.getName() +
                    "(" + I + ")" +
                    printRet);
            for(Expression expression : functionCall.getArgs()) {
                expression.accept(this);
            }

        }catch(ItemNotFoundException e){
            // handled in typeAnalyzer
        }
        return null;
    }

    @Override
    public Void visit(IntValue value) {
        if(value.getConstant() > 5 || value.getConstant() < 0) {
            jasminCommands.add("    bipush " + value.getConstant());
        }
        else {
            jasminCommands.add("    iconst_" + value.getConstant());
        }
        return null;
    }

    @Override
    public Void visit(FloatValue value) {
        System.out.println("float value not supported");
        return null;
    }

    @Override
    public Void visit(BooleanValue value) {
        System.out.println("boolean value not supported");
        return null;
    }

    // not needed
//    @Override
//    public Type visit(IntType value) {
//        return new IntType();
//    }
//    @Override
//    public Type visit(FloatType value) {
//        return new FloatType();
//    }
//
//    @Override
//    public Type visit(BooleanType value) {
//        return new BooleanType();
//    }

    // not needed
//    @Override
//    public Type visit(ArrayAccess arrayAccess) {
//        try{
//            if(SymbolTable.top.get(ArrayItem.STARTKEY + arrayAccess.getName()) instanceof VariableItem) {
//                VariableItem arrayItem = (VariableItem) SymbolTable.top.get(ArrayItem.STARTKEY + arrayAccess.getName());
//                return arrayItem.getType();
//            }
//            else if(SymbolTable.top.get(ArrayItem.STARTKEY + arrayAccess.getName()) instanceof ArrayItem) {
//                ArrayItem arrayItem = (ArrayItem) SymbolTable.top.get(ArrayItem.STARTKEY + arrayAccess.getName());
//                return arrayItem.getType();
//            }
//            typeErrors.add(new VarNotDeclared(arrayAccess.getLine(), arrayAccess.getName()));
//            return new NoType();
//        } catch (ItemNotFoundException e) {
//            typeErrors.add(new VarNotDeclared(arrayAccess.getLine(), arrayAccess.getName()));
//            return new NoType();
//        }
//    }

}
