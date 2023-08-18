package visitor.typeAnalyzer;

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
import compileError.Type.FunctionNotDeclared;
import compileError.Type.UnsupportedOperandType;
import compileError.Type.VarNotDeclared;
import symbolTable.SymbolTable;
import symbolTable.itemException.ItemAlreadyExistsException;
import symbolTable.itemException.ItemNotFoundException;
import symbolTable.symbolTableItems.*;
import visitor.Visitor;

import java.util.ArrayList;

public class ExpressionTypeChecker extends Visitor<Type> {
    public ArrayList<CompileError> typeErrors;
    public ExpressionTypeChecker(ArrayList<CompileError> typeErrors){
        this.typeErrors = typeErrors;
    }

    public boolean sameType(Type el1, Type el2){
        //TODO check the two type are same or not

        // +
        if(el1.toString().equals(el2.toString()))
        {
            return true;
        }
        // +
        return false;
    }

    public boolean isLvalue(Expression expr){
        //TODO check the expr are lvalue or not

        return false;
    }


    @Override
    public Type visit(UnaryExpression unaryExpression) {

        Expression uExpr = unaryExpression.getOperand();
        Type expType = uExpr.accept(this);
        UnaryOperator operator = unaryExpression.getUnaryOperator();

        if((expType instanceof  FloatType) && !operator.equals(UnaryOperator.not)){
            return new FloatType();
        }
        else if((expType instanceof BooleanType) && operator.equals(UnaryOperator.not)){
            return new BooleanType();
        }
        else if((expType instanceof IntType) && !operator.equals(UnaryOperator.not)){
            return new IntType();
        }
        else if(expType instanceof NoType) { // don't added ti error of operands
            return new NoType();
        }
        else {
            typeErrors.add(new UnsupportedOperandType(unaryExpression.getLine(), operator.name()));
            return new NoType();
        }
    }

    @Override
    public Type visit(BinaryExpression binaryExpression) {
        Type tl = binaryExpression.getLeft().accept(this);
        Type tr = binaryExpression.getRight().accept(this);
        BinaryOperator operator = binaryExpression.getBinaryOperator();
        // +
        if ((tl instanceof BooleanType && tr instanceof BooleanType)
                && (operator.toString().equals(BinaryOperator.or.toString())
                || operator.toString().equals(BinaryOperator.and.toString())
                || operator.toString().equals(BinaryOperator.eq.toString())
                || operator.toString().equals(BinaryOperator.gt.toString())
                || operator.toString().equals(BinaryOperator.gte.toString())
                || operator.toString().equals(BinaryOperator.lt.toString())
                || operator.toString().equals(BinaryOperator.lte.toString())
                || operator.toString().equals(BinaryOperator.neq.toString()))) {
            return new BooleanType();
        } else if ((tl instanceof IntType && tr instanceof IntType)
                && (operator.toString().equals(BinaryOperator.add.toString())
                || operator.toString().equals(BinaryOperator.sub.toString())
                || operator.toString().equals(BinaryOperator.div.toString())
                || operator.toString().equals(BinaryOperator.mult.toString())
                || operator.toString().equals(BinaryOperator.mod.toString()))) {
            return new IntType();
        } else if ((tl instanceof FloatType && tr instanceof FloatType)
                && (operator.toString().equals(BinaryOperator.add.toString())
                || operator.toString().equals(BinaryOperator.sub.toString())
                || operator.toString().equals(BinaryOperator.div.toString())
                || operator.toString().equals(BinaryOperator.mult.toString()))) {
            return new FloatType();
        } else if((tl instanceof IntType && tr instanceof IntType)
                || (tl instanceof FloatType && tr instanceof FloatType)
                && (operator.toString().equals(BinaryOperator.eq.toString())
                || operator.toString().equals(BinaryOperator.lt.toString())
                || operator.toString().equals(BinaryOperator.lte.toString())
                || operator.toString().equals(BinaryOperator.gt.toString())
                || operator.toString().equals(BinaryOperator.gte.toString()))) {
            return new BooleanType();

        } else if((operator.toString().equals(BinaryOperator.add.toString())
                || operator.toString().equals(BinaryOperator.sub.toString())
                || operator.toString().equals(BinaryOperator.div.toString())
                || operator.toString().equals(BinaryOperator.mult.toString())
                || operator.toString().equals(BinaryOperator.mod.toString()))
                && (tl instanceof BooleanType || tr instanceof BooleanType)) {
            typeErrors.add(new UnsupportedOperandType(binaryExpression.getLine(), operator.name()));
            return new NoType();
        }
        else if((operator.toString().equals(BinaryOperator.and.toString())
                || operator.toString().equals(BinaryOperator.or.toString()))
                && ((tl instanceof IntType) || (tl instanceof FloatType)
                || (tr instanceof IntType) || (tr instanceof FloatType))){
            typeErrors.add(new UnsupportedOperandType(binaryExpression.getLine(), operator.name()));
            return new NoType();
        } else if((tl instanceof NoType) || (tr instanceof NoType)) { // don't add to errors of operands
            return new NoType();
        }
        else {
            typeErrors.add(new UnsupportedOperandType(binaryExpression.getLine(), operator.name()));
            return new NoType();
        }
    }
        // +

    @Override
    public Type visit(Identifier identifier) {
        try {
            if(SymbolTable.top.get(VariableItem.STARTKEY + identifier.getName()) instanceof VariableItem) {
                VariableItem variableItem = (VariableItem) SymbolTable.top.get(VariableItem.STARTKEY + identifier.getName());
                return variableItem.getType();
            }
            else if (SymbolTable.top.get(VariableItem.STARTKEY + identifier.getName()) instanceof ArrayItem) {
                ArrayItem arrayItem = (ArrayItem) SymbolTable.top.get(VariableItem.STARTKEY + identifier.getName());
                return arrayItem.getType();
            }
        }catch (ItemNotFoundException e){

        }
        typeErrors.add(new VarNotDeclared(identifier.getLine(), identifier.getName()));
        return new NoType();
    }

    @Override
    public Type visit(FunctionCall functionCall) {
        try {
            FunctionItem functionItem = (FunctionItem) SymbolTable.root.get(FunctionItem.STARTKEY + functionCall.getUFuncName().getName());
            for(Expression expression : functionCall.getArgs()) {
                expression.accept(this);
            }
            return functionItem.getHandlerDeclaration().getType();
        }catch(ItemNotFoundException e){
            typeErrors.add(new FunctionNotDeclared(functionCall.getLine(), functionCall.getUFuncName().getName()));
            for(Expression expression : functionCall.getArgs()) {
                expression.accept(this);
            }
            return new NoType();
        }
    }

    @Override
    public Type visit(IntValue value) {
        return new IntType();
    }

    @Override
    public Type visit(FloatValue value) {
        return new FloatType();
    }

    @Override
    public Type visit(BooleanValue value) {
        return new BooleanType();
    }

    @Override
    public Type visit(IntType value) {
        return new IntType();
    }
    @Override
    public Type visit(FloatType value) {
        return new FloatType();
    }

    @Override
    public Type visit(BooleanType value) {
        return new BooleanType();
    }

    @Override
    public Type visit(ArrayAccess arrayAccess) {
        try{
            if(SymbolTable.top.get(ArrayItem.STARTKEY + arrayAccess.getName()) instanceof VariableItem) {
                VariableItem arrayItem = (VariableItem) SymbolTable.top.get(ArrayItem.STARTKEY + arrayAccess.getName());
                return arrayItem.getType();
            }
            else if(SymbolTable.top.get(ArrayItem.STARTKEY + arrayAccess.getName()) instanceof ArrayItem) {
                ArrayItem arrayItem = (ArrayItem) SymbolTable.top.get(ArrayItem.STARTKEY + arrayAccess.getName());
                return arrayItem.getType();
            }
            typeErrors.add(new VarNotDeclared(arrayAccess.getLine(), arrayAccess.getName()));
            return new NoType();
        } catch (ItemNotFoundException e) {
            typeErrors.add(new VarNotDeclared(arrayAccess.getLine(), arrayAccess.getName()));
            return new NoType();
        }
    }

    @Override
    public Type visit(QueryExpression queryExpression) {
        if(queryExpression.getVar() == null) {
            return new NoType();
        }
        else {
            queryExpression.getVar().accept(this);
            // check if not valid turn type to notype // checked don't need
            return new BooleanType();
        }
    }
}
