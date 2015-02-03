package br.ufrj.pee.pocketmotrix.service;

import java.util.Observable;
import java.util.Observer;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EService;

import android.accessibilityservice.AccessibilityService;
import android.app.Instrumentation;
import android.content.Context;
import android.media.AudioManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityEvent;
import br.ufrj.pee.pocketmotrix.engine.SREngine;
import br.ufrj.pee.pocketmotrix.engine.TTSEngine;

@EService
public class PocketMotrixService extends AccessibilityService implements
		Observer {

	private static final String TAG = PocketMotrixService_.class.getName();
	private Context context;
	private AudioManager audioManager;

	@Bean
	SREngine mSREngine;

	@Bean
	TTSEngine mTTSEngine;

	@Override
	public void onServiceConnected() {
		Log.i(TAG, "PocketMotrixService Connected");
		context = getApplicationContext();
		audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
	}

	@AfterInject
	public void settingupEngines() {
		mTTSEngine.addObserver(this);
		mSREngine.addObserver(this);
	}

	@Override
	public void onAccessibilityEvent(AccessibilityEvent event) {
		Log.i(TAG, "Accessibility Event");
		// mTTS.speakToUser("This is a test!");
	}

	@Override
	public void onDestroy() {
		mSREngine.deleteObserver(this);
		mSREngine.finishEngine();
		mTTSEngine.deleteObserver(this);
		mTTSEngine.finishEngine();
	}

	@Override
	public void update(Observable observable, Object data) {
		if (observable instanceof SREngine) {

			mTTSEngine.speakToUser(mSREngine.getResultText());

			String cmd = mSREngine.getResultText().replaceAll("\\s", "");
			execute(Command.get(cmd));

		} else if (observable instanceof TTSEngine) {
			if (mTTSEngine.getIsInitialized())
				mSREngine.setupEngine();
		}
	}

	@Override
	public void onInterrupt() {
		// TODO Auto-generated method stub

	}

	private void execute(Command cmd) {

		switch (cmd) {
		case GO_HOME:
			performGlobalAction(GLOBAL_ACTION_HOME);
			break;
		case GO_BACK:
			performGlobalAction(GLOBAL_ACTION_BACK);
			break;
		case GO_RIGHT:
			performGlobalAction(GESTURE_SWIPE_RIGHT);
			break;
		case GO_NEXT:
			simulateKey(KeyEvent.KEYCODE_VOLUME_MUTE);
			break;
		case LOUDER:
			audioManager.adjustVolume(AudioManager.ADJUST_RAISE,
					AudioManager.FLAG_SHOW_UI | AudioManager.FLAG_PLAY_SOUND);
			break;
		case QUIETER:
			audioManager.adjustVolume(AudioManager.ADJUST_LOWER,
					AudioManager.FLAG_SHOW_UI | AudioManager.FLAG_PLAY_SOUND);
			break;

		default:
			break;
		}

	}

	@Background
	public void simulateKey(final int KeyCode) {
		Log.d(TAG, "1");
		Log.d(TAG, "2");
		try {
			Instrumentation inst = new Instrumentation();
			Log.d(TAG, "3 " + KeyCode);
			inst.sendKeyDownUpSync(KeyCode);
			Log.d(TAG, "4");
		} catch (Exception e) {
			Log.e(TAG, e.getMessage());
		}

	}

}
