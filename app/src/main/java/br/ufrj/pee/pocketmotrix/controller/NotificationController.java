package br.ufrj.pee.pocketmotrix.controller;

import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.sharedpreferences.Pref;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.util.Log;

import br.ufrj.pee.pocketmotrix.listener.NotifierListener;
import br.ufrj.pee.pocketmotrix.notifier.AbstractNotifier;
import br.ufrj.pee.pocketmotrix.notifier.SpeakNotifier;
import br.ufrj.pee.pocketmotrix.notifier.SystemNotifier;
import br.ufrj.pee.pocketmotrix.util.PocketMotrixPrefs_;

@EBean
public class NotificationController extends AbstractController implements Runnable, NotifierListener, OnSharedPreferenceChangeListener {

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
        systemNotifier.setListener(this);
	}

	@Override
	public void startEngine() {

		if(prefs.useSystemNotifier().get()) {
            addNotifier(systemNotifier);
        }

        if(prefs.useSpeakNotifier().get()) {
            addNotifier(speakNotifier);
        }
	}

	@Override
	public void stopEngine() {
		this.stop = false;
	}
	
	public void addMessage(String message) {
		messages.add(message);
	}
	
	public void addNotifier(AbstractNotifier notifier) {
        if(!notifiers.contains(notifier)) {
            if(!notifier.isReady()) {
                notifier.setupNotifier();
            } else notifiers.add(notifier);
        }
	}
	
	public void removeNotifier(AbstractNotifier notifier) {
		notifiers.remove(notifier);
	}
	
	@Override
	public void onNotifierReady(AbstractNotifier notifier) {
		notifiers.add(notifier);
	}
	
	@Override
	public void onNotifierError(String errorMessage) {
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
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
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
