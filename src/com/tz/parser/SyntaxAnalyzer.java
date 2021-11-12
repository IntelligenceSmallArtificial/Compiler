package com.tz.parser;

import com.tz.scanner.LexicalAnalyzer.*;
import com.tz.parser.Util.*;
import com.tz.service.FileHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class SyntaxAnalyzer {

    private final Map<String, SymbolContent> symbolTable;
    private final List<Token> token;
    private Stack<Integer> stateStack;
    private Stack<String> symbolStack;
    private List<String> reduceList;
    private final String PARSER_PATH = "data/out/parserList.txt";
    private SemanticAnalyzer semanticAnalyzer;

    public SyntaxAnalyzer(Map<String, SymbolContent> symbolTable, List<Token> token) {
        this.symbolTable = symbolTable;
        this.token = token;
        semanticAnalyzer = new SemanticAnalyzer(symbolTable);
        syntaxAnalyze();
    }

    private void throwError(){
        System.out.println("Error!");
        System.exit(0);
    }

    private void initStack(){
        stateStack = new Stack<>();
        symbolStack = new Stack<>();
        stateStack.push(0);
        symbolStack.push("6");
    }

    private void writeParser(){
        StringBuilder content= new StringBuilder();
        for (String value : reduceList) {
            content.append(value);
            content.append('\n');
        }
        String _content = content.toString();
        System.out.println("Parser：");
        System.out.println(_content);
        System.out.println();
        FileHandler.writeFile(PARSER_PATH,_content,true);
    }

    private void printStack(){
        System.out.print("状态栈：");
        for (int x : stateStack) {
            System.out.print(x+" ");
        }
        System.out.println();
        System.out.print("符号栈：");
        for (String x : symbolStack) {
            System.out.print(x+" ");
        }
        System.out.println();
    }

    public void syntaxAnalyze(){
        initStack();
        reduceList = new ArrayList<>();
        int length = token.size();
        Token currentToken;
        int topState;
        Action action;
        char actionChoose;
        int code;
        Grammar grammar;
        String[] beforeReduce;
        char afterReduce;
        String tempSymbol;

        int i = 0;
        while (i < length){
//            System.out.println();
//            printStack();
            topState = stateStack.peek();
            currentToken = token.get(i);
            action = Util.getAction(topState,currentToken.getType());
            actionChoose = action.getActionChoose();
            code = action.getCode();
            if(actionChoose == 's'){
                stateStack.push(code);
                symbolStack.push(Integer.toString(currentToken.getType()));
                semanticAnalyzer.shift(currentToken);
                i++;
            } else if (actionChoose == 'r') {
                grammar = Util.getGrammar(code);
                beforeReduce = grammar.getBeforeReduce();
                afterReduce = grammar.getAfterReduce();
                for (int j = beforeReduce.length-1;j >= 0;j--){
                    stateStack.pop();
                    tempSymbol = symbolStack.pop();
                    if(!tempSymbol.equals(beforeReduce[j])) throwError();
                }
                reduceList.add(grammar.toString());
                symbolStack.push(Character.toString(afterReduce));
                topState = stateStack.peek();
                stateStack.push(Util.getGoto(topState,afterReduce));
                semanticAnalyzer.reduce(code, beforeReduce.length);
            } else if (actionChoose == 'a') {
                grammar = Util.getGrammar(code);
                reduceList.add(grammar.toString());
                initStack();
                semanticAnalyzer.accept();
                i++;
            } else {
                throwError();
            }
//            printStack();
        }
        writeParser();
        semanticAnalyzer.writeSymbolTable();
        semanticAnalyzer.writeIntermediateCode();
        semanticAnalyzer.callAsmGenerator();
    }





}
