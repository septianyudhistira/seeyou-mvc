package com.seeyou.mvc.utils;

/**
 * @author Septian Yudhistira
 * @version 1.0
 * @since 2025-01-06
 */
public class KeyGen {
    public static String getRandomKey(int length) {
        String base = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String randomkey = new String();

        for(int i = 0; i < length; ++i) {
            randomkey = randomkey + "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".charAt((int)Math.floor(Math.random() * (double)"abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".length()));
        }

        return randomkey;
    }
}
