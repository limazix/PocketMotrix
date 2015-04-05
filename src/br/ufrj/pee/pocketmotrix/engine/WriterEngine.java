package br.ufrj.pee.pocketmotrix.engine;

import java.util.ArrayList;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.EBean.Scope;
import org.androidannotations.annotations.RootContext;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import br.ufrj.pee.pocketmotrix.listener.WriterListener;

@EBean(scope = Scope.Singleton)
public class WriterEngine implements RecognitionListener {

	private static final int MATCHES_TOP_POSITION = 0;

	private static final String TAG = WriterEngine_.class.getName();

	@RootContext
	Context context;
	
	private SpeechRecognizer recognizer;
	private Intent recognizerIntent;
	
	private WriterListener listener;

	public void setupRecognitionEngine() {
		recognizer = SpeechRecognizer.createSpeechRecognizer(context);
		recognizer.setRecognitionListener(this);
		
		recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
		recognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getClass().getPackage().getName());
		recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
				RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
		
	}
	
	public WriterListener getWriterListener() {
		return listener;
	}

	public void setWriterListener(WriterListener listener) {
		this.listener = listener;
	}

	public void startListening() {
		recognizer.startListening(recognizerIntent);
	}

	public void stopListening() {
		recognizer.stopListening();
		listener.onWriterStoped();
	}

	@Override
	public void onReadyForSpeech(Bundle params) {
		listener.onWriterReady();
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
		listener.onWriterError(String.valueOf(error));
	}

	@Override
	public void onResults(Bundle results) {
		ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
		listener.onWriteText(matches.get(MATCHES_TOP_POSITION));
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
