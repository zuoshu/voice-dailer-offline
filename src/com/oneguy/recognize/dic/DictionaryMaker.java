package com.oneguy.recognize.dic;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.oneguy.recognize.Util;

public class DictionaryMaker {
	private ArpabetMaker maker;

	public void setArpabetMaker(ArpabetMaker maker) {
		if (maker == null) {
			throw new IllegalArgumentException("ArpabetMaker null!");
		}
		this.maker = maker;
	}

	public DictionaryMaker() {
		maker = new ChineseArpabetMaker();
	}

	public void makeDictionary(List<String> words, String outputFilename) {
		List<String> convertWords = null;
		if (words != null && words.size() != 0) {
			convertWords = new ArrayList<String>(words.size());
			for (int i = 0; i < words.size(); i++) {
				convertWords.add(maker.convert(words.get(i)));
			}
		}
		BufferedWriter bw = null;
		try {
			Util.ensureFileExsit(outputFilename);
			bw = new BufferedWriter(new FileWriter(outputFilename));
			if (convertWords != null && convertWords.size() != 0) {
				for (int i = 0; i < words.size(); i++) {
					if (words.get(i) == null
							|| words.get(i).trim().length() == 0) {
						continue;
					}
					bw.write(words.get(i));
					bw.write("\t");
					bw.write(convertWords.get(i));
					if (i != words.size() - 1) {
						bw.write("\r\n");
					}
					bw.flush();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (bw != null) {
				try {
					bw.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
