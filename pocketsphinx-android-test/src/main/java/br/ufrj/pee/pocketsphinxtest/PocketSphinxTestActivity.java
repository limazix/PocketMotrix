package br.ufrj.pee.pocketsphinxtest;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.widget.TextView;
import android.widget.Toast;

import net.rdrei.android.dirchooser.DirectoryChooserActivity;

import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OnActivityResult;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;

import br.ufrj.pee.pocketsphinxtest.view.DirectoryPickerView;

@OptionsMenu(R.menu.menu_pocket_sphinx_test)
@EActivity(R.layout.activity_pocket_sphinx_test)
public class PocketSphinxTestActivity extends ActionBarActivity {

    private static final int REQUEST_DIRECTORY = 0;
    private static final String TAG = PocketSphinxTestActivity_.class.getName();

    @ViewById(R.id.directoryPickerBatchFiles)
    DirectoryPickerView directoryPickerBatchFiles;

    @Click(R.id.directoryPickerBatchFiles)
    void onChoseDirectoryClick() {
        final Intent choserIntent = new Intent(
                PocketSphinxTestActivity.this,
                DirectoryChooserActivity.class
        );

        choserIntent.putExtra(DirectoryChooserActivity.EXTRA_NEW_DIR_NAME, "DirName");

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
}
