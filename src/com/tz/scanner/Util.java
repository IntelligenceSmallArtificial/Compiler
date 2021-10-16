package com.tz.scanner;

import java.util.HashMap;
import java.util.Map;

public class Util {
    public final static Map<Integer, String> codingMap = new HashMap<>();
    public final static Map<String, Integer> keywordMap = new HashMap<>();

    static {
        codingMap.put(1,"Identifier");  //标识符
        codingMap.put(2,"IntConst");   //无符号整常数
        codingMap.put(3,"CharConst");    //字符常数
        codingMap.put(4,"StringConst");    //字符串常数
        codingMap.put(5,",");
        codingMap.put(6,";");
        codingMap.put(7,"=");
        codingMap.put(8,"+");
        codingMap.put(9,"-");
        codingMap.put(10,"*");
        codingMap.put(11,"/");
        codingMap.put(12,"%");
        codingMap.put(13,"(");
        codingMap.put(14,")");
        codingMap.put(15,"{");
        codingMap.put(16,"}");
        codingMap.put(17,"break");
        codingMap.put(18,"case");
        codingMap.put(19,"char");
        codingMap.put(20,"const");
        codingMap.put(21,"continue");
        codingMap.put(22,"default");
        codingMap.put(23,"do");
        codingMap.put(24,"double");
        codingMap.put(25,"else");
        codingMap.put(26,"float");
        codingMap.put(27,"for");
        codingMap.put(28,"goto");
        codingMap.put(29,"if");
        codingMap.put(30,"int");
        codingMap.put(31,"long");
        codingMap.put(32,"return");
        codingMap.put(33,"short");
        codingMap.put(34,"switch");
        codingMap.put(35,"unsigned");
        codingMap.put(36,"void");
        codingMap.put(37,"while");

        for (int i=5;i<=37;i++) {
            keywordMap.put(codingMap.get(i),i);
        }
    }

}
