package com.tz.scanner;

import com.tz.parser.SyntaxAnalyzer;
import com.tz.service.FileHandler;

import java.util.*;

public class LexicalAnalyzer {
    private String src;
    private int status=0;
    private String tempWord="";
    private Map<String,SymbolContent> symbolTable;
    private List<Token> token;
    private final String SRC_PATH = "data/in/input_code.txt";
    private final String TOKEN_PATH = "data/out/token.txt";
    private final String SYMBOL_TABLE_PATH = "data/out/symbolTable.txt";

    public LexicalAnalyzer() {
        lexicalAnalyze();
    }

    public Map<String, SymbolContent> getSymbolTable() {
        return symbolTable;
    }

    public List<Token> getToken() {
        return token;
    }

    private void initClass(){
        symbolTable = new HashMap<String,SymbolContent>();
        token = new ArrayList<>();
        src = null;
        initStatus();
    }

    private String symbolTableToString(Object key){
        return "("+key.toString()+","+symbolTable.get(key).toString()+")";

    }

    public static class SymbolContent {
        private final int type;
        private final Object property;

        public SymbolContent() {
            this.type = -1;
            this.property = "?";
        }

        public SymbolContent(int type, Object property) {
            this.type = type;
            this.property = property;
        }


        @Override
        public String toString() {
            return type+","+property.toString();
        }
    }

    public static class Token {
        private final int type;

        private final String describe;

        public Token(int type, String describe) {
            this.type = type;
            this.describe = describe;
        }

        public String getDescribe() {
            return describe;
        }

        public int getType() {
            return type;
        }

        @Override
        public String toString() {
            return "("+type+","+describe+")";
        }

        public String toDetailed() {
            return "("+Util.codingMap.get(type)+","+describe+")";
        }
    }

    private void readSrc(){
        src = FileHandler.readFile(SRC_PATH);
        if(src==null){
            System.out.println("未找到源代码内容");
            FileHandler.createEmptyFile(SRC_PATH);
            src = FileHandler.readFile(SRC_PATH);
        }
        System.out.println("源代码：");
        System.out.println(src);
        System.out.println();
    }

    private void writeToken(){
        StringBuilder content= new StringBuilder();
        for (Token value : token) {
            content.append(value);
            content.append('\n');
        }
        String _content = content.toString();
        System.out.println("Token：");
        System.out.println(_content);
        System.out.println();
        FileHandler.writeFile(TOKEN_PATH,_content,true);
    }

    private void printDetailedToken(){
        StringBuilder content= new StringBuilder();
        for (Token value : token) {
            content.append(value.toDetailed());
            content.append('\n');
        }
        String _content = content.toString();
        System.out.println("DetailedToken：");
        System.out.println(_content);
        System.out.println();
    }

    private void writeSymbolTable(){
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

    private void throwError(char c){
        System.out.println("Error!");
        System.out.println("char："+c+" UTF-8： "+(int)c);
        System.out.println("currentStatus："+status);
        System.out.println("tempWord："+tempWord);
        System.exit(0);
    }

    private void throwError(){
        System.out.println("Error!");
        System.out.println("currentStatus："+status);
        System.out.println("tempWord："+tempWord);
        System.exit(0);
    }

    private void endChar(){         //遇到了终结符
        switch (status){
            case 0:
                break;
            case 1:         //纯小写字母
                Integer type=Util.keywordMap.get(tempWord);
                if(type==null){         //标识符

                    symbolTable.put(tempWord,new SymbolContent());
                    token.add(new Token(1, tempWord));
                }else{                  //关键字
                    token.add(new Token(type, tempWord));
                }
                break;
            case 2:        //非关键字
                symbolTable.put(tempWord,new SymbolContent());  //标识符
                token.add(new Token(1, tempWord));
                break;
            case 3:        //纯数字
                token.add(new Token(2, tempWord));
                break;
            default:
                throwError();
                break;
        }
    }

    private void initStatus() {
        status=0;
        tempWord="";
    }

    /**
     *状态定义：
     * 0：初始态
     * 1：tempWord=纯小写字母的正闭包
     * 2：tempWord读取过存在大写字母或下划线或数字（不会成为关键字）
     * 3：tempWord=纯数字的正闭包
     * 4: tempWord='
     * 5: tempWord='+任意一个字符
     * 6：tempWord="+任意字符克林闭包
     */

    public void lexicalAnalyze(){
        initClass();
        readSrc();
        for (char c : src.toCharArray()) {
            if((c>=0x41 && c<=0x5A )||c=='_'){   //A~Z或_
                if(status==0||status==1||status==2){
                    status=2;
                    tempWord+=c;
                } else if(status==4){
                    status=5;
                    tempWord+=c;
                } else if(status==6){
                    tempWord+=c;
                } else {
                    throwError(c);  //状态3、5会报错
                }
            } else if(c>=0x61 && c<=0x7A){ //a~z
                if(status==0||status==1){
                    status=1;
                    tempWord+=c;
                } else if(status==2){
                    tempWord+=c;
                } else if(status==4){
                    status=5;
                    tempWord+=c;
                } else if(status==6){
                    tempWord+=c;
                } else {
                    throwError(c);  //状态3、5会报错
                }
            } else if(c>=0x30 && c<=0x39) {   //0~9
                if(status==1||status==2){
                    status=2;
                    tempWord+=c;
                } else if(status==0||status==3){
                    status=3;
                    tempWord+=c;
                } else if(status==4){
                    status=5;
                    tempWord+=c;
                } else if(status==6){
                    tempWord+=c;
                } else {
                    throwError(c);  //状态5会报错
                }
            } else if(c=='\r'||c=='\n'||c=='\t'||c==' ') {
                if(status==0){}
                else if (status==1||status==2||status==3){
                    endChar();
                    initStatus();
                } else if(status==4){
                    status=5;
                    tempWord+=c;
                } else if(status==6){
                    tempWord+=c;
                } else {
                    throwError(c);  //状态5会报错
                }
            } else if(c=='\'') {        //'
                if (status==0){
                    status=4;
                } else if (status==5){     //'+任意字符
                    token.add(new Token(3, tempWord));
                    initStatus();
                } else {
                    throwError(c);  //状态1、2、3、4会报错
                }

            } else if(c=='\"') {        //"
                if (status==0){
                    status=6;
                } else if (status==6){
                    token.add(new Token(4, tempWord));
                    initStatus();
                } else {
                    throwError(c);  //状态1、2、3、4、5会报错
                }
            } else if(c==','||c==';'||c=='='||c=='+'||c=='-'||c=='*'||c=='/'||c=='%'||c=='('||c==')'||c=='{'||c=='}'||c=='<'||c=='>') {
                endChar();
                initStatus();
                String cToString = ""+c;
                Integer type=Util.keywordMap.get(cToString);
                if(type==null){         //标识符
                    throwError(c);
                }else{                  //关键字
                    token.add(new Token(type, cToString));
                }
            } else {
                if (status==6){
                    tempWord+=c;
                }
                else {
                    throwError(c);
                }

            }
        }
        writeSymbolTable();
        writeToken();
//        printDetailedToken();
        new SyntaxAnalyzer(this.symbolTable, this.token);
    }








}
