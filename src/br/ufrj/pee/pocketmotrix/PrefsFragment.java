package br.ufrj.pee.pocketmotrix;

import org.androidannotations.annotations.EFragment;

import android.os.Bundle;
import android.preference.PreferenceFragment;

@EFragment
public class PrefsFragment extends PreferenceFragment {
	
	public static String PREF_NAME = "PocketMotrixPrefs";
	
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getPreferenceManager().setSharedPreferencesName(PREF_NAME);
 
        addPreferencesFromResource(R.xml.preferences);
    }

}
