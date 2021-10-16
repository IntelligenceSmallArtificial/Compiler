package com.tz.service;

import java.io.*;
import java.util.ArrayList;


public class FileHandler {

    public static String readFile(String path) {
        StringBuilder result= new StringBuilder();
        File f = new File(path);
        if (!f.exists()) {// 如果文件不存在
            System.out.println("(readFile) 文件："+path+" 未找到");
            return null;
        }
        FileInputStream fis = null;
        InputStreamReader isr = null;
        BufferedReader br = null;
        try {
            fis = new FileInputStream(f);// 文件字节输入流
            isr = new InputStreamReader(fis);// 字节流转字符流
            br = new BufferedReader(isr);// 缓冲字符流
            int s = -1;
            while ((s = br.read())!=-1){
                result.append((char)s);
            }
            br.close();
            isr.close();
            fis.close();
            return result.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    public static ArrayList<String> readLines(String path) {
        ArrayList<String> result = new ArrayList<>();
        File f = new File(path);
        if (!f.exists()) {// 如果文件不存在
            System.out.println("(readFile) 文件："+path+" 未找到");
            return null;
        }
        FileInputStream fis = null;
        InputStreamReader isr = null;
        BufferedReader br = null;
        try {
            fis = new FileInputStream(f);// 文件字节输入流
            isr = new InputStreamReader(fis);// 字节流转字符流
            br = new BufferedReader(isr);// 缓冲字符流
            String s;
            while ((s = br.readLine())!=null){
                if(s.length()>0) result.add(s);
            }
            br.close();
            isr.close();
            fis.close();
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void writeFile(String path,String content,boolean autoCreateFile){
        try {
            File f = new File(path);
            if (!f.exists()) {  // 如果文件不存在
                System.out.println("(writeFile) 文件："+path+" 未找到");
                if(autoCreateFile) {
                    f.createNewFile();
                    System.out.println("(writeFile) 已自动新建文件："+path);
                } else {
                    return;
                }
            }
            FileOutputStream fos = null;
            OutputStreamWriter osw = null;
            BufferedWriter bw = null;
            fos = new FileOutputStream(f);// 文件字节输出流
            osw = new OutputStreamWriter(fos);// 字节流转字符流
            bw = new BufferedWriter(osw);// 缓冲字符流
            bw.write(content);
            bw.flush();// 字符流刷新
            bw.close();
            osw.close();
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void createEmptyFile(String path) {
        File f = new File(path);
        if (!f.exists()) {  // 如果文件不存在
            try {
                f.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println("(createEmptyFile) 已新建文件：" + path);
        } else {
            System.out.println("(createEmptyFile) 文件已存在，创建失败：" + path);
        }
    }

}

