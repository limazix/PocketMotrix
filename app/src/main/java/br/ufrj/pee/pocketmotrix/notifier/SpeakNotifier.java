package br.ufrj.pee.pocketmotrix.notifier;

import java.util.Locale;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.EBean.Scope;
import org.androidannotations.annotations.RootContext;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.util.Log;
import br.ufrj.pee.pocketmotrix.R;
import br.ufrj.pee.pocketmotrix.listener.SpeakerListener;

@EBean(scope = Scope.Singleton)
public class SpeakNotifier extends AbstractNotifier implements OnInitListener {

	private static final String TAG = SpeakNotifier_.class.getName();
	private TextToSpeech mTTS;
	
	private SpeakerListener listener;
	
	@RootContext
	Context context;
	
	@Override
	public void setupNotifier() {
		mTTS = new TextToSpeech(context, this);
	}
	
	@Override
	public void onInit(int status) {
		Log.i(TAG, "oninit");
		if (status == TextToSpeech.SUCCESS) {
			int result = mTTS.setLanguage(Locale.getDefault());
			if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
				listener.onSpeakerError(context.getString(R.string.lang_not_available));
				Log.i(TAG, context.getString(R.string.lang_not_available));
			} else {
				setReady(true);
				listener.onSpeakerReady();
			}
		} else {
			String errorMessage = "Could not initialize TextToSpeech.";
			listener.onSpeakerError(errorMessage);
			Log.e(TAG, errorMessage);
		}
	}

	public void finishEngine() {
		mTTS.stop();
	}

	public void setListener(SpeakerListener listener) {
		this.listener = listener;
	}

	@SuppressWarnings("deprecation")
	@Override
	public void notifyUser(String message) {
		mTTS.speak(message, TextToSpeech.QUEUE_ADD, null);
	}
	
	
}
