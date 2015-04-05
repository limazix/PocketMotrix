package br.ufrj.pee.pocketmotrix.controller;

import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;

import android.util.Log;
import br.ufrj.pee.pocketmotrix.listener.SpeakerListener;
import br.ufrj.pee.pocketmotrix.notifier.AbstractNotifier;
import br.ufrj.pee.pocketmotrix.notifier.SpeakNotifier;
import br.ufrj.pee.pocketmotrix.notifier.SystemNotifier;

@EBean
public class NotificationController extends AbstractController implements Runnable, SpeakerListener {

	private static final int MESSAGE_NOTIFICATION_INTERVAL = 100;
	private static final String TAG = NotificationController_.class.getName();
	private ArrayList<AbstractNotifier> notifiers = new ArrayList<AbstractNotifier>();
	private LinkedBlockingQueue<String> messages = new LinkedBlockingQueue<String>();
	
	private boolean stop = false;
	
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

}
