package com.rpc.common.util;

public class StringUtils {

    public static boolean isEmpty(String s){
        return s == null || s.isEmpty();
    }

    public static boolean isNotBlank(String s){
        return !isEmpty(s);
    }
}
