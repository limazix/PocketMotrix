package br.ufrj.pee.pocketmotrix.badge;

import org.androidannotations.annotations.EBean;

@EBean
public class BadgeUtils {
	
//	private static final String TAG = BadgeUtils.class.getName();
	private static final String STRING_FORMATER_TYPE = "d";
	private static final String STRING_FORMATER_COMPLEMENT = "%0";
	
	public static String getBadge(int position, int numberOfNodes) {
		
		int numDigits = (int) Math.log10(numberOfNodes) + 1;
		if(numDigits < 2) numDigits = 2;
		String format = STRING_FORMATER_COMPLEMENT + String.valueOf(numDigits) + STRING_FORMATER_TYPE;
		String badge = String.format(format, position);
		
		return badge;
	}

}
