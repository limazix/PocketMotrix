package br.ufrj.pee.pocketmotrix.service;

import java.util.Locale;

import org.androidannotations.annotations.EService;

import android.accessibilityservice.AccessibilityService;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

@EService
public class PocketMotrixService extends AccessibilityService implements
		OnInitListener {

	private static final String TAG = PocketMotrixService_.class.getName();
	private TextToSpeech mTTS;
	
	@Override
	public void onServiceConnected() {
		Log.i(TAG, "PocketMotrixService Connected");
		startTTS();
	}

	@Override
	public void onAccessibilityEvent(AccessibilityEvent event) {
		// TODO Auto-generated method stub
		Log.i(TAG, "Accessibility Event");
		speakToUser("This is a test");
	}

	@Override
	public void onInterrupt() {
		// TODO Auto-generated method stub

	}

	private void startTTS() {
		Log.i(TAG, "Start TTS");
		mTTS = new TextToSpeech(getApplicationContext(), this);
	}

	@SuppressWarnings("deprecation")
	private void speakToUser(String msg) {
		Log.i(TAG, "Speaking: " + msg);
		mTTS.speak(msg, TextToSpeech.QUEUE_FLUSH, null);
	}

	@Override
	public void onInit(int status) {
		Log.v(TAG, "oninit");
		if (status == TextToSpeech.SUCCESS) {
			int result = mTTS.setLanguage(Locale.US);
			if (result == TextToSpeech.LANG_MISSING_DATA
					|| result == TextToSpeech.LANG_NOT_SUPPORTED) {
				Log.v(TAG, "Language is not available.");
			}
		} else {
			Log.v(TAG, "Could not initialize TextToSpeech.");
		}
	}

}
