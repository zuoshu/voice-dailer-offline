package com.example.localrecognize;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.TextView;

public class FileInfoActivity extends Activity {

	private TextView mInfoView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_file);
		Intent from = getIntent();
		if (from == null) {
			return;
		}
		String file = from.getStringExtra("name");
		mInfoView = (TextView) findViewById(R.id.info);
		updateFileInfo(file);
	}

	private void updateFileInfo(String file) {
		String completeFilename;
		if (file.equals("dic")) {
			completeFilename = getFilesDir() + "/names.dic";
		} else if (file.equals("lm")) {
			completeFilename = getFilesDir() + "/names.lm";
		} else {
			completeFilename = getFilesDir() + "/names.txt";
		}
		String fileContent = FileManager.getInstance(this).readFileContent(
				completeFilename);
		if (!TextUtils.isEmpty(fileContent)) {
			mInfoView.setText(fileContent);
		}
	}
}
