package br.ufrj.pee.pocketmotrix.engine;

import java.util.Locale;
import java.util.Observable;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.androidannotations.annotations.EBean.Scope;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.util.Log;

@EBean(scope = Scope.Singleton)
public class TTSEngine extends Observable implements OnInitListener {

	private static final String TAG = TTSEngine_.class.getName();
	private TextToSpeech mTTS;
	private Boolean isInitialized = false;
	
	@RootContext
	Context context;
	
	@AfterInject
	public void setupEngine() {
		Log.i(TAG, "Setup");
		mTTS = new TextToSpeech(context, this);
	}
	
	@SuppressWarnings("deprecation")
	public void speakToUser(String msg) {
		Log.i(TAG, "Speaking: " + msg);
		if(isInitialized)
			mTTS.speak(msg, TextToSpeech.QUEUE_ADD, null);
		else
			Log.i(TAG, "Not initialized");
	}

	@Override
	public void onInit(int status) {
		Log.i(TAG, "oninit");
		if (status == TextToSpeech.SUCCESS) {
			int result = mTTS.setLanguage(Locale.US);
			if (result == TextToSpeech.LANG_MISSING_DATA
					|| result == TextToSpeech.LANG_NOT_SUPPORTED) {
				Log.i(TAG, "Language is not available.");
			} else {
				setIsInitialized(true);
				Log.i(TAG, "Successful Initialized");
			}
		} else {
			Log.i(TAG, "Could not initialize TextToSpeech.");
		}
	}

	public Boolean getIsInitialized() {
		return isInitialized;
	}

	public void setIsInitialized(Boolean isInitialized) {
		this.isInitialized = isInitialized;
		setChanged();
		notifyObservers();
	}
	
	public void finishEngine() {
		Log.i(TAG, "Stop");
//		mTTS.stop();
	}
	
}
