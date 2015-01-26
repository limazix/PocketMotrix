package br.ufrj.pee.pocketmotrix;

import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;

import android.support.v7.app.ActionBarActivity;
import android.widget.TextView;

@EActivity(R.layout.activity_pocket_motrix)
@OptionsMenu(R.menu.pocket_motrix)
public class PocketMotrixActivity extends ActionBarActivity {

    @ViewById(R.id.caption_text)
    TextView captionText;
    
    @ViewById(R.id.result_text)
    TextView resultText;
    
}