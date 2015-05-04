package br.ufrj.pee.pocketsphinxtest.listener;

/**
 * Created by limazix on 03/05/15.
 */
public interface TestProgressListener {

    public void onTestProgressUpdate(Integer progress);

    public void onScenarioProgressUpdate(Integer progress);

    public void onScenarioChange(String scenario);

    public void onScenarioError(String errorMsg);

    public void onError(String errorMsg);
}
