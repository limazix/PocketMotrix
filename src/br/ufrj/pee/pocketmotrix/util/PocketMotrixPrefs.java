package br.ufrj.pee.pocketmotrix.util;

import org.androidannotations.annotations.sharedpreferences.DefaultRes;
import org.androidannotations.annotations.sharedpreferences.SharedPref;
import org.androidannotations.annotations.sharedpreferences.SharedPref.Scope;

import br.ufrj.pee.pocketmotrix.R;

@SharedPref(value = Scope.UNIQUE)
public interface PocketMotrixPrefs {

	@DefaultRes(R.bool.default_useSpeakNotifier)
	boolean useSpeakNotifier();
	
	@DefaultRes(R.bool.default_useSystemNotifier)
	boolean useSystemNotifier();
}
