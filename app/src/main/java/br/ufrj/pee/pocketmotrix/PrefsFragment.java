package br.ufrj.pee.pocketmotrix;

import org.androidannotations.annotations.EFragment;

import android.os.Bundle;
import android.preference.PreferenceFragment;
import br.ufrj.pee.pocketmotrix.app.PocketMotrixApp_;

@EFragment
public class PrefsFragment extends PreferenceFragment {
	
	public static String PREF_NAME = "PocketMotrixPrefs";
    
	PocketMotrixApp_ app;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        app = (PocketMotrixApp_) getActivity().getApplication();
        getPreferenceManager().setSharedPreferencesName(PREF_NAME);
 
        addPreferencesFromResource(R.xml.preferences);
    }
    
    @Override
	public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(app.getNotificationController());
    }

    @Override
	public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(app.getNotificationController());
    }

}
