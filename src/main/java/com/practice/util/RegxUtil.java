package com.practice.util;

import java.util.regex.Matcher;

import static java.util.regex.Pattern.compile;

public class RegxUtil {
    public static final String REG_DIGITAL = "[0-9]";
    public static final String REG_Blank = "";
    public static final String REG_ZERO = "0";
    public static final String PATH_SPLIT = "/";
    public static final String STRIP_SPLIT = "-";
    public static final String COMMA_SPLIT = ",";

    public static final String REG_ZH = "^[\\u4e00-\\u9fa5]";
    public static final String REG_TX = "(?<=\\[)(.+?)(?=\\])";

    /*public static final String REG_PDF = ".(pdf)$/";
    public static final String REG_JPG = ".(jpg)$/";
    //"\\w+\\.(jpg|gif|bmp|png).*";*/

    public static final String FILE_PDF = ".pdf";
    public static final String FILE_DOC = ".doc";
    public static final String FILE_JPG = ".jpg";
    public static final String FILE_XML = ".xml";

    public static Matcher getMatcher(String word, String regx) {
        return compile(regx).matcher(word);
    }

    public static boolean isMatch(String regex, CharSequence input) {
        return input != null && input.length() > 0 && compile(regex).matcher(input).find();
    }

    public static String replaceAllToPath(String dateStr, Boolean isDelete) {
        String[] split = dateStr.split(STRIP_SPLIT);
        dateStr = split[0].concat(PATH_SPLIT);
        if (isDelete && split[1].startsWith("0")) {
            dateStr += split[1].replace(RegxUtil.REG_ZERO, RegxUtil.REG_Blank).concat(PATH_SPLIT);
        } else {
            dateStr += split[1].concat(PATH_SPLIT);
        }
        if (isDelete && split[2].startsWith("0")) {
            dateStr += split[2].replace(RegxUtil.REG_ZERO, RegxUtil.REG_Blank);
        } else {
            dateStr += split[2];
        }
        return dateStr;
       // return dateStr.replaceAll(STRIP_SPLIT, PATH_SPLIT);
    }
    public static void main(String[] args) {
        String word = "2015-12-10";
        String word_zh = "message [username]]; default message [参数userName缺失]\\nField error in object 'user' on field 'password':" +
                "default message [密码不能为空]\\nField error in object 'user' on field 'password' [密码参数缺失]\\nField error in" +
                " object 'user' on field 'username': rejected value [null]; default message [username]]; default message [用户名不能为空]";
        //printMatch(word, REG_DIGITAL);
        //String s = extractTargetBetweenSymbolRange(word_zh, REG_ZH, REG_TX);
        /*boolean match = isMatch("123.jpg", REG_JPG);
        boolean match2 = isMatch("123jpg", REG_JPG);*/
    }

    public static String extractTargetBetweenSymbolRange(String input, String targetRegx, String symRange) {
        StringBuffer extract = new StringBuffer();
        Matcher matcher = getMatcher(input, symRange);
        while (matcher.find()) {
            if (isMatch(targetRegx, matcher.group())) {
                extract.append(matcher.group()).append("; ");
            }
        }
        return extract.substring(0, extract.length() - 2);
    }

    private static void printMatch(String word, String regx) {
        Matcher matcher = getMatcher(word, regx);
        while (matcher.find()) {
            if (isMatch(REG_ZH, matcher.group())) {
                System.out.println(matcher.group());
            }
        }
    }

}
