package com.oneguy.recognize.dic;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

public class ChineseArpabetMaker implements ArpabetMaker {

	@Override
	public String convert(String input) {
		if (input == null || input.length() == 0) {
			return "";
		}
		StringBuilder pinyin = new StringBuilder();
		for (int i = 0; i < input.length(); i++) {
			String[] pinyinArray = convertPinyin(input.charAt(i));
			String[] arpabetArray = convertArpabet(pinyinArray);
			if (arpabetArray != null && arpabetArray.length != 0) {
				// TODO
				// for those with multi pronunciation, we should generate all
				// possibility
				String arpabet = arpabetArray[0];
				if(arpabet!=null&&arpabet.trim().length()!=0){
					pinyin.append(arpabet.trim());
					pinyin.append(" ");
				}
			}
		}
		return pinyin.toString().trim();
	}

	private String[] convertArpabet(String[] pinyinArray) {
		return PinyinToArpabetResource.getInstance()
				.toArpabetArray(pinyinArray);
	}

	private String[] convertPinyin(char c) {
		HanyuPinyinOutputFormat format = new HanyuPinyinOutputFormat();
		format.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
		String[] pinyinArray;
		try {
			pinyinArray = PinyinHelper.toHanyuPinyinStringArray(c, format);
			return pinyinArray;
		} catch (BadHanyuPinyinOutputFormatCombination e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static void main(String[] args){
		ChineseArpabetMaker maker = new ChineseArpabetMaker();
		String output = maker.convert("王永利 许国飞 上海");
		System.out.println(output);
	}

}
