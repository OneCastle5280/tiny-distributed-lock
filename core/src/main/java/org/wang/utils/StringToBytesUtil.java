package org.wang.utils;

import java.io.UnsupportedEncodingException;

/**
 * @author wangjiabao
 */
public class StringToBytesUtil {

    private StringToBytesUtil(){}

    public static byte[] convertWithUTF8(String input) throws UnsupportedEncodingException {
        if (input == null || "".equals(input)) {
            return null;
        }

        return input.getBytes("UTF-8");
    }
}
