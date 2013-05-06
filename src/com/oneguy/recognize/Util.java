package com.oneguy.recognize;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

public class Util {
	public static HashMap<String, Integer> increaseValue(
			HashMap<String, Integer> map, String key) {
		if (map == null) {
			return null;
		}
		int value = 0;
		if (map.containsKey(key)) {
			value = map.get(key);
		}
		map.put(key, value + 1);
		return map;
	}
	
	public static void ensureFileExsit(String filename) {
		try {
			File f = new File(filename);
			if (!f.getParentFile().exists()) {
				f.getParentFile().mkdir();
			}
			if (!f.exists()) {
				f.createNewFile();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
