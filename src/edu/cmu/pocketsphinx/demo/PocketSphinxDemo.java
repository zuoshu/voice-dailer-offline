package edu.cmu.pocketsphinx.demo;

import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.localrecognize.FileInfoActivity;
import com.example.localrecognize.R;
import com.oneguy.recognize.ContactManager;
import com.oneguy.recognize.ContactManagerImpl;
import com.oneguy.recognize.RecognizeFileManager;
import com.oneguy.recognize.RecognizeFileManagerImpl;

public class PocketSphinxDemo extends Activity implements OnTouchListener,
		RecognitionListener {
	static {
		System.loadLibrary("pocketsphinx_jni");
	}
	/**
	 * Recognizer task, which runs in a worker thread.
	 */
	RecognizerTask rec;
	/**
	 * Thread in which the recognizer task runs.
	 */
	Thread rec_thread;
	/**
	 * Time at which current recognition started.
	 */
	Date start_date;
	/**
	 * Number of seconds of speech.
	 */
	float speech_dur;
	/**
	 * Are we listening?
	 */
	boolean listening;
	/**
	 * Progress dialog for final recognition.
	 */
	ProgressDialog rec_dialog;
	/**
	 * Performance counter view.
	 */
	TextView performance_text;
	/**
	 * Editable text view.
	 */
	EditText edit_text;

	/**
	 * Respond to touch events on the Speak button.
	 * 
	 * This allows the Speak button to function as a "push and hold" button, by
	 * triggering the start of recognition when it is first pushed, and the end
	 * of recognition when it is released.
	 * 
	 * @param v
	 *            View on which this event is called
	 * @param event
	 *            Event that was triggered.
	 */

	// private FileManager fileManager;
	private RecognizeFileManager mRecognizeFileManager;
	private TextView info;
	private EditText inputName;
	private ContactManager mContactManager;

	public boolean onTouch(View v, MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			start_date = new Date();
			this.listening = true;
			this.rec.start();
			break;
		case MotionEvent.ACTION_UP:
			Date end_date = new Date();
			long nmsec = end_date.getTime() - start_date.getTime();
			this.speech_dur = (float) nmsec / 1000;
			if (this.listening) {
				Log.d(getClass().getName(), "Showing Dialog");
				this.rec_dialog = ProgressDialog.show(PocketSphinxDemo.this,
						"", "Recognizing speech...", true);
				this.rec_dialog.setCancelable(false);
				this.listening = false;
			}
			this.rec.stop();
			break;
		default:
			;
		}
		/* Let the button handle its own state */
		return false;
	}

	/** Called when the activity is first created. */
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mRecognizeFileManager = new RecognizeFileManagerImpl(this);
//		mRecognizeFileManager.initializeWithTestData();
		mContactManager = new ContactManagerImpl(this);
		initWithContacts();
		info = (TextView) findViewById(R.id.info);
		inputName = (EditText) findViewById(R.id.inputName);
		updateListInfo();

		this.rec = new RecognizerTask();
		this.rec_thread = new Thread(this.rec);
		this.listening = false;
		Button b = (Button) findViewById(R.id.Button01);
		b.setOnTouchListener(this);
		this.performance_text = (TextView) findViewById(R.id.PerformanceText);
		this.edit_text = (EditText) findViewById(R.id.EditText01);
		this.rec.setRecognitionListener(this);
		this.rec_thread.start();

		Button add = (Button) findViewById(R.id.add);
		add.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				String word = inputName.getText().toString();
				mRecognizeFileManager.addWord(word);
				updateListInfo();
				showExitTip();
			}

		});

		Button reset = (Button) findViewById(R.id.reset);
		reset.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mRecognizeFileManager.initializeWithTestData();
				updateListInfo();
				showExitTip();
			}
		});

		Button showName = (Button) findViewById(R.id.showNames);
		showName.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent i = new Intent(PocketSphinxDemo.this,
						FileInfoActivity.class);
				i.putExtra("name", "name");
				PocketSphinxDemo.this.startActivity(i);
			}
		});

		Button showDic = (Button) findViewById(R.id.showDic);
		showDic.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent i = new Intent(PocketSphinxDemo.this,
						FileInfoActivity.class);
				i.putExtra("name", "dic");
				PocketSphinxDemo.this.startActivity(i);
			}
		});

		Button showLm = (Button) findViewById(R.id.showLm);
		showLm.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent i = new Intent(PocketSphinxDemo.this,
						FileInfoActivity.class);
				i.putExtra("name", "lm");
				PocketSphinxDemo.this.startActivity(i);
			}
		});

		Button initContacts = (Button) findViewById(R.id.initContacts);
		initContacts.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				initWithContacts();
			}
		});
	}

	private void initWithContacts(){
		List<String> contacts = mContactManager.readContact();
		mRecognizeFileManager.initialize(contacts);
	}
	private void showExitTip() {
		Toast.makeText(PocketSphinxDemo.this, "重启程序生效", Toast.LENGTH_SHORT)
				.show();
	}

	/** Called when partial results are generated. */
	public void onPartialResults(Bundle b) {
		final PocketSphinxDemo that = this;
		final String hyp = b.getString("hyp");
		that.edit_text.post(new Runnable() {
			public void run() {
				that.edit_text.setText(hyp);
			}
		});
	}

	/** Called with full results are generated. */
	public void onResults(Bundle b) {
		final String hyp = b.getString("hyp");
		final PocketSphinxDemo that = this;
		this.edit_text.post(new Runnable() {
			public void run() {
				that.edit_text.setText(hyp);
				Date end_date = new Date();
				long nmsec = end_date.getTime() - that.start_date.getTime();
				float rec_dur = (float) nmsec / 1000;
				that.performance_text.setText(String.format(
						"%.2f seconds %.2f xRT", that.speech_dur, rec_dur
								/ that.speech_dur));
				Log.d(getClass().getName(), "Hiding Dialog");
				that.rec_dialog.dismiss();
			}
		});
	}

	public void onError(int err) {
		final PocketSphinxDemo that = this;
		that.edit_text.post(new Runnable() {
			public void run() {
				that.rec_dialog.dismiss();
			}
		});
	}

	private void updateListInfo() {
		String dic = mRecognizeFileManager.getDic();
		if (!TextUtils.isEmpty(dic)) {
			info.setText(dic);
		}
	}
}