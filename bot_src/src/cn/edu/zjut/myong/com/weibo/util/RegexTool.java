package cn.edu.zjut.myong.com.weibo.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexTool {

    public static String findFirst(String text, String reg) {
        Pattern ptn = Pattern.compile(reg);
        Matcher mth = ptn.matcher(text);
        if (mth.find()) {
            return mth.group();
        } else {
            return null;
        }
    }

    public static String findFirstAndSubstring(String text, String reg, int beginDelta) {
        return findFirstAndSubstring(text, reg, beginDelta, 0);
    }

    public static String findFirstAndSubstring(String text, String reg, int beginDelta, int endDelta) {
        String str = findFirst(text, reg);
        if (str == null) {
            return null;
        } else {
            return str.substring(beginDelta, str.length()-endDelta);
        }
    }
}
