package com.oneguy.recognize.lm;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.Collator;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import com.oneguy.recognize.Util;

public class LmMaker {
	private static final double DISCOUNT_MASS = 0.5;
	private static final double DEFLATOR = 1 - DISCOUNT_MASS;
	private static final int DEFAULT_CAPACITY = 128;

	/**
	 * Each input sentence begins with <s> and end with </s>
	 * 
	 * @param sentences
	 *            Input sentences
	 * @param outputFilename
	 *            lm output file
	 * @throws IOException
	 */
	public void makeLM(List<String> sentences, String outputFilename)
			throws IOException {
		if (sentences == null || sentences.size() == 0) {
			throw new IllegalArgumentException("Input words empty!");
		}
		if (outputFilename == null || outputFilename.trim().length() == 0) {
			throw new IllegalArgumentException("No output filename!");
		}
		// add <s> and </s>
		sentences = prepareSentence(sentences);
		HashMap<String, Integer> unigram = new HashMap<String, Integer>(
				DEFAULT_CAPACITY);
		HashMap<String, Integer> bigram = new HashMap<String, Integer>(
				DEFAULT_CAPACITY);
		HashMap<String, Integer> trigram = new HashMap<String, Integer>(
				DEFAULT_CAPACITY);
		for (String sentence : sentences) {
			String[] words = sentence.split(" ");
			for (int i = 0; i < words.length; i++) {
				// single word
				Util.increaseValue(unigram, words[i]);

				// double words
				if (i < words.length - 1) {
					String doubleWords = words[i].concat(" ").concat(
							words[i + 1]);
					Util.increaseValue(bigram, doubleWords);
				}

				// triple words
				if (i < words.length - 2) {
					String tripleWords = words[i].concat(" ")
							.concat(words[i + 1]).concat(" ")
							.concat(words[i + 2]);
					Util.increaseValue(trigram, tripleWords);
				}
			}
		}
		// compute counts
		int uniSum = 0;
		for (String key : unigram.keySet()) {
			uniSum += unigram.get(key);
		}
		BufferedWriter bw = null;
		try {
			Util.ensureFileExsit(outputFilename);
			bw = new BufferedWriter(new FileWriter(outputFilename));
			int uniCount = unigram.size();
			int biCount = bigram.size();
			int triCount = trigram.size();
			bw.write("\\data\\\n");
			bw.write("ngram 1=" + uniCount + "\n");
			if (biCount > 0) {
				bw.write("ngram 2=" + biCount + "\n");
			}
			if (triCount > 0) {
				bw.write("ngram 3=" + triCount + "\n");
			}
			bw.write("\n");

			// compute uni probs
			HashMap<String, Double> uniprob = new HashMap<String, Double>(
					DEFAULT_CAPACITY);
			for (String key : unigram.keySet()) {
				double value1 = unigram.get(key).doubleValue() / uniSum;
				uniprob.put(key, value1 * DEFLATOR);
			}

			// compute alphas
			HashMap<String, Double> alpha = new HashMap<String, Double>(
					DEFAULT_CAPACITY);
			for (String y : unigram.keySet()) {
				String w1 = y;
				double sumDenom = 0;
				for (String x : bigram.keySet()) {
					if (x.substring(0, x.lastIndexOf(" ")).equals(w1)) {
						String w2 = x.substring(x.indexOf(" ") + 1);
						sumDenom += uniprob.get(w2);
					}
				}
				alpha.put(w1, DISCOUNT_MASS / (1 - sumDenom));
			}

			bw.write("\\1-grams:\n");
			Comparator cmp = Collator.getInstance(java.util.Locale.CHINA);
			String[] uniKeys = new String[uniCount];
			uniKeys = unigram.keySet().toArray(uniKeys);
			Arrays.sort(uniKeys, cmp);
			for (String k : uniKeys) {
				DecimalFormat df = new DecimalFormat("0.0000");
				double value1 = Math.log(uniprob.get(k)) / Math.log(10);
				double value2 = Math.log(alpha.get(k)) / Math.log(10);
				bw.write(df.format(value1) + " " + k + " " + df.format(value2));
				bw.write("\n");
			}
			bw.write("\n");

			// compute bi probs
			HashMap<String, Double> biprob = new HashMap<String, Double>(
					DEFAULT_CAPACITY);
			for (String x : bigram.keySet()) {
				String w1 = x.substring(0, x.indexOf(" "));
				double value = bigram.get(x).doubleValue()
						/ unigram.get(w1).doubleValue();
				biprob.put(x, value * DEFLATOR);
			}

			// compute bialphas
			HashMap<String, Double> bialpha = new HashMap<String, Double>(
					DEFAULT_CAPACITY);
			for (String x : bigram.keySet()) {
				String w1w2 = x;
				double sumDenom = 0;
				for (String y : trigram.keySet()) {
					if (y.substring(0, y.lastIndexOf(" ")).equals(w1w2)) {
						String w2w3 = y.substring(y.indexOf(" ") + 1);
						sumDenom += biprob.get(w2w3);
					}
				}
				bialpha.put(w1w2, DISCOUNT_MASS / (1 - sumDenom));
			}
			// output the bigrams and trigrams (now that we have the alphas
			// computed).
			if (biCount > 0) {
				String[] biKeys = new String[biCount];
				biKeys = bigram.keySet().toArray(biKeys);
				Arrays.sort(biKeys, cmp);
				bw.write("\\2-grams:\n");
				for (String k : biKeys) {
					DecimalFormat df = new DecimalFormat("0.0000");
					double value1 = Math.log(biprob.get(k)) / Math.log(10);
					double value2 = Math.log(bialpha.get(k)) / Math.log(10);
					bw.write(df.format(value1) + " " + k + " "
							+ df.format(value2));
					bw.write("\n");
				}
				bw.write("\n");
			}
			if (triCount > 0) {
				String[] triKeys = new String[triCount];
				triKeys = trigram.keySet().toArray(triKeys);
				Arrays.sort(triKeys, cmp);
				bw.write("\\3-grams:\n");
				for (String x : triKeys) {
					String w1w2 = x.substring(0, x.lastIndexOf(" "));
					double value1 = trigram.get(x).doubleValue() * DEFLATOR;
					double value2 = bigram.get(w1w2).doubleValue();
					double value3 = Math.log(value1 / value2) / Math.log(10);
					DecimalFormat df = new DecimalFormat("0.0000");
					bw.write(df.format(value3) + " " + x);
					bw.write("\n");
				}
				bw.write("\n");
			}
			bw.write("\\end\\\n");
			bw.flush();
		} finally {
			if (bw != null) {
				bw.close();
			}
		}
	}

	private List<String> prepareSentence(List<String> input) {
		if (input == null || input.size() == 0) {
			return null;
		}
		List<String> output = new ArrayList<String>(input.size());
		for (int i = 0; i < input.size(); i++) {
			output.add("<s> " + input.get(i) + " </s>");
		}
		return output;
	}

}
