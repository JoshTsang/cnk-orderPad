package com.htb.cnk.lib;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

public class HanziHelper {
	
	private static HanyuPinyinOutputFormat format= new HanyuPinyinOutputFormat();
	
	static {
		format.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
		format.setVCharType(HanyuPinyinVCharType.WITH_U_UNICODE);
		format.setCaseType(HanyuPinyinCaseType.LOWERCASE);
	}
	
	public static String char2Pinyin(char c) {
		String[] pinyin = null;
		try {
			pinyin = PinyinHelper.toHanyuPinyinStringArray(c, format);
        } catch(BadHanyuPinyinOutputFormatCombination e) {
            e.printStackTrace();
        }
        if(pinyin == null) {
        	return Character.toString(c);
        } else {
        	return pinyin[0];
        }
	}
	
	public static String words2Pinyin(String words) {
		StringBuilder sb = new StringBuilder();
		char[] chars = words.toCharArray();
		for(int i = 0, length = chars.length; i < length; i++) {
			sb.append(char2Pinyin(chars[i]));
		}
		return sb.toString();
	}
    
}
