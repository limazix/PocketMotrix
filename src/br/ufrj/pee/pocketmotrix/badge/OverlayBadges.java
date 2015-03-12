package br.ufrj.pee.pocketmotrix.badge;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import org.androidannotations.annotations.EBean;

import android.content.Context;
import android.util.Log;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityNodeInfo;
import ca.idrc.tecla.framework.SimpleOverlay;

@EBean
public class OverlayBadges extends SimpleOverlay {

	public static final String TAG = OverlayBadges.class.getName();
	
	private String prefix = "";
	private HashMap<String, BadgeView> badgesMap = new HashMap<String, BadgeView>();
	
	public OverlayBadges(Context context) {
		super(context);
		
		final WindowManager.LayoutParams params = getParams();
		params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;
		params.flags |= WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
		params.flags |= WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
		//params.flags |= WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD;
		params.flags |= WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED;
		setParams(params);
	}
	
	@Override
	public void onHide() {
		clearBadges();
	}
	
	public void clearBadges() {
		for(BadgeView badgeView : badgesMap.values()) {
			badgeView.clear();
			badgeView.postInvalidate();
		}
		
		badgesMap.clear();
		clearView();
	}
	
	public ArrayList<AccessibilityNodeInfo> filterBadges(String filter) {
		prefix += filter;
		ArrayList<AccessibilityNodeInfo> response = new ArrayList<AccessibilityNodeInfo>();
		for(String key : badgesMap.keySet()) {
			BadgeView badgeView = badgesMap.get(key);
			if(!key.startsWith(prefix)) {
				badgeView.clear();
				badgeView.postInvalidate();
				removeView(badgeView);
			} else {
				response.add(badgeView.getNode());
			}
		}
		
		return response;
	}
	
	public void addBadges(ArrayList<AccessibilityNodeInfo> nodes) {
		clearBadges();
		if(!nodes.isEmpty()) {
			prefix = "";
			int i = 1;
			for(AccessibilityNodeInfo node : nodes) {
				if(node != null) {
					String key = BadgeUtils_.getBadge(i, nodes.size());
					BadgeView badgeView = BadgeView_.build(getContext());
					badgeView.add(node, key);
					addContentView(badgeView);
					badgeView.postInvalidate();
					badgesMap.put(key, badgeView);
					i++;
				}
			}
		}
	}
	
}
