package com.oneguy.recognize.dic;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import com.example.localrecognize.MyApplication;

public class PinyinToArpabetResource {
	private static PinyinToArpabetResource instance;
	private Properties unicodeToArpabetTable = null;

	public static synchronized PinyinToArpabetResource getInstance() {
		if (instance == null) {
			instance = new PinyinToArpabetResource();
		}
		return instance;
	}

	private PinyinToArpabetResource() {
		loadResource();
	}

	private void loadResource() {
		try {
			final String resourceName = "chinese_arpabet_table.txt";

			setUnicodeToArpabetTable(new Properties());
			getUnicodeToArpabetTable().load(
					MyApplication.getContext().getAssets().open(resourceName)

			);
		} catch (FileNotFoundException ex) {
			ex.printStackTrace();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	private void setUnicodeToArpabetTable(Properties unicodeToArpabetTable) {
		this.unicodeToArpabetTable = unicodeToArpabetTable;
	}

	private Properties getUnicodeToArpabetTable() {
		return unicodeToArpabetTable;
	}

	public String[] toArpabetArray(String[] pinyinArray) {
		if (pinyinArray == null || pinyinArray.length == 0) {
			return null;
		}
		String[] arpebetArray = new String[pinyinArray.length];
		for (int i = 0; i < pinyinArray.length; i++) {
			arpebetArray[i] = toArpabet(pinyinArray[i]);
		}
		return arpebetArray;
	}

	public String toArpabet(String pinyin) {
		return getUnicodeToArpabetTable().getProperty(pinyin, "");
	}

	public static void main(String[] args) {
		PinyinToArpabetResource resource = PinyinToArpabetResource
				.getInstance();
		String input = "wang yong li shang hai xu guo fei";
		String[] inputArray = input.split(" ");
		for (String s : inputArray) {
			System.out.println(resource.toArpabet(s));
		}
	}
}
