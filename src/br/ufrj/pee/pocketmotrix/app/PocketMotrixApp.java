package br.ufrj.pee.pocketmotrix.app;

import org.androidannotations.annotations.EApplication;

import android.app.Application;
import android.app.KeyguardManager;
import android.app.KeyguardManager.KeyguardLock;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;

@EApplication
public class PocketMotrixApp extends Application {

	private static final String TAG = PocketMotrixApp.class.getName();
	private static final long MIN_WAKE_TIME = 30000; 
	
	private AudioManager audioManager;
	private PowerManager powerManager;
	private KeyguardManager keyguardManager;
	
	private WakeLock wakeLock;
	private KeyguardLock keyguardLock;
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
		wakeLock = powerManager.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.FULL_WAKE_LOCK | PowerManager.ON_AFTER_RELEASE, TAG);

		keyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
		keyguardLock = keyguardManager.newKeyguardLock(TAG);
		
		audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);


		registerReceiver(mReceiver, new IntentFilter(Intent.ACTION_SCREEN_OFF));
	}
	
	public void wakeupScreen() {
		keyguardLock.disableKeyguard();
		wakeLock.acquire(MIN_WAKE_TIME);
	}
	
	public void releaseLockScreen() {
		if (wakeLock.isHeld())
			wakeLock.release();
	}
	
	public void volumeLouder() {
		audioManager.adjustVolume(AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI | AudioManager.FLAG_PLAY_SOUND);
	}
	
	public void volumeQuieter() {
		audioManager.adjustVolume(AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI | AudioManager.FLAG_PLAY_SOUND);
	}

	private BroadcastReceiver mReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF))
				keyguardLock.reenableKeyguard();
		}
	};
}
