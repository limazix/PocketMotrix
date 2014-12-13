package br.ufrj.pee.pocketmotrix;

import java.util.Observable;
import java.util.Observer;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;

import br.ufrj.pee.pocketmotrix.engine.SREngine;
import br.ufrj.pee.pocketmotrix.engine.SREngine_;
import android.support.v7.app.ActionBarActivity;
import android.widget.TextView;

@EActivity(R.layout.activity_pocket_motrix)
@OptionsMenu(R.menu.pocket_motrix)
public class PocketMotrixActivity extends ActionBarActivity implements Observer {

	@Bean
	SREngine mSREngine;
	
    @ViewById(R.id.caption_text)
    TextView captionText;
    
    @ViewById(R.id.result_text)
    TextView resultText;
    
    @AfterViews
    public void setupActivity() {
    	mSREngine.addObserver(this);
    	mSREngine.setupEngine();
    }

	@Override
	public void update(Observable observable, Object data) {
		if(observable instanceof SREngine){
			SREngine_ sre = (SREngine_) observable;
			captionText.setText(sre.getCaptionText());
			resultText.setText(sre.getResultText());
		}
	}
   
}