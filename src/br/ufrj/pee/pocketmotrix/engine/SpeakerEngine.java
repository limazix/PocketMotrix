package br.ufrj.pee.pocketmotrix.engine;

import java.util.Locale;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.EBean.Scope;
import org.androidannotations.annotations.RootContext;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.util.Log;
import br.ufrj.pee.pocketmotrix.listener.SpeakerListener;

@EBean(scope = Scope.Singleton)
public class SpeakerEngine implements OnInitListener {

	private static final String TAG = SpeakerEngine_.class.getName();
	private TextToSpeech mTTS;
	private Boolean isInitialized = false;
	
	private SpeakerListener listener;
	
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
			Log.e(TAG, "Not initialized");
	}

	@Override
	public void onInit(int status) {
		Log.i(TAG, "oninit");
		if (status == TextToSpeech.SUCCESS) {
			int result = mTTS.setLanguage(Locale.getDefault());
			if (result == TextToSpeech.LANG_MISSING_DATA
					|| result == TextToSpeech.LANG_NOT_SUPPORTED) {
				Log.i(TAG, "Language is not available.");
			} else {
				setIsInitialized(true);
			}
		} else {
			String errorMessage = "Could not initialize TextToSpeech.";
			listener.onSpeakerError(errorMessage);
			Log.e(TAG, errorMessage);
		}
	}

	public Boolean getIsInitialized() {
		return isInitialized;
	}

	public void setIsInitialized(Boolean isInitialized) {
		this.isInitialized = isInitialized;
		listener.onSpeakerReady();
	}
	
	public void finishEngine() {
		mTTS.stop();
	}

	public void setListener(SpeakerListener listener) {
		this.listener = listener;
	}
	
	
}
