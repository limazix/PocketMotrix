package br.ufrj.pee.pocketsphinxtest;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.widget.Toast;

import com.github.lzyzsd.circleprogress.ArcProgress;

import net.rdrei.android.dirchooser.DirectoryChooserActivity;

import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OnActivityResult;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import br.ufrj.pee.pocketsphinxtest.listener.TestProgressListener;
import br.ufrj.pee.pocketsphinxtest.view.DirectoryPickerView;

@EActivity(R.layout.activity_pocket_sphinx_test)
public class PocketSphinxTestActivity extends ActionBarActivity implements TestProgressListener {

    private static final String TAG = PocketSphinxTestActivity_.class.getName();

    private static final int REQUEST_DIRECTORY = 0;
    private static final String DIR_NAME = "DirName";

    private static final int START_PROGRESS = 0;

    @ViewById(R.id.progressScenario)
    ArcProgress progressSenarioView;

    @ViewById(R.id.progressTest)
    ArcProgress progressTestView;

    @ViewById(R.id.directoryPickerBatchFiles)
    DirectoryPickerView directoryPickerBatchFiles;

    @Click(R.id.btnStartStop)
    void onStartStopButtonClick() {
        Toast.makeText(this, "Button Clicked", Toast.LENGTH_LONG).show();
    }

    @Click(R.id.directoryPickerBatchFiles)
    void onChoseDirectoryClick() {
        final Intent choserIntent = new Intent(
                PocketSphinxTestActivity.this,
                DirectoryChooserActivity.class
        );

        choserIntent.putExtra(DirectoryChooserActivity.EXTRA_NEW_DIR_NAME, DIR_NAME);

        startActivityForResult(choserIntent, REQUEST_DIRECTORY);
    }

    @OnActivityResult(REQUEST_DIRECTORY)
    void onResult(int resultCode, Intent data) {
        if(resultCode == DirectoryChooserActivity.RESULT_CODE_DIR_SELECTED) {
            directoryPickerBatchFiles.setDirectoryPath(data.getStringExtra(DirectoryChooserActivity.RESULT_SELECTED_DIR));
        } else {
            Toast.makeText(this, "Nothing Selected", Toast.LENGTH_LONG).show();
        }
    }

    @UiThread
    @Override
    public void onTestProgressUpdate(Integer progress) {
        progressTestView.setProgress(progress);
    }

    @UiThread
    @Override
    public void onScenarioProgressUpdate(Integer progress) {
        progressSenarioView.setProgress(progress);
    }

    @UiThread
    @Override
    public void onScenarioChange(String scenario) {
        progressSenarioView.setBottomText(scenario);
        progressSenarioView.setProgress(START_PROGRESS);
    }

    @Override
    public void onScenarioError(String errorMsg) {
        Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onError(String errorMsg) {
        Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show();
    }
}
