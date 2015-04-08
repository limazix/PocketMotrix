package br.ufrj.pee.pocketmotrix;

import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.widget.TextView;

@OptionsMenu(R.menu.pocket_motrix)
@EActivity(R.layout.activity_pocket_motrix)
public class PocketMotrixActivity extends ActionBarActivity {

    @ViewById(R.id.caption_text)
    TextView captionText;
    
    @ViewById(R.id.result_text)
    TextView resultText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
     super.onCreate(savedInstanceState);

     getFragmentManager().beginTransaction().replace(android.R.id.content,
                   new PrefsFragment()).commit();
    }

}