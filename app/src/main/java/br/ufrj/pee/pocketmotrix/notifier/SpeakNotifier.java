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

@EBean(scope = Scope.Singleton)
public class SpeakNotifier extends AbstractNotifier implements OnInitListener {

	private static final String TAG = SpeakNotifier_.class.getName();
	private TextToSpeech mTTS;

	@RootContext
	Context context;
	
	@Override
	public void setupNotifier() {
		mTTS = new TextToSpeech(context, this);
	}
	
	@Override
	public void onInit(int status) {
		if (status == TextToSpeech.SUCCESS) {
			int result = mTTS.setLanguage(Locale.getDefault());
			if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
				getListener().onNotifierError(context.getString(R.string.lang_not_available));
				Log.i(TAG, context.getString(R.string.lang_not_available));
			} else {
				setReady(true);
			    getListener().onNotifierReady(this);
			}
		} else {
			String errorMessage = context.getString(R.string.notifier_speak_couldnt_initialize_tts);
			getListener().onNotifierError(errorMessage);
			Log.e(TAG, errorMessage);
		}
	}

	public void finishEngine() {
		mTTS.stop();
	}

	@SuppressWarnings("deprecation")
	@Override
	public void notifyUser(String message) {
		mTTS.speak(message, TextToSpeech.QUEUE_ADD, null);
	}
}
