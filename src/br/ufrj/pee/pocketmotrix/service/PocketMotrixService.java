package br.ufrj.pee.pocketmotrix.service;

import java.util.Observable;
import java.util.Observer;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EService;

import android.accessibilityservice.AccessibilityService;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import br.ufrj.pee.pocketmotrix.engine.SREngine;
import br.ufrj.pee.pocketmotrix.engine.TTSEngine;

@EService
public class PocketMotrixService extends AccessibilityService implements Observer {

	private static final String TAG = PocketMotrixService_.class.getName();
	
	@Bean
	SREngine mSREngine;
	
	@Bean
	TTSEngine mTTSEngine;
	
	@Override
	public void onServiceConnected() {
		Log.i(TAG, "PocketMotrixService Connected");
	}
	
	@AfterInject
	public void settingupEngines() {
		mTTSEngine.addObserver(this);
		mSREngine.addObserver(this);
	}

	@Override
	public void onAccessibilityEvent(AccessibilityEvent event) {
		Log.i(TAG, "Accessibility Event");
		//mTTS.speakToUser("This is a test!");
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
		if(observable instanceof SREngine){
			Log.d(TAG, "notified");
			mTTSEngine.speakToUser(mSREngine.getResultText());
		} else if(observable instanceof TTSEngine){ 
			if(mTTSEngine.getIsInitialized())
				mSREngine.setupEngine();
		}
	}

	@Override
	public void onInterrupt() {
		// TODO Auto-generated method stub
		
	}
}
