package br.ufrj.pee.pocketmotrix.engine;

import static edu.cmu.pocketsphinx.SpeechRecognizerSetup.defaultSetup;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Observable;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.EBean.Scope;
import org.androidannotations.annotations.RootContext;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import br.ufrj.pee.pocketmotrix.R;
import edu.cmu.pocketsphinx.Assets;
import edu.cmu.pocketsphinx.Hypothesis;
import edu.cmu.pocketsphinx.RecognitionListener;
import edu.cmu.pocketsphinx.SpeechRecognizer;

@EBean(scope = Scope.Singleton)
public class SREngine extends Observable implements RecognitionListener {

	private static final String TAG = SREngine_.class.getName();
	
	private static final String KWS_SEARCH = "wakeup";
    private static final String KEYPHRASE = "oh mighty computer";
    private static final String NAVIGATION_SEARCH = "navigation";
	private static final String DEACTIVATE_SEARCH = "stop listening";

    private SpeechRecognizer recognizer;
    private HashMap<String, Integer> captions;
   
    private String captionText = "";
    private String resultText = "";
	
	@RootContext
	Context context;
	
	public void setupEngine() {
        // Prepare the data for UI
        captions = new HashMap<String, Integer>();
        captions.put(KWS_SEARCH, R.string.kws_caption);
        captions.put(NAVIGATION_SEARCH, R.string.navigation_caption);
        
        Log.i(TAG, "Preparing the recognizer");
        setCaptionText("Preparing the recognizer");

        new AsyncTask<Void, Void, Exception>() {
            @Override
            protected Exception doInBackground(Void... params) {
                try {
                    Assets assets = new Assets(context);
                    File assetDir = assets.syncAssets();
                    setupRecognizer(assetDir);
                } catch (IOException e) {
                    return e;
                }
                return null;
            }

            @Override
            protected void onPostExecute(Exception result) {
                if (result != null) {
                	setCaptionText("Failed to init recognizer " + result);
                    Log.e(TAG, "Failed to init recognizer " + result);
                } else {
                    switchSearch(KWS_SEARCH);
                }
            }
        }.execute();
    }
	
	public void finishEngine() {
		Log.i(TAG, "Shutdown");
//		recognizer.cancel();
//		recognizer.shutdown();
	}

    @Override
    public void onPartialResult(Hypothesis hypothesis) {
        if (hypothesis == null)
    	    return;

        String text = hypothesis.getHypstr();
        
		if (text.equals(DEACTIVATE_SEARCH))
			switchSearch(KWS_SEARCH);
		else if (text.equals(KEYPHRASE))
			switchSearch(NAVIGATION_SEARCH);
		else
			Log.i(TAG, "parcial result: " + text);
    }

    @Override
    public void onResult(Hypothesis hypothesis) {
        if (hypothesis != null) {
            String text = hypothesis.getHypstr();

            /** 
             * Workaround to force the engine to keep listening after the speech ends
             * TODO: Redesign the SpeechRecognition class of Pocketsphinx lib to use
             * Segment and SegmentIterator instead Hypothesis.
             * See https://code.google.com/p/cjoycap/source/browse/trunk/cjoycap/WinMMTest1/VoiceRecognizer.cpp
             */ 
            if(KWS_SEARCH.equals(recognizer.getSearchName()) && !DEACTIVATE_SEARCH.equals(text))
    			switchSearch(NAVIGATION_SEARCH);
    		
    		if(!DEACTIVATE_SEARCH.equals(text) || 
    				!KWS_SEARCH.equals(recognizer.getSearchName()) || 
    					!KEYPHRASE.equals(text)) {
    			setResultText(text);
    		}
            Log.i(TAG, "result: " + text);
        }
    }

    @Override
    public void onBeginningOfSpeech() {
    }

    @Override
    public void onEndOfSpeech() {
        if (!recognizer.getSearchName().equals(KWS_SEARCH))
            switchSearch(KWS_SEARCH);
		
        Log.d(TAG, "============END============");
    }

    private void switchSearch(String searchName) {
        recognizer.stop();
        
//         If we are not spotting, start listening with timeout
        if (searchName.equals(KWS_SEARCH))
            recognizer.startListening(searchName);
        else
            recognizer.startListening(searchName, 10000);

        String caption = context.getString(captions.get(searchName));
    	setCaptionText(caption);
        Log.i(TAG, "caption: " + caption);
    }

    private void setupRecognizer(File assetsDir) {
        // The recognizer can be configured to perform multiple searches
        // of different kind and switch between them
        
        File modelsDir = new File(assetsDir, "models");
        try {
			recognizer = defaultSetup()
			        .setAcousticModel(new File(modelsDir, "hmm/en-us-semi"))
			        .setDictionary(new File(modelsDir, "dict/cmu07a.dic"))
			        
			        // To disable logging of raw audio comment out this call (takes a lot of space on the device)
			        .setRawLogDir(assetsDir)
			        
			        // Threshold to tune for keyphrase
			        .setKeywordThreshold(1e-40f)
			        
			        // Use context-independent phonetic search, context-dependent is too slow for mobile
			        .setBoolean("-allphone_ci", true)
			        
			        .getRecognizer();
		} catch (IOException e) {
			Log.e(TAG, e.getMessage());
		}
        recognizer.addListener(this);

        // Create keyword-activation search.
        recognizer.addKeyphraseSearch(KWS_SEARCH, KEYPHRASE);
        
        // Create grammar-based search for selection between demos
        File navigationGrammar = new File(modelsDir, "grammar/navigation.gram");
        recognizer.addGrammarSearch(NAVIGATION_SEARCH, navigationGrammar);

    }

    @Override
    public void onError(Exception error) {
    	setCaptionText(error.getMessage());
    	Log.e(TAG, "============" + error.getMessage() + "============");
    }

    @Override
    public void onTimeout() {
        switchSearch(KWS_SEARCH);
    }

	public String getCaptionText() {
		return captionText;
	}

	public void setCaptionText(String captionText) {
		this.captionText = captionText;
	}

	public String getResultText() {
		return resultText;
	}

	public void setResultText(String resultText) {
		this.resultText = resultText;
		setChanged();
		notifyObservers(resultText);
	}

}