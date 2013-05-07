package com.oneguy.recognize;

import java.util.List;

public interface RecognizeFileManager {
	public void addWord(String word);
	public void	initialize(List<String> words);
	public void initializeWithTestData();
	public List<String> getWords();
	public String getWordsInLineString();
	public String getDic();
	public String getLm();
}
