package br.ufrj.pee.pocketsphinxtest.engine;

import static edu.cmu.pocketsphinx.SpeechRecognizerSetup.defaultSetup;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;

import org.androidannotations.annotations.EService;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import br.ufrj.pee.pocketsphinxtest.pojo.InputPojo;
import edu.cmu.pocketsphinx.Assets;
import edu.cmu.pocketsphinx.Hypothesis;
import edu.cmu.pocketsphinx.RecognitionListener;
import edu.cmu.pocketsphinx.SpeechRecognizer;

/**
 * Created by limazix on 05/05/15.
 */
@EService
public class PocketSphinxEngine extends Service implements RecognitionListener {

    private static final String TAG = PocketSphinxEngine_.class.getName();

    private static final String KWS_SEARCH = "wakeup";
    private static final String KEYPHRASE = "start listening";
    private static final String NAVIGATION_SEARCH = "navigation";
    private static final String DEACTIVATE_SEARCH = "stop listening";

    private File assetsDir;
    private SpeechRecognizer recognizer;

    private String currentSearch = "";
    private String resultText = "";

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void setupEngine() {

        new AsyncTask<Void, Void, Exception>() {
            @Override
            protected Exception doInBackground(Void... params) {
                try {
                    Assets assets = new Assets(getApplication().getApplicationContext());
                    assetsDir = assets.syncAssets();
                } catch (IOException e) {
                    Log.e(TAG, e.getMessage());
                    return e;
                }
                return null;
            }

            @Override
            protected void onPostExecute(Exception result) {
                Log.i(TAG, "Engine is Ready");
            }
        }.execute();
    }

    public void setupRecognizer(InputPojo inputPojo) {
        File modelsDir = new File(assetsDir, "models");
        try {

            for(Map.Entry<String, String> entry : inputPojo.getStringInputMap().entrySet()) {
                defaultSetup().setString(entry.getKey(), entry.getValue());
            }

            for(Map.Entry<String, Boolean> entry : inputPojo.getBooleanInputMap().entrySet()) {
                defaultSetup().setBoolean(entry.getKey(), entry.getValue());
            }

            for(Map.Entry<String, Integer> entry : inputPojo.getIntegerInputMap().entrySet()) {
                defaultSetup().setInteger(entry.getKey(), entry.getValue());
            }

            for(Map.Entry<String, Float> entry : inputPojo.getFloatInputMap().entrySet()) {
                defaultSetup().setFloat(entry.getKey(), entry.getValue());
            }

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
        }
        recognizer.addListener(this);

        recognizer.addKeyphraseSearch(KWS_SEARCH, KEYPHRASE);

        File navigationGrammar = new File(modelsDir, "grammar/navigation.gram");
        recognizer.addGrammarSearch(NAVIGATION_SEARCH, navigationGrammar);

    }

    public void finishEngine() {
        recognizer.cancel();
        recognizer.shutdown();
    }

    private void switchSearch(String searchName) {
        recognizer.stop();

        if (searchName.equals(KWS_SEARCH))
            recognizer.startListening(searchName);
        else
            recognizer.startListening(searchName, 10000);

    }

    @Override
    public void onBeginningOfSpeech() {

    }

    @Override
    public void onEndOfSpeech() {

    }

    @Override
    public void onPartialResult(Hypothesis hypothesis) {

    }

    @Override
    public void onResult(Hypothesis hypothesis) {
        if (hypothesis != null) {
            String text = hypothesis.getHypstr();
        }
    }

    @Override
    public void onError(Exception e) {

    }

    @Override
    public void onTimeout() {

    }
}
