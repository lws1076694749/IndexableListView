package com.lenovo.myapplication;

/**
 * Created by lenovo on 2015/9/19.
 */
public class StringMatcher {
    public static boolean match(String value, String keyword) {
        if (value == null || keyword == null) {
            return false;
        }
        if (value.length() < keyword.length()) {
            return false;
        }
        if (value.contains(keyword)) {
            return true;
        } else {
            return false;
        }
    }
}
