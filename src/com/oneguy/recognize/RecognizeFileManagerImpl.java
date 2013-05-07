package com.oneguy.recognize;

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

import com.example.localrecognize.MyApplication;
import com.oneguy.recognize.dic.DictionaryMaker;
import com.oneguy.recognize.lm.LmMaker;

import android.content.Context;
import android.text.TextUtils;

public class RecognizeFileManagerImpl implements RecognizeFileManager {

	private Context mContext;
	private static final String WORDS_FILENAME = "/words.txt";
	private static final String LM_FILENAME = "/words.lm";
	private static final String DIC_FILENAME = "/words.dic";
	private static final String HMM_DIR_NAME = "/tdt_sc_8k";
	private static final String TEST_DATA_FILENAME = "test_names.txt";

	public RecognizeFileManagerImpl(Context c) {
		mContext = c;
	}

	@Override
	public void addWord(String word) {
		List<String> words = getWords();
		if (words == null) {
			words = new LinkedList<String>();
		}
		words.add(word);
		initialize(words);
	}

	@Override
	public void initialize(List<String> words) {
		String filename = mContext.getFilesDir() + WORDS_FILENAME;
		writeList(words, filename);

		String dicFilename = mContext.getFilesDir() + DIC_FILENAME;
		generateDic(words, dicFilename);

		String lmFilename = mContext.getFilesDir() + LM_FILENAME;
		generateLm(words, lmFilename);

	}

	@Override
	public void initializeWithTestData() {
		List<String> originWords = readListFromAssert(TEST_DATA_FILENAME);
		List<String> filtedWords = new LinkedList<String>();
		if (originWords == null) {
			return;
		}
		for (String name : originWords) {
			// comment starts with "//"
			if (!name.startsWith("//") && !TextUtils.isEmpty(name)) {
				filtedWords.add(name);
			}
		}
		initialize(filtedWords);
	}

	@Override
	public List<String> getWords() {
		String filename = mContext.getFilesDir() + WORDS_FILENAME;
		return readListFromFile(filename);
	}

	@Override
	public String getWordsInLineString() {
		List<String> words = getWords();
		if (words == null || words.size() == 0) {
			return "";
		}
		StringBuilder sb = new StringBuilder();
		for (String s : words) {
			sb.append(s);
			sb.append("\n");
		}
		return sb.toString();
	}

	@Override
	public String getDic() {
		String dicFilename = mContext.getFilesDir() + DIC_FILENAME;
		return readFileContent(dicFilename);
	}

	@Override
	public String getLm() {
		String lmFilename = mContext.getFilesDir() + LM_FILENAME;
		return readFileContent(lmFilename);
	}

	private void writeList(List<String> list, String filename) {
		if (list == null || list.size() == 0) {
			return;
		}
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

	private void generateDic(List<String> words, String filename) {
		DictionaryMaker dicMaker = new DictionaryMaker();
		dicMaker.makeDictionary(words, filename);
	}

	private void generateLm(List<String> words, String lmFilename) {
		LmMaker lmMaker = new LmMaker();
		try {
			lmMaker.makeLM(words, lmFilename);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private String readFileContent(String filename) {
		File file = new File(filename);
		if (!file.exists()) {
			return "";
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
		return "";
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

	public void copyHmmFilesIfNotExsit() {
		String hmmFileDir = mContext.getFilesDir() + HMM_DIR_NAME;
		if (!isFileExsit(hmmFileDir)) {
			copyHmmFiles();
		}
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

	public boolean isFileExsit(String filename) {
		File file = new File(filename);
		return file.exists();
	}

}
