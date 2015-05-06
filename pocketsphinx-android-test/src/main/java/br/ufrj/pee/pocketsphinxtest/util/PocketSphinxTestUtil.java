package br.ufrj.pee.pocketsphinxtest.util;

import android.util.Log;

import java.io.FileReader;
import java.util.ArrayList;

import br.ufrj.pee.pocketsphinxtest.pojo.PojoMapper;
import br.ufrj.pee.pocketsphinxtest.pojo.ScenarioPojo;

/**
 * Created by limazix on 04/05/15.
 */
public class PocketSphinxTestUtil {

    private static final String TAG = PocketSphinxTestUtil.class.getName();
    private static final String INPUT_FILE_NAME = "inputs.json";
    private static final String PATH_DELIMITER = "/";

    public static ArrayList<ScenarioPojo> buildScenariosFromFile(String dir) {
        ArrayList<ScenarioPojo> scenarioList = null;
        try {
            FileReader fr = new FileReader(dir + PATH_DELIMITER + INPUT_FILE_NAME);
            scenarioList = (ArrayList<ScenarioPojo>) PojoMapper.fromJsonArray(fr, ScenarioPojo.class);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }

        return scenarioList;
    }
}
