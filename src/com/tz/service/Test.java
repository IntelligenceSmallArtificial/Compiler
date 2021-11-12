package com.tz.service;

import static com.tz.service.Util.split;

public class Test {
    private void t(String s){
        s="1";
    }

    public static void main(String[] args) {
        String a="m";
        new Test().t(a);
        System.out.println(a);
    }
}
