package com.framework.utils;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

public class PinYinUtil {
    public static void main(String[] args) {

        System.out.println(getPinyin("中国红123")); //--zhongguohong123
        System.out.println(getPinyin("dalian")); //--zhongguohong123
    }

    /**
     * @param china (string Chinese characters)
     * @return Chinese characters to pinyin, other characters remain unchanged
     */
    public static String getPinyin(String china){
        HanyuPinyinOutputFormat formart = new HanyuPinyinOutputFormat();
        formart.setCaseType(HanyuPinyinCaseType.LOWERCASE);
        formart.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
        formart.setVCharType(HanyuPinyinVCharType.WITH_V);
        char[] arrays = china.trim().toCharArray();
        String result = "";
        try {
            for (int i=0;i<arrays.length;i++) {
                char ti = arrays[i];
                if(Character.toString(ti).matches("[\\u4e00-\\u9fa5]")){ //Whether the match is Chinese
                    String[] temp = PinyinHelper.toHanyuPinyinStringArray(ti,formart);
                    result += temp[0];
                }else{
                    result += ti;
                }
            }
        } catch (BadHanyuPinyinOutputFormatCombination e) {
            e.printStackTrace();
        }

        return result;
    }
}