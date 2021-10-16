package com.tz.parser;


import com.tz.service.FileHandler;
import static com.tz.service.Util.split;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;



public class Util {
    private final static String ACTION_PATH = "data/in/action.CSV";
    private final static String GOTO_PATH = "data/in/goto.CSV";
    private final static String GRAMMAR_PATH = "data/in/grammar.txt";
    private final static String [][] actions;
    private final static String [][] gotos;
    private final static Map<Integer,Integer> actionMap = new HashMap<>();
    private final static Map<Character,Integer> gotoMap = new HashMap<>();
    private final static ArrayList<String> grammarList;

    public static class Action{
        private final char actionChoose;
        private final int code;
        public Action(String actionStr){
//            System.out.println("获取到了Action："+actionStr);
            if(actionStr.length()>0){
                String[] actionArray = split(actionStr," ");
                this.actionChoose = actionArray[0].charAt(0);
                this.code = Integer.parseInt(actionArray[1]);
            }else {
                this.actionChoose = '\0';
                this.code = -1;
            }
        }

        public char getActionChoose() {
            return actionChoose;
        }

        public int getCode(){
            return code;
        }
    }

    public static class Grammar{
        private final char afterReduce;
        private final String[] beforeReduce;
        private final String grammarStr;

        public Grammar(String grammarStr) {
            this.grammarStr = grammarStr;
            String[] grammar1 = split(grammarStr," -> ");
            afterReduce = grammar1[0].charAt(0);
            String[] grammar2 = split(grammar1[1],";");
            beforeReduce = split(grammar2[0]," ");
        }

        public char getAfterReduce() {
            return afterReduce;
        }

        public String[] getBeforeReduce() {
            return beforeReduce;
        }

        public String getGrammarStr() {
            return grammarStr;
        }

        public String toString() {
            return grammarStr;
        }
    }

    private static void printGoto(){
        System.out.println("Goto表:");
        for (String[] aGoto : gotos) {
            for (int j = 0; j < gotos[0].length; j++) {
                switch (aGoto[j].length()) {
                    case 0:
                        System.out.print("? |");
                        break;
                    case 1:
                        System.out.print(aGoto[j] + " |");
                        break;
                    case 2:
                        System.out.print(aGoto[j] + "|");
                        break;
                }
            }
            System.out.println();
        }
        System.out.println();
    }

    static {
        String[] tempStrings;
        ArrayList<String> actionList = FileHandler.readLines(ACTION_PATH);
        tempStrings = split(actionList.get(0),",");
        for (int i = 1;i < tempStrings.length;i++){
            actionMap.put(Integer.valueOf(tempStrings[i]),i-1);
        }
        actions = new String[actionList.size()-1][tempStrings.length-1];
        for(int i = 1;i < actionList.size();i++){
            tempStrings = split(actionList.get(i),",");
            if (tempStrings.length - 1 >= 0)
                System.arraycopy(tempStrings, 1, actions[i - 1], 0, tempStrings.length - 1);
        }

        ArrayList<String> gotoList = FileHandler.readLines(GOTO_PATH);
        tempStrings = split(gotoList.get(0),",");
        for (int i = 1;i < tempStrings.length;i++){
            gotoMap.put(tempStrings[i].charAt(0),i-1);
        }
        gotos = new String[gotoList.size()-1][tempStrings.length-1];
        for(int i = 1;i < gotoList.size();i++){
            tempStrings = split(gotoList.get(i),",");
            if (tempStrings.length - 1 >= 0)
                System.arraycopy(tempStrings, 1, gotos[i - 1], 0, tempStrings.length - 1);

        }
        grammarList = FileHandler.readLines(GRAMMAR_PATH);
    }

    public static Action getAction(int state, int type){
//        System.out.println("getAction：(当前状态："+state+", Token类型："+type+", actionMap："+actionMap.get(type)+")");
        return new Action(actions[state][actionMap.get(type)]);
    }

    public static int getGoto(int state, char type){
//        System.out.println("getGoto：(当前状态："+state+", Token类型："+type+", gotoMap："+gotoMap.get(type)+")");
        String r = gotos[state][gotoMap.get(type)];
//        System.out.println("获取到了Goto："+r);
        if(r.length()>0){
            return Integer.parseInt(r);
        } else {
            return -1;
        }
    }

    public static Grammar getGrammar(int index){
        return new Grammar(grammarList.get(index));
    }


}
