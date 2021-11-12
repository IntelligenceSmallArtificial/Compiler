package com.tz.asmGenerator;

import com.tz.scanner.LexicalAnalyzer;
import com.tz.scanner.LexicalAnalyzer.SymbolContent;
import com.tz.parser.SemanticAnalyzer.Intermediate;
import com.tz.service.FileHandler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AsmGenerator {
    private final Map<String, SymbolContent> symbolTable;
    private final List<Intermediate> intermediateList;
    private StringBuilder asm;
    private final String ASSEMBLY_LANGUAGE_PATH = "data/out/assemblyLanguage.asm";
    private final String[] registerCode = {"EAX","EBX","ECX","EDX"};
    private final String[] registerTable = new String[registerCode.length];         //寄存器表
    private final boolean[] registerHeld = new boolean[registerCode.length];     //寄存器是否被占有
    private final int MAX_FUTURE = 10;
    private String op;
    private String arg1;
    private String arg2;
    private String result;
    private int tr;
    private int t1;
    private int t2;
    private boolean regSave;


    public AsmGenerator(Map<String, SymbolContent> symbolTable, List<Intermediate> intermediateList) {
        this.symbolTable = symbolTable;
        this.intermediateList = intermediateList;
        asmGenerate();
    }

    private void error(){
        System.out.println(asm.toString());
        System.out.println("Error!");
        System.exit(0);
    }

    private void init(){
        for (int i = 0;i < registerCode.length;i++){
            registerTable[i] = "";
            registerHeld[i] = false;
        }
        asm = new StringBuilder();
        regSave = false;
    }

    private int getSymbolReg(String symbol){
        for (int i = 0;i < registerCode.length;i++){
            if(registerTable[i].equals(symbol)&&registerHeld[i]) return i;
        }
        return -1;
    }

    private int recentlyUse(String id,int currentCode){
        Intermediate intermediate;
        for (int i = 0;i < MAX_FUTURE;i++){
            intermediate = intermediateList.get(i+currentCode);
            if(intermediate.getArg1().equals(id)||intermediate.getArg2().equals(id)){
                return i;
            }
        }
        return MAX_FUTURE;
    }

    private int getUnusedReg(){
        for (int i = 0;i < registerCode.length;i++){
            if(!registerHeld[i]) return i;
        }
        return -1;
    }

    private void getTypes(){
        t1 = getType(arg1);
        t2 = getType(arg2);
        tr = getType(result);
    }

    //arg顺序：$靠前于id靠前于num,在寄存器中id靠前于不在（t1>=t2)
    private int sortType(boolean canExchange){
        getTypes();
        String t;
        if(t2==-1) return tr*100+t1*10;
        if(t1<t2&&canExchange){
            t = arg1;
            arg1 = arg2;
            arg2 = t;
            getTypes();
        }
        if(t1==1&&t2==1&&canExchange){
            if(getSymbolReg(arg1)==-1&&getSymbolReg(arg2)!=-1){
                t = arg1;
                arg1 = arg2;
                arg2 = t;
                getTypes();
            }
        }
        regSave = (t1 == 1 && getSymbolReg(arg1) != -1);
        return tr*100+t1*10+t2;
    }

    //number:0  id:1  $:2
    private int getType(String symbol){
        if(symbol.length()==0) return -1;
        char c = symbol.charAt(0);
        if(c == '$'){
            return 2;
        } else if(c>='0' && c<='9'){
            return 0;
        } else if((c>='a' && c<='z')||(c>='A' && c<='Z')){
            return 1;
        } else {
            return -1;
        }
    }

    private int readDollarReg(String symbol){
        for (int i = 0;i < registerCode.length;i++){
            if(registerTable[i].equals(symbol)&&registerHeld[i]) {
                registerHeld[i] = false;
                return i;
            }
        }
        error();
        return -1;
    }

    //夺取一个寄存器
    private int robReg(int currentCode){
        int reg = getUnusedReg();
        if(reg!=-1) return reg;
        int future = 0;
        int currentFuture;
        String currentId;
        for (int i = 0;i < registerCode.length;i++){
            currentId = registerTable[i];
            if(getType(currentId)==1){
                currentFuture = recentlyUse(currentId,currentCode);
                if(currentFuture >= future) {
                    reg = i;
                }
            }
        }
        if(reg!=-1){
            registerHeld[reg]=false;
            registerTable[reg]="";
        }
        return reg;
    }

    private void GenDS(){
        asm.append("DATAS SEGMENT\n");
        SymbolContent symbolContent;
        for(String key:symbolTable.keySet()){
            symbolContent = symbolTable.get(key);
            if(symbolContent.getType()==30){
                asm.append("\t");
                asm.append(key);
                asm.append(" DD 0\n");
            }
        }
        asm.append("DATAS ENDS\n\n");
    }

    private void GenCS(){
        asm.append("CODES SEGMENT \nASSUME CS:CODES,DS:DATAS\nSTART: \n\tMOV AX,DATAS\n\tMOV DS,AX\n");

        int reg1 = -1;
        int reg2 = -1;
        int i = 0;
        int types;
        for (Intermediate intermediate:intermediateList) {
            i++;
            op = intermediate.getOp();
            arg1 = intermediate.getArg1();
            arg2 = intermediate.getArg2();
            result = intermediate.getResult();
            t1 = getType(arg1);
            t2 = getType(arg2);
            tr = getType(result);

            switch (op){
                case "=":
                    types=sortType(false);
                    if(t1==0) {
                        asm.append("\tMOV ").append(result).append(",").append(arg1).append("\n");
                    } else if (t1==1) {
                        if(regSave){
                            reg1 = getSymbolReg(arg1);
                        } else {
                            reg1 = robReg(i);
                            asm.append("\tMOV ").append(registerCode[reg1]).append(",").append(arg1).append("\n");
                        }
                        asm.append("\tMOV ").append(result).append(",").append(registerCode[reg1]).append("\n");
                        registerTable[reg1]=result;
                        registerHeld[reg1]=true;
                    } else if (t1==2) {
                        reg1 = readDollarReg(arg1);
                        asm.append("\tMOV ").append(result).append(",").append(registerCode[reg1]).append("\n");
                        registerTable[reg1]=result;
                        registerHeld[reg1]=true;
                    } else error();
                    break;
                case "+":
                    sortType(false);
                    if(t1==0){
                        reg1 = robReg(i);
                        asm.append("\tMOV ").append(registerCode[reg1]).append(",").append(arg1).append("\n");
                    } else if (t1==1) {
                        if(regSave){
                            reg1 = getSymbolReg(arg1);
                        } else {
                            reg1 = robReg(i);
                            asm.append("\tMOV ").append(registerCode[reg1]).append(",").append(arg1).append("\n");
                        }
                    } else if (t1==2) {
                        reg1 =  readDollarReg(arg1);
                    } else error();
                    reg2 = getSymbolReg(arg2);
                    if(reg2==-1){
                        asm.append("\tADD ").append(registerCode[reg1]).append(",").append(arg2).append("\n");
                    } else {
                        asm.append("\tADD ").append(registerCode[reg1]).append(",").append(registerCode[reg2]).append("\n");
                    }
                    if (tr==1){
                        asm.append("\tMOV ").append(result).append(",").append(registerCode[reg1]).append("\n");
                    }
                    registerTable[reg1]=result;
                    registerHeld[reg1]=true;
                    break;
                case "-":
                    sortType(false);
                    if(t1==0){
                        reg1 = robReg(i);
                        asm.append("\tMOV ").append(registerCode[reg1]).append(",").append(arg1).append("\n");
                    } else if (t1==1) {
                        if(regSave){
                            reg1 = getSymbolReg(arg1);
                        } else {
                            reg1 = robReg(i);
                            asm.append("\tMOV ").append(registerCode[reg1]).append(",").append(arg1).append("\n");
                        }
                    } else if (t1==2) {
                        reg1 =  readDollarReg(arg1);
                    } else error();
                    reg2 = getSymbolReg(arg2);
                    if(reg2==-1){
                        asm.append("\tSUB ").append(registerCode[reg1]).append(",").append(arg2).append("\n");
                    } else {
                        asm.append("\tSUB ").append(registerCode[reg1]).append(",").append(registerCode[reg2]).append("\n");
                    }
                    if (tr==1){
                        asm.append("\tMOV ").append(result).append(",").append(registerCode[reg1]).append("\n");
                    }
                    registerTable[reg1]=result;
                    registerHeld[reg1]=true;
                    break;
                case "*":
                    sortType(true);
                    sortType(false);
                    if(t1==0){
                        reg1 = robReg(i);
                        asm.append("\tMOV ").append(registerCode[reg1]).append(",").append(arg1).append("\n");
                    } else if (t1==1) {
                        if(regSave){
                            reg1 = getSymbolReg(arg1);
                        } else {
                            reg1 = robReg(i);
                            asm.append("\tMOV ").append(registerCode[reg1]).append(",").append(arg1).append("\n");
                        }
                    } else if (t1==2) {
                        reg1 =  readDollarReg(arg1);
                    } else error();
                    reg2 = getSymbolReg(arg2);
                    if(reg2==-1){
                        asm.append("\tIMUL ").append(registerCode[reg1]).append(",").append(arg2).append("\n");
                    } else {
                        asm.append("\tIMUL ").append(registerCode[reg1]).append(",").append(registerCode[reg2]).append("\n");
                    }
                    if (tr==1){
                        asm.append("\tMOV ").append(result).append(",").append(registerCode[reg1]).append("\n");
                    }
                    registerTable[reg1]=result;
                    registerHeld[reg1]=true;
                    break;
                default:
                    error();
                    break;
            }
        }
        asm.append("\tMOV AH,4CH\n");
        asm.append("\tINT 21H\n");
        asm.append("CODES ENDS\n");
        asm.append("\tEND START\n\n");
    }

    private void writeAsm(){
        String asmString = asm.toString();
        System.out.println(asmString);
        FileHandler.writeFile(ASSEMBLY_LANGUAGE_PATH, asmString,true);
    }

    private void asmGenerate(){
        init();
        GenDS();
        GenCS();
        writeAsm();
    }

}
