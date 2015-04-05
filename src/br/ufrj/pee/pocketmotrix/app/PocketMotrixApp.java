package br.ufrj.pee.pocketmotrix.app;

import org.androidannotations.annotations.EApplication;

import br.ufrj.pee.pocketmotrix.R;
import br.ufrj.pee.pocketmotrix.service.PocketMotrixService;
import android.app.Application;
import android.app.KeyguardManager;
import android.app.NotificationManager;
import android.app.KeyguardManager.KeyguardLock;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.support.v4.app.NotificationCompat;

@EApplication
public class PocketMotrixApp extends Application {

	private static final String CLIPBOARD_LABEL = "label";
	private static final String TAG = PocketMotrixApp.class.getName();
	private static final long MIN_WAKE_TIME = 30000;
	private static final int NOTIFICATION_DEFAULT_ID = 0;
	
	private PocketMotrixService pocketMotrixService;
	
	private NotificationManager notificationManager;
	private ClipboardManager clipboardManager;
	private AudioManager audioManager;
	private PowerManager powerManager;
	private KeyguardManager keyguardManager;
	
	private WakeLock wakeLock;
	private KeyguardLock keyguardLock;
	
	private NotificationCompat.Builder notificationBuilder;
	
	private String notificationTitle;
	private String notificationContentText;
	private String notificationTicker;
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
		wakeLock = powerManager.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.FULL_WAKE_LOCK | PowerManager.ON_AFTER_RELEASE, TAG);

		keyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
		keyguardLock = keyguardManager.newKeyguardLock(TAG);
		
		audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

		clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
		
		notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		setupNotificationManager();
		
		registerReceiver(mReceiver, new IntentFilter(Intent.ACTION_SCREEN_OFF));
	}
	
	private void setupNotificationManager() {
		notificationBuilder = new NotificationCompat.Builder(this)
		.setSmallIcon(R.drawable.ic_launcher);
		setNotificationTitle(getResources().getString(R.string.app_name));
		showNotification();
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
	
	public void wrteOnClipboard(String text) {
		ClipData clip = ClipData.newPlainText(CLIPBOARD_LABEL, text);
		clipboardManager.setPrimaryClip(clip);

	}

	public void showNotification() {
		notificationManager.notify(NOTIFICATION_DEFAULT_ID, notificationBuilder.build());
	}
	
	public String getNotificationTitle() {
		return notificationTitle;
	}

	public void setNotificationTitle(String notificationTitle) {
		this.notificationTitle = notificationTitle;
		notificationBuilder.setContentTitle(notificationTitle);
	}

	public String getNotificationContentText() {
		return notificationContentText;
	}

	public void setNotificationContentText(String notificationContentText) {
		this.notificationContentText = notificationContentText;
		notificationBuilder.setContentText(notificationContentText);
	}

	public String getNotificationTicker() {
		return notificationTicker;
	}

	public void setNotificationTicker(String notificationTicker) {
		this.notificationTicker = notificationTicker;
		notificationBuilder.setTicker(notificationTicker);
	}

	public PocketMotrixService getPocketMotrixService() {
		return pocketMotrixService;
	}

	public void setPocketMotrixService(PocketMotrixService pocketMotrixService) {
		this.pocketMotrixService = pocketMotrixService;
	}

	private BroadcastReceiver mReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF))
				keyguardLock.reenableKeyguard();
		}
	};
}
