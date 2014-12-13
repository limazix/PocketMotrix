package br.ufrj.pee.pocketmotrix.service;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EService;

import android.accessibilityservice.AccessibilityService;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import br.ufrj.pee.pocketmotrix.engine.TTSEngine;

@EService
public class PocketMotrixService extends AccessibilityService {

	private static final String TAG = PocketMotrixService_.class.getName();
	
	@Bean
	TTSEngine mTTSEngine;
	
	@Override
	public void onServiceConnected() {
		Log.i(TAG, "PocketMotrixService Connected");
	    
	}

	@Override
	public void onAccessibilityEvent(AccessibilityEvent event) {
		// TODO Auto-generated method stub
		Log.i(TAG, "Accessibility Event");
		//mTTS.speakToUser("This is a test!");
	}

	@Override
	public void onInterrupt() {
		// TODO Auto-generated method stub

	}
}
