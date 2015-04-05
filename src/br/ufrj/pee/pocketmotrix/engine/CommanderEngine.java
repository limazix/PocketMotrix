package br.ufrj.pee.pocketmotrix.engine;

import static edu.cmu.pocketsphinx.SpeechRecognizerSetup.defaultSetup;

import java.io.File;
import java.io.IOException;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.EBean.Scope;
import org.androidannotations.annotations.RootContext;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import br.ufrj.pee.pocketmotrix.listener.NavigationListener;
import br.ufrj.pee.pocketmotrix.service.Command;
import br.ufrj.pee.pocketmotrix.util.PocketMotrixUtils;
import edu.cmu.pocketsphinx.Assets;
import edu.cmu.pocketsphinx.Hypothesis;
import edu.cmu.pocketsphinx.RecognitionListener;
import edu.cmu.pocketsphinx.SpeechRecognizer;

@EBean(scope = Scope.Singleton)
public class CommanderEngine implements RecognitionListener {

	private static final String TAG = CommanderEngine_.class.getName();

	private static final String KWS_SEARCH = "wakeup";
	private static final String KEYPHRASE = "start listening";
	private static final String NAVIGATION_SEARCH = "navigation";
	private static final String DEACTIVATE_SEARCH = "stop listening";
	
	private SpeechRecognizer recognizer;

	private NavigationListener listener;
	
	private String currentSearch = "";
	private String resultText = "";

	@RootContext
	Context context;

	public void setNavigationListener(NavigationListener listener) {
		this.listener = listener;
	}
	
	public void setupEngine() {

		new AsyncTask<Void, Void, Exception>() {
			@Override
			protected Exception doInBackground(Void... params) {
				try {
					Assets assets = new Assets(context);
					File assetDir = assets.syncAssets();
					setupRecognizer(assetDir);
				} catch (IOException e) {
					Log.e(TAG, e.getMessage());
					listener.onNavigationError("");
					return e;
				}
				return null;
			}

			@Override
			protected void onPostExecute(Exception result) {
				if (result != null) {
					Log.e(TAG, "Failed to init recognizer " + result.getMessage());
					listener.onNavigationError("");
				} else {
					switchSearch(KWS_SEARCH);
					currentSearch = KWS_SEARCH;
					listener.onNavigatorReady();
				}
			}
		}.execute();
	}

	public void finishEngine() {
		recognizer.cancel();
		recognizer.shutdown();
	}

	@Override
	public void onPartialResult(Hypothesis hypothesis) {
		if (hypothesis == null)
			return;

		String text = hypothesis.getHypstr();

		if (text.equals(DEACTIVATE_SEARCH)) {
			switchSearch(KWS_SEARCH);
			currentSearch = KWS_SEARCH;
		} else if (text.equals(KEYPHRASE) || text.contains(NAVIGATION_SEARCH)) {
			switchSearch(NAVIGATION_SEARCH);
			currentSearch = NAVIGATION_SEARCH;
		} else
			Log.i(TAG, "[SEARCH] parcial result: [" + currentSearch + "] " + text);
	}

	@Override
	public void onResult(Hypothesis hypothesis) {
		if (hypothesis != null) {
			String text = hypothesis.getHypstr();

			/**
			 * Workaround to force the engine to keep listening after the speech
			 * ends TODO: Redesign the SpeechRecognition class of Pocketsphinx
			 * lib to use Segment and SegmentIterator instead Hypothesis. See
			 * https://code.google.com/p/cjoycap/source/browse/trunk/cjoycap/
			 * WinMMTest1/VoiceRecognizer.cpp
			 */
			if (KWS_SEARCH.equals(recognizer.getSearchName())
					&& !DEACTIVATE_SEARCH.equals(text))
				switchSearch(currentSearch);
			

			setResultText(text);

			Log.i(TAG, "[SEARCH] result: [" + currentSearch + "] " + text);
		}
	}

	@Override
	public void onBeginningOfSpeech() {
	}

	@Override
	public void onEndOfSpeech() {
		if (!recognizer.getSearchName().equals(KWS_SEARCH))
			switchSearch(currentSearch);
	}

	private void switchSearch(String searchName) {
		recognizer.stop();

		if (searchName.equals(KWS_SEARCH))
			recognizer.startListening(searchName);
		else
			recognizer.startListening(searchName, 10000);

	}

	private void setupRecognizer(File assetsDir) {

		File modelsDir = new File(assetsDir, "models");
		try {
			recognizer = defaultSetup()
					.setAcousticModel(new File(modelsDir, "hmm/en-us-ptm"))
	                .setDictionary(new File(modelsDir, "dict/cmudict-en-us.dict"))

					// Threshold to tune for keyphrase
					.setKeywordThreshold(1e-40f)

					// Use context-independent phonetic search,
					// context-dependent is too slow for mobile
					.setBoolean("-allphone_ci", true)

					.getRecognizer();
		} catch (IOException e) {
			Log.e(TAG, e.getMessage());
			listener.onNavigationError("");
		}
		recognizer.addListener(this);

		recognizer.addKeyphraseSearch(KWS_SEARCH, KEYPHRASE);

		File navigationGrammar = new File(modelsDir, "grammar/navigation.gram");
		recognizer.addGrammarSearch(NAVIGATION_SEARCH, navigationGrammar);

	}

	@Override
	public void onError(Exception error) {
		listener.onNavigationError("");
		Log.e(TAG, "============" + error.getMessage() + "============");
	}

	@Override
	public void onTimeout() {
		switchSearch(KWS_SEARCH);
	}

	public String getResultText() {
		return resultText;
	}

	public void setResultText(String resultText) {
		this.resultText = resultText;
		String cmd = resultText.replaceAll("\\s", "");
		if(PocketMotrixUtils.numbers.containsKey(cmd))
			listener.onNavigationNumber(PocketMotrixUtils.numbers.get(cmd));
		else
			listener.onNavigationCommand(Command.get(cmd));
	}
	
	public void toIddle() {
		switchSearch(KWS_SEARCH);
		currentSearch = KWS_SEARCH;
	}
	
	

}
