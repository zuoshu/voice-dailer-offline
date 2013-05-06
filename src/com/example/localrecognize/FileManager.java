package com.example.localrecognize;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.LinkedList;
import java.util.List;

import android.content.Context;
import android.text.TextUtils;

import com.oneguy.recognize.Util;
import com.oneguy.recognize.dic.DictionaryMaker;
import com.oneguy.recognize.lm.LmMaker;

public class FileManager {
	private static FileManager instance = null;
	private Context mContext;
	private static final String NAME_FILENAME = "/names.txt";
	private static final String LM_FILENAME = "/names.lm";
	private static final String DIC_FILENAME = "/names.dic";
	private static final List<String> DEFAULT_LIST = new LinkedList<String>();
	private List<String> mNameList;

	public synchronized static FileManager getInstance(Context context) {
		if (instance == null) {
			instance = new FileManager(context);
		}
		return instance;
	}

	private FileManager(Context context) {
		mContext = context;
		loadTestDataDefault();
		mNameList = null;
	};

	private void loadTestDataDefault() {
		List<String> names = readListFromAssert("test_names.txt");
		if (names == null) {
			return;
		}
		for (String name : names) {
			// comment starts with "//"
			if (!name.startsWith("//") && !TextUtils.isEmpty(name)) {
				DEFAULT_LIST.add(name);
			}
		}
	}

	public void alwaysInitFile() {
		mNameList = DEFAULT_LIST;
		writeList(mNameList);
	}
	
	public void initFileIfNotExsit() {
		List<String> list = readNameList();
		if (list != null && list.size() != 0) {
			return;
		}
		mNameList = DEFAULT_LIST;
		writeList(mNameList);
	}

	private void writeList(List<String> list) {
		if (list == null || list.size() == 0) {
			return;
		}
		String filename = mContext.getFilesDir() + NAME_FILENAME;
		BufferedWriter bw = null;
		try {
			bw = new BufferedWriter(new FileWriter(filename));
			for (String s : list) {
				bw.write(s);
				bw.write("\n");
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

	public void appendLine(String line) {
		if (line == null || line.trim().length() == 0) {
			return;
		}
		List<String> list = readNameList();
		if (list == null) {
			list = new LinkedList<String>();
		}
		list.add(line);
		writeList(list);
	}

	public void generateLmAndDic() {
		List<String> list = readNameList();
		if (list == null || list.size() == 0) {
			return;
		}
		String lmFilename = mContext.getFilesDir() + LM_FILENAME;
		// String lmFilename = "/sdcard/Android/data/test/names.lm";
		LmMaker lmMaker = new LmMaker();
		try {
			lmMaker.makeLM(list, lmFilename);
		} catch (IOException e) {
			e.printStackTrace();
		}
		String dicFilename = mContext.getFilesDir() + DIC_FILENAME;
		// String dicFilename = "/sdcard/Android/data/test/names.dic";
		DictionaryMaker dicMaker = new DictionaryMaker();
		dicMaker.makeDictionary(list, dicFilename);
	}

	public void resetListDefault() {
		writeList(DEFAULT_LIST);
	}

	public List<String> readNameList() {
		String filename = mContext.getFilesDir() + NAME_FILENAME;
		return readListFromFile(filename);
	}
	
	public List<String> readDicList() {
		String filename = mContext.getFilesDir() + DIC_FILENAME;
		return readListFromFile(filename);
	}

	public List<String> readListFromFile(String filename) {
		Reader in = null;
		try {
			in = new InputStreamReader(new FileInputStream(filename));
			return readList(in);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return null;
	}

	public List<String> readListFromAssert(String filename) {
		Reader in = null;
		try {
			in = new InputStreamReader(mContext.getAssets().open(filename));
			return readList(in);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return null;
	}

	public List<String> readList(Reader in) {
		List<String> result = null;
		BufferedReader br = null;
		try {
			;
			br = new BufferedReader(in);
			result = new LinkedList<String>();
			String line = br.readLine();
			while (line != null) {
				result.add(line);
				line = br.readLine();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return result;
	}

	public void copyHmmFiles() {
		String fileDir = MyApplication.getContext().getFilesDir().getPath();
		copyAssertToLocalOnce("tdt_sc_8k/feat.params", fileDir
				+ "/tdt_sc_8k/feat.params");
		copyAssertToLocalOnce("tdt_sc_8k/mdef", fileDir + "/tdt_sc_8k/mdef");
		copyAssertToLocalOnce("tdt_sc_8k/means", fileDir + "/tdt_sc_8k/means");
		copyAssertToLocalOnce("tdt_sc_8k/noisedict", fileDir
				+ "/tdt_sc_8k/noisedict");
		copyAssertToLocalOnce("tdt_sc_8k/sendump", fileDir
				+ "/tdt_sc_8k/sendump");
		copyAssertToLocalOnce("tdt_sc_8k/transition_matrices", fileDir
				+ "/tdt_sc_8k/transition_matrices");
		copyAssertToLocalOnce("tdt_sc_8k/variances", fileDir
				+ "/tdt_sc_8k/variances");
	}

	public void copyAssertToLocalOnce(String from, String to) {
		if (isFileExsit(to)) {
			return;
		}
		Util.ensureFileExsit(to);
		FileOutputStream fos = null;
		InputStream is = null;
		try {
			fos = new FileOutputStream(to);
			is = MyApplication.getContext().getAssets().open(from);
			byte[] buffer = new byte[1024];
			int read = is.read(buffer);
			while (read != -1) {
				fos.write(buffer, 0, read);
				read = is.read(buffer);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public boolean isFileExsit(String filename) {
		File file = new File(filename);
		return file.exists();
	}

	public String readFileContent(String filename) {
		File file = new File(filename);
		if (!file.exists()) {
			return null;
		}
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(file));
			StringBuilder sb = new StringBuilder();
			String line = br.readLine();
			while (line != null) {
				sb.append(line);
				sb.append("\n");
				line = br.readLine();
			}
			return sb.toString();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

}
