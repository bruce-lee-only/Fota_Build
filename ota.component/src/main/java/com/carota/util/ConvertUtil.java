package com.carota.util;

import java.util.regex.Pattern;

public class ConvertUtil {
    public static Long toHex(String s) {
        String regHex = "-?[0-9a-fA-F]+";
        Pattern r = Pattern.compile(regHex);
        if (s.startsWith("0x")) {
            s = s.substring(2);
        }
        if(r.matcher(s).matches()){
            return Long.valueOf(s, 16);
        } else {
//            throw new Exception("not hex", null);
            return -1L;
        }
    }

    public static int int2Hex(String s) {
        String regHex = "-?[0-9a-fA-F]+";
        Pattern r = Pattern.compile(regHex);
        if (s.startsWith("0x")) {
            s = s.substring(2);
        }
        if(r.matcher(s).matches()){
            return Integer.valueOf(s, 16);
        } else {
//            throw new Exception("not hex", null);
            return -1;
        }
    }
}

