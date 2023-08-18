package visitor.typeAnalyzer;

import ast.node.Program;
import ast.node.declaration.ArgDeclaration;
import ast.node.declaration.Declaration;
import ast.node.declaration.FuncDeclaration;
import ast.node.declaration.MainDeclaration;
import ast.node.expression.Expression;
import ast.node.expression.FunctionCall;
import ast.node.expression.Identifier;
import ast.node.expression.operators.BinaryOperator;
import ast.node.statement.*;
import ast.type.NoType;
import ast.type.Type;
import ast.type.primitiveType.BooleanType;
import compileError.CompileError;
import compileError.Type.FunctionNotDeclared;
import compileError.Type.LeftSideNotLValue;
import compileError.Type.UnsupportedOperandType;
import compileError.Type.ConditionTypeNotBool;
import symbolTable.SymbolTable;
import symbolTable.itemException.ItemAlreadyExistsException;
import symbolTable.itemException.ItemNotFoundException;
import symbolTable.symbolTableItems.ForLoopItem;
import symbolTable.symbolTableItems.FunctionItem;
import symbolTable.symbolTableItems.MainItem;
import symbolTable.symbolTableItems.VariableItem;
import visitor.Visitor;
import symbolTable.symbolTableItems.ArrayItem;
import java.lang.Object;

import java.util.ArrayList;

public class TypeAnalyzer extends Visitor<Void> {
    public ArrayList<CompileError> typeErrors = new ArrayList<>();
    ExpressionTypeChecker expressionTypeChecker = new ExpressionTypeChecker(typeErrors);

    @Override
    public Void visit(Program program) {
        for(var functionDec : program.getFuncs()) {
            functionDec.accept(this);
        }

        program.getMain().accept(this);

        return null;
    }

    @Override
    public Void visit(FuncDeclaration funcDeclaration) {
        try {
            FunctionItem functionItem = (FunctionItem)  SymbolTable.root.get(FunctionItem.STARTKEY + funcDeclaration.getName().getName());

            SymbolTable.push((functionItem.getFunctionSymbolTable()));
        } catch (ItemNotFoundException e) {
            //unreachable
        }

        for(ArgDeclaration arg : funcDeclaration.getArgs()) {
            arg.accept(this);
        }

        for(var stmt : funcDeclaration.getStatements()) {
            stmt.accept(this);
        }

        SymbolTable.pop();

        return null;
    }

//    is not needed
//    @Override
//    public Void visit(ArgDeclaration argDeclaration) {
//        try {
//            VariableItem variableItem = (VariableItem) SymbolTable.top.get(VariableItem.STARTKEY + argDeclaration.getIdentifier().getName());
//            try {
//                SymbolTable.top.put(variableItem);
//            } catch (ItemAlreadyExistsException e) {
//                System.out.printf("does exist");
//            }
//        } catch(ItemNotFoundException e) {
//
//        }
//
//        return null;
//    }

    @Override
    public Void visit(MainDeclaration mainDeclaration) {
        var mainItem = new MainItem(mainDeclaration);
        var mainSymbolTable = new SymbolTable(null, "main");
        mainItem.setMainItemSymbolTable(mainSymbolTable);

        SymbolTable.push(mainItem.getMainItemSymbolTable());

        for (var stmt : mainDeclaration.getMainStatements()) {
            stmt.accept(this);
        }


        return null;
    }
    @Override
    public Void visit(ForloopStmt forloopStmt) {

        Type arrayItemType = forloopStmt.getArrayName().accept(expressionTypeChecker);

        ForLoopItem forLoopItem = new ForLoopItem(forloopStmt);
        forLoopItem.setForLoopSymbolTable(new SymbolTable(SymbolTable.top, SymbolTable.top.name));
        SymbolTable.push(forLoopItem.getForLoopSymbolTable());

        VariableItem forItVar = new VariableItem(forloopStmt.getIterator().getName(), arrayItemType);
        try {
            SymbolTable.top.put(forItVar);
        } catch (ItemAlreadyExistsException e) {

        }
        for(Statement stmt : forloopStmt.getStatements()) {
            stmt.accept(this);
        }

        SymbolTable.pop();

        return null;
    }

    @Override
    public Void visit(AssignStmt assignStmt) {
        Type tl = assignStmt.getLValue().accept(expressionTypeChecker);
        Type tr = assignStmt.getRValue().accept(expressionTypeChecker);

        if((!expressionTypeChecker.sameType(tl, tr)) && (!(tl instanceof NoType)) && (!(tr instanceof NoType)))
        {
            typeErrors.add(new UnsupportedOperandType(assignStmt.getLine(), BinaryOperator.assign.name()));
        }

        return null;
    }

    @Override
    public Void visit(ImplicationStmt implicationStmt) {
        Type impCondition = implicationStmt.getCondition().accept(expressionTypeChecker);
        if(!(impCondition instanceof BooleanType || impCondition instanceof NoType)) { // + condition for notype condition error relaxation
            typeErrors.add(new ConditionTypeNotBool(implicationStmt.getLine()));
        }
        SymbolTable.push(new SymbolTable(null, SymbolTable.top.name));
        for (Statement s : implicationStmt.getStatements()) {
            s.accept(this);
        }
        SymbolTable.pop();
        return null;
    }
    @Override
    public Void visit(VarDecStmt varDecStmt) {
        if(varDecStmt.getInitialExpression() != null) {
            Type decExpression = varDecStmt.getInitialExpression().accept(expressionTypeChecker);
            if(!decExpression.toString().equals(varDecStmt.getType().toString()) && (!(decExpression instanceof NoType))) {
                typeErrors.add(new UnsupportedOperandType(varDecStmt.getLine(), BinaryOperator.assign.name()));
            }
        }
        try {
            SymbolTable.top.put(new VariableItem(varDecStmt.getIdentifier().getName(), varDecStmt.getType()));
        } catch (ItemAlreadyExistsException e) {
            // not this phase
        }
        return null;
    }

    @Override
    public Void visit(ArrayDecStmt arrayDecStmt) {
        try {
            SymbolTable.top.put(new ArrayItem(arrayDecStmt.getIdentifier().getName(), arrayDecStmt.getType()));
        } catch (ItemAlreadyExistsException e) {

        }
        boolean isInitializerOk = true;
        for(Expression expression : arrayDecStmt.getInitialValues()){
            Type initChecker = expression.accept(expressionTypeChecker);
            if(initChecker instanceof NoType) {
                // do nothing
            }
            else if(!arrayDecStmt.getType().toString().equals(initChecker.toString())) {
                typeErrors.add(new UnsupportedOperandType(arrayDecStmt.getLine(), BinaryOperator.assign.name()));
            }
        }
        if(isInitializerOk == false) {
            typeErrors.add(new UnsupportedOperandType(arrayDecStmt.getLine(), BinaryOperator.assign.name()));
        }
        return null;
    }
    @Override
    public Void visit(PrintStmt printStmt) {
        printStmt.getArg().accept(expressionTypeChecker);
        return null;
    }

    @Override
    public Void visit(ReturnStmt returnStmt) {
        if(returnStmt.getExpression() != null) {
            returnStmt.getExpression().accept(expressionTypeChecker);
        }
        return null;
    }
}
