package com.example.localrecognize;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.TextView;

import com.oneguy.recognize.RecognizeFileManager;
import com.oneguy.recognize.RecognizeFileManagerImpl;

public class FileInfoActivity extends Activity {

	private TextView mInfoView;
	private RecognizeFileManager mRecognizeFileManager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_file);
		mRecognizeFileManager = new RecognizeFileManagerImpl(this);
		Intent from = getIntent();
		if (from == null) {
			return;
		}
		String file = from.getStringExtra("name");
		mInfoView = (TextView) findViewById(R.id.info);
		updateFileInfo(file);
	}

	private void updateFileInfo(String file) {
		String fileContent;
		if (file.equals("dic")) {
			fileContent = mRecognizeFileManager.getDic();
		} else if (file.equals("lm")) {
			fileContent = mRecognizeFileManager.getLm();
		} else {
			fileContent = mRecognizeFileManager.getWordsInLineString();

		}
		if (!TextUtils.isEmpty(fileContent)) {
			mInfoView.setText(fileContent);
		}
	}
}
