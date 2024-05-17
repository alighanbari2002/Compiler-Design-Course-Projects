package visitor.codeGeneration;

import ast.node.Program;
import ast.node.declaration.*;
import ast.node.expression.Expression;
import ast.node.expression.Variable;
import ast.node.expression.operators.BinaryOperator;
import ast.node.expression.values.IntValue;
import ast.node.statement.*;
import ast.type.NoType;
import ast.type.Type;
import ast.type.primitiveType.BooleanType;
import ast.type.primitiveType.IntType;
import compileError.*;
import compileError.Name.*;
import compileError.Type.ConditionTypeNotBool;
import compileError.Type.UnsupportedOperandType;
import jdk.jshell.spi.ExecutionControl;
import symbolTable.SymbolTable;
import symbolTable.itemException.ItemNotFoundException;
import symbolTable.symbolTableItems.*;
import symbolTable.itemException.ItemAlreadyExistsException;
import symbolTable.symbolTableItems.VariableItem;
import visitor.Visitor;

import java.io.*;

import java.util.ArrayList;
import java.util.HashMap;

public class CodeGeneration extends Visitor<Void> {

    private IntValue localVarIndex = new IntValue(0);
    private IntValue line = new IntValue(0);
    public final String CODE_FILE = "Main";
    private ArrayList<String> jasminCommands = new ArrayList<>();
    private HashMap<String, IntValue> localArray = new HashMap<>();
    private void generateJasmin() {
        try {
            FileWriter printWriter = new FileWriter("result.j");
            String ss = "";
            for(String s : jasminCommands) {
                ss += s + '\n';
//                System.out.println(s);
            }
//            System.out.println(ss);
            printWriter.write(ss);
            printWriter.close();
        } catch (IOException e) {
            System.out.println("error occured on writing to file");
        }

    }
    ExpressionCodeGenerator expressionCodeGenerator = new ExpressionCodeGenerator(jasminCommands, localVarIndex, localArray);

    @Override
    public Void visit(Program program) {
        jasminCommands.add(".class public " + CODE_FILE);
        jasminCommands.add(".super java/lang/Object");
        jasminCommands.add(".method public <init>()V");
        jasminCommands.add("    .limit stack 80");
        jasminCommands.add("    .limit locals 80");
        jasminCommands.add("    0: aload_0");
        jasminCommands.add("    1: invokespecial java/lang/Object/<init>()V");
        jasminCommands.add("    4: return");
        jasminCommands.add(".end method\n");
        for(var functionDec : program.getFuncs()) {
            functionDec.accept(this);
        }

        program.getMain().accept(this);

        generateJasmin();
//        try {
//            Runtime.getRuntime().exec("java -jar jasmin.jar *.j && java Main");
//        } catch (Exception e) {
//            System.out.println("command did not run");
//        }
        return null;
    }

    @Override
    public Void visit(FuncDeclaration funcDeclaration) {
        localVarIndex.setConstant(0);
        FunctionItem functionItem;
        try {
            functionItem = (FunctionItem)  SymbolTable.root.get(FunctionItem.STARTKEY + funcDeclaration.getName().getName());

            SymbolTable.push((functionItem.getFunctionSymbolTable()));
        } catch (ItemNotFoundException e) {
            //unreachable
            return null;
        }
        String I = "";
        for(ArgDeclaration arg : funcDeclaration.getArgs()) {
            I += "I";
            localVarIndex.inc();
        }
        jasminCommands.add(".method static " +
                            functionItem.getName() +
                            "(" + I + ")" + "I");
        jasminCommands.add("    .limit stack 80");
        jasminCommands.add("    .limit locals 80");

        for(ArgDeclaration arg : funcDeclaration.getArgs()) {
            arg.accept(this);
        }

        for(var stmt : funcDeclaration.getStatements()) {
            stmt.accept(this);
        }

        SymbolTable.pop();

        jasminCommands.add(".end method\n");
        return null;
    }

    @Override
    public Void visit(MainDeclaration mainDeclaration) {
        var mainItem = new MainItem(mainDeclaration);
        var mainSymbolTable = new SymbolTable(null, "main");
        mainItem.setMainItemSymbolTable(mainSymbolTable);

        SymbolTable.push(mainItem.getMainItemSymbolTable());

        jasminCommands.add(".method public static main([Ljava/lang/String;)V");
        jasminCommands.add("    .limit stack 80");
        jasminCommands.add("    .limit locals 80");
        localVarIndex.setConstant(1);
        for (var stmt : mainDeclaration.getMainStatements()) {
            stmt.accept(this);
        }

        jasminCommands.add("    getstatic java/lang/System/out Ljava/io/PrintStream;\n" +
                "   iload_1\n" +
                "   invokevirtual java/io/PrintStream/println(I)V");
        jasminCommands.add("    return");
        jasminCommands.add(".end method\n");
        return null;
    }

    // does not need to be used
//    @Override
//    public Void visit(ForloopStmt forloopStmt) {
//
//        Type arrayItemType = forloopStmt.getArrayName().accept(expressionCodeGenerator);
//
//        ForLoopItem forLoopItem = new ForLoopItem(forloopStmt);
//        forLoopItem.setForLoopSymbolTable(new SymbolTable(SymbolTable.top, SymbolTable.top.name));
//        SymbolTable.push(forLoopItem.getForLoopSymbolTable());
//
//        VariableItem forItVar = new VariableItem(forloopStmt.getIterator().getName(), arrayItemType);
//        try {
//            SymbolTable.top.put(forItVar);
//        } catch (ItemAlreadyExistsException e) {
//
//        }
//        for(Statement stmt : forloopStmt.getStatements()) {
//            stmt.accept(this);
//        }
//
//        SymbolTable.pop();
//
//        return null;
//    }

    // does not need to be used
//    @Override
//    public Void visit(ImplicationStmt implicationStmt) {
//        Type impCondition = implicationStmt.getCondition().accept(expressionCodeGenerator);
//        if(!(impCondition instanceof BooleanType || impCondition instanceof NoType)) { // + condition for notype condition error relaxation
//            typeErrors.add(new ConditionTypeNotBool(implicationStmt.getLine()));
//        }
//        SymbolTable.push(new SymbolTable(null, SymbolTable.top.name));
//        for (Statement s : implicationStmt.getStatements()) {
//            s.accept(this);
//        }
//        SymbolTable.pop();
//        return null;
//    }

    @Override
    public Void visit(VarDecStmt varDecStmt) {

        localArray.put(varDecStmt.getIdentifier().getName(), new IntValue(localVarIndex.getConstant()));
        if(varDecStmt.getInitialExpression() != null) {
            varDecStmt.getInitialExpression().accept(expressionCodeGenerator);
//            if(!decExpression.toString().equals(varDecStmt.getType().toString()) && (!(decExpression instanceof NoType))) {
//                typeErrors.add(new UnsupportedOperandType(varDecStmt.getLine(), BinaryOperator.assign.name()));
//            }
            if(localArray.get(varDecStmt.getIdentifier().getName()).getConstant() <= 3 && (varDecStmt.getType() instanceof IntType)) {
                jasminCommands.add("    istore_" +
                                    localArray.get(varDecStmt.getIdentifier().getName()).getConstant());
                localVarIndex.inc();
            }
            else if(localArray.get(varDecStmt.getIdentifier().getName()).getConstant() > 3 && (varDecStmt.getType() instanceof IntType)) {
                jasminCommands.add("    istore " +
                                    localArray.get(varDecStmt.getIdentifier().getName()).getConstant());
                localVarIndex.inc();
            }
            else {
                System.out.println("Not Supported in line: " + varDecStmt.getLine());
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
    public Void visit(AssignStmt assignStmt) {

        assignStmt.getRValue().accept(expressionCodeGenerator);

        Variable lv = (Variable) assignStmt.getLValue();
        if(localArray.containsKey(lv.getName())) {
            if(localArray.get(lv.getName()).getConstant() > 3) {
                jasminCommands.add("    istore " +
                        localArray.get(lv.getName()).getConstant());
            }
            else {
                jasminCommands.add("    istore_" +
                        localArray.get(lv.getName()).getConstant());
            }
        }
        else {
            System.out.println("variable: " + lv.getName() + "does not exist");
        }

        return null;
    }

//    @Override
//    public Void visit(ArrayDecStmt arrayDecStmt) {
//        try {
//            SymbolTable.top.put(new ArrayItem(arrayDecStmt.getIdentifier().getName(), arrayDecStmt.getType()));
//        } catch (ItemAlreadyExistsException e) {
//
//        }
//        boolean isInitializerOk = true;
//        for (Expression expression : arrayDecStmt.getInitialValues()) {
//            Type initChecker = expression.accept(expressionCodeGenerator);
//            if (initChecker instanceof NoType) {
//                // do nothing
//            } else if (!arrayDecStmt.getType().toString().equals(initChecker.toString())) {
//                typeErrors.add(new UnsupportedOperandType(arrayDecStmt.getLine(), BinaryOperator.assign.name()));
//            }
//        }
//        if (isInitializerOk == false) {
//            typeErrors.add(new UnsupportedOperandType(arrayDecStmt.getLine(), BinaryOperator.assign.name()));
//        }
//        return null;
//    }

//    we do not use print in this phase
//    @Override
//    public Void visit(PrintStmt printStmt) {
//        printStmt.getArg().accept(expressionCodeGenerator);
//        return null;
//    }

    @Override
    public Void visit(ReturnStmt returnStmt) {
        if(returnStmt.getExpression() != null) {
            returnStmt.getExpression().accept(expressionCodeGenerator);
        }
        jasminCommands.add("    ireturn");
        return null;
    }
}
