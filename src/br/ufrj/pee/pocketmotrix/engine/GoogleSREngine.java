package br.ufrj.pee.pocketmotrix.engine;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.androidannotations.annotations.EBean.Scope;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.widget.Toast;

@EBean(scope = Scope.Singleton)
public class GoogleSREngine extends Observable implements RecognitionListener {

	private static final String VOICE_RECOGNIZER_NOT_PRESENT = "Voice recognizer not present";

	private static final int MATCHES_TOP_POSITION = 0;

	private static final String TAG = GoogleSREngine.class.getName();

	@RootContext
	Context context;
	
	private String match = "";
	
	private SpeechRecognizer recognizer;
	private Intent recognizerIntent;

	public void setupRecognitionEngine() {
		recognizer = SpeechRecognizer.createSpeechRecognizer(context);
		recognizer.setRecognitionListener(this);
		
		recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
		recognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getClass().getPackage().getName());
		recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
				RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
		
	}
	
	public void startListening() {
		recognizer.startListening(recognizerIntent);
	}

	public void stopListening() {
		recognizer.stopListening();
	}
	
	public boolean hasVoiceRecognition() {
		PackageManager pm = context.getPackageManager();
		Intent recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
		List<ResolveInfo> activities = pm.queryIntentActivities(recognizerIntent, 0);
		if (activities.isEmpty()) {
			Toast.makeText(context, VOICE_RECOGNIZER_NOT_PRESENT, Toast.LENGTH_SHORT).show();
			Log.e(TAG, VOICE_RECOGNIZER_NOT_PRESENT);
			return false;
		}
		return true;
	}

	public String getMatch() {
		return match;
	}

	public void setMatch(String match) {
		this.match = match;
		setChanged();
		notifyObservers(match);
	}

	@Override
	public void onReadyForSpeech(Bundle params) {
		Log.i(TAG, "ready for speech");
	}

	@Override
	public void onBeginningOfSpeech() {
		Log.i(TAG, "beginning of speech");
	}

	@Override
	public void onRmsChanged(float rmsdB) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onBufferReceived(byte[] buffer) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onEndOfSpeech() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onError(int error) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onResults(Bundle results) {
		ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
		setMatch(matches.get(MATCHES_TOP_POSITION));
	}

	@Override
	public void onPartialResults(Bundle partialResults) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onEvent(int eventType, Bundle params) {
		// TODO Auto-generated method stub

	}

}
