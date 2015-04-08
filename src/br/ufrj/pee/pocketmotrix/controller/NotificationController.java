package br.ufrj.pee.pocketmotrix.controller;

import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.sharedpreferences.Pref;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.util.Log;
import br.ufrj.pee.pocketmotrix.listener.SpeakerListener;
import br.ufrj.pee.pocketmotrix.notifier.AbstractNotifier;
import br.ufrj.pee.pocketmotrix.notifier.SpeakNotifier;
import br.ufrj.pee.pocketmotrix.notifier.SystemNotifier;
import br.ufrj.pee.pocketmotrix.util.PocketMotrixPrefs_;

@EBean
public class NotificationController extends AbstractController implements Runnable, SpeakerListener, OnSharedPreferenceChangeListener {

	private static final int MESSAGE_NOTIFICATION_INTERVAL = 100;
	private static final String TAG = NotificationController_.class.getName();
	private ArrayList<AbstractNotifier> notifiers = new ArrayList<AbstractNotifier>();
	private LinkedBlockingQueue<String> messages = new LinkedBlockingQueue<String>();
	
	private boolean stop = false;

	@Pref
	PocketMotrixPrefs_ prefs;
	
	@Bean
	SpeakNotifier speakNotifier;
	
	@Bean
	SystemNotifier systemNotifier; 
	
	@Override
	public void setup() {
		speakNotifier.setListener(this);
	}

	@Override
	public void startEngine() {
		speakNotifier.setupNotifier();
		systemNotifier.setupNotifier();
		
		if(prefs.useSystemNotifier().get())
			addNotifier(systemNotifier);
	}

	@Override
	public void stopEngine() {
		this.stop = false;
	}
	
	public void addMessage(String message) {
		Log.d(TAG, "add message: " + message);
		messages.add(message);
	}
	
	public void addNotifier(AbstractNotifier notifier) {
		notifiers.add(notifier);
	}
	
	public void removeNotifier(AbstractNotifier notifier) {
		notifiers.remove(notifiers);
	}
	
	@Override
	public void onSpeakerReady() {
		if(prefs.useSpeakNotifier().get())
			addNotifier(speakNotifier);
	}
	
	@Override
	public void onSpeakerError(String errorMessage) {
		addMessage(errorMessage);
	}

	@Override
	public void run() {
		startEngine();
		while(!stop) {
			try {
				if(!messages.isEmpty()) {
					String message = messages.poll();
					for (AbstractNotifier notifier : notifiers)
						notifier.notifyUser(message);
				}
				
				Thread.sleep(MESSAGE_NOTIFICATION_INTERVAL);
			} catch (InterruptedException e) {
				Log.e(TAG, e.getMessage());
			}
		}
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		if(prefs.useSpeakNotifier().key().equals(key)) {
			if(prefs.useSpeakNotifier().get())
				addNotifier(speakNotifier);
			else
				removeNotifier(speakNotifier);
		} else if(prefs.useSystemNotifier().key().equals(key)) {
			if(prefs.useSystemNotifier().get())
				addNotifier(systemNotifier);
			else
				removeNotifier(systemNotifier);
		}
	}

}
