package com.tz.service;

import java.util.ArrayList;

//这个类提供了一些好用的方法
public class Util {

        public static String[] split(String originStr, String regex){
        ArrayList<String> arrayList = new ArrayList<>();
        String tempStr = originStr;
        int regexLength = regex.length();
        int index;
        while ((index=tempStr.indexOf(regex))!=-1){
            arrayList.add(tempStr.substring(0,index));
            tempStr = tempStr.substring(index+regexLength);
        }
        arrayList.add(tempStr);
        String[] result = new String[arrayList.size()];
        for (int i = 0;i < result.length;i++){
            result[i] = arrayList.get(i);
        }
        return result;
    }


}
