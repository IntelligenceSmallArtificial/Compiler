package com.tz.scanner;

import com.tz.scanner.LexicalAnalyzer.*;
import com.tz.parser.Util.*;
import com.tz.service.FileHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class SemanticAnalyzer {
    private Stack<Semantic> stateStack;
    private final String INTERMEDIATE_Code_PATH = "data/out/intermediateCode.txt";
    private final String SYMBOL_TABLE_PATH = "data/out/symbolTable.txt";
    private final List<Intermediate> intermediateList;
    private int offset = 0;
    private int addr = 0;

    private final Map<String, SymbolContent> symbolTable;
    public SemanticAnalyzer(Map<String, SymbolContent> symbolTable) {
        this.symbolTable = symbolTable;
        initStack();
        intermediateList = new ArrayList<>();
    }

    public class Intermediate{
        private final String op;
        private final String arg1;
        private final String arg2;
        private final String result;

        public Intermediate(String op, String arg1, String arg2, String result) {
            this.op = op;
            this.arg1 = arg1;
            this.arg2 = arg2;
            this.result = result;
        }

        public String getOp() {
            return op;
        }

        public String getArg1() {
            return arg1;
        }

        public String getArg2() {
            return arg2;
        }

        public String getResult() {
            return result;
        }

        @Override
        public String toString() {
            return "("+op+','+arg1+','+ arg2 + ','+result+')';
        }
    }

    public class Semantic{
        private int type;
        private int width;
        private String name;
        private String addr;

        public Semantic(int type, int width, String name, String addr) {
            this.type = type;
            this.width = width;
            this.name = name;
            this.addr = addr;
        }

        public Semantic(String name) {
            this.type = 0;
            this.width = 0;
            this.name = name;
            this.addr = null;
        }

        public Semantic(int type, int width) {
            this.type = type;
            this.width = width;
            this.name = null;
            this.addr = null;
        }

        public Semantic() {
            this.type = 0;
            this.width = 0;
            this.name = null;
            this.addr = null;
        }

        public void setAddr(String addr) {
            this.addr = addr;
        }

        public void setType(int type) {
            this.type = type;
        }

        public void setWidth(int width) {
            this.width = width;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getType() {
            return type;
        }

        public int getWidth() {
            return width;
        }

        public String getName() {
            return name;
        }

        public String getAddr() {
            return addr;
        }
    }

    public void error(){
        System.out.println("SemanticAnalyzer Error!");
        System.exit(1);
    }

    public void initStack(){
        stateStack = new Stack<>();
        stateStack.push(new Semantic());
    }

    public void enter(String name, int type, int offset){
        symbolTable.put(name, new SymbolContent(type, offset));
    }

    public String newtemp(){
        String temp = "t" + addr;
        addr++;
        return temp;
    }

    public void genCode(String op, String arg1, String arg2, String result){
        intermediateList.add(new Intermediate(op, arg1, arg2, result));
    }

    public void genCode(String op, String arg1, String result){
        intermediateList.add(new Intermediate(op, arg1, "", result));
    }

    public void shift(Token token){
        int type = token.getType();
        String describe = token.getDescribe();
        if (type == 1||type == 2) {
            stateStack.push(new Semantic(describe));
        } else {
            stateStack.push(new Semantic());
        }
    }

    public void reduce(int code , int length){
        Semantic[] s = new Semantic[3];
        String t;
        for (int i = length-1;i >=0 ;i--){
            s[i] = stateStack.pop();
        }
        switch (code){
            case 1: // S -> D 1;
                enter(s[1].name, s[0].type,offset);
                offset+=s[0].width;
                stateStack.push(new Semantic());
                break;
            case 2: // D -> 30;
                stateStack.push(new Semantic(30,4));
                break;
            case 3: // S -> 1 7 E;
                if(symbolTable.get(s[0].name)!=null){
                    genCode("=",s[0].name,s[2].addr);
                    stateStack.push(new Semantic());
                } else error();
                break;
            case 4: // E -> E 8 A;
                t = newtemp();
                genCode("+",s[0].addr,s[2].addr,t);
                stateStack.push(new Semantic(0,0,"",t));
                break;
            case 5: // E -> E 9 A;
                t = newtemp();
                genCode("-",s[0].addr,s[2].addr,t);
                stateStack.push(new Semantic(0,0,"",t));
                break;
            case 6: // E -> A;
            case 8: // A -> B;
                stateStack.push(new Semantic(0,0,"",s[0].addr));
                break;
            case 7: // A -> A 10 B;
                t = newtemp();
                genCode("*",s[0].addr,s[2].addr,t);
                stateStack.push(new Semantic(0,0,"",t));
                break;
            case 9: // B -> 13 E 14;
                stateStack.push(new Semantic(0,0,"",s[1].addr));
                break;
            case 10: // B -> 1;
                if(symbolTable.get(s[0].name)!=null){
                    stateStack.push(new Semantic(0,0,"",s[0].name));
                } else error();
                break;
            case 11: // B -> 2;
                stateStack.push(new Semantic(0,0,"",s[0].name));
                break;
            default:
                error();
                break;
        }

    }

    public void accept(){
         // P -> S;
        initStack();
    }

    private String symbolTableToString(Object key){
        return "("+key.toString()+","+symbolTable.get(key).toString()+")";

    }

    public void writeSymbolTable(){
        StringBuilder content= new StringBuilder();
        for(String key:symbolTable.keySet()){
            content.append(symbolTableToString(key));
            content.append('\n');
        }
        String _content = content.toString();
        System.out.println("SymbolTable：");
        System.out.println(_content);
        System.out.println();
        FileHandler.writeFile(SYMBOL_TABLE_PATH,_content,true);
    }

    public void writeIntermediateCode(){
        StringBuilder content= new StringBuilder();
        for(Intermediate intermediate:intermediateList){
            content.append(intermediate.toString());
            content.append("\n");
        }
        String _content = content.toString();
        System.out.println("IntermediateCode：");
        System.out.println(_content);
        System.out.println();
        FileHandler.writeFile(INTERMEDIATE_Code_PATH,_content,true);
    }


}
