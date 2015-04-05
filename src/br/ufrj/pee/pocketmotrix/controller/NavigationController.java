package br.ufrj.pee.pocketmotrix.controller;

import java.util.ArrayList;

import org.androidannotations.annotations.App;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;

import android.accessibilityservice.AccessibilityService;
import android.view.accessibility.AccessibilityNodeInfo;
import br.ufrj.pee.pocketmotrix.app.PocketMotrixApp;
import br.ufrj.pee.pocketmotrix.engine.NavigatorEngine;
import br.ufrj.pee.pocketmotrix.listener.NavigationListener;
import br.ufrj.pee.pocketmotrix.service.Command;
import br.ufrj.pee.pocketmotrix.service.PocketMotrixService;

@EBean
public class NavigationController implements NavigationListener {

	private PocketMotrixService service;

	@App
	PocketMotrixApp app;
	
	@Bean
	NavigatorEngine navigatorEngine;
	
	public void setup() {
		navigatorEngine.setNavigationListener(this);
		navigatorEngine.setupEngine();
		service = app.getPocketMotrixService();
	}
	
	public void finish() {
		navigatorEngine.finishEngine();
	}
	

	private void execute(Command cmd) {

		switch (cmd) {
		case GO_HOME:
			service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_HOME);
			break;
		case GO_BACK:
			service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK);
			break;
		case NOTIFICATION:
			service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_NOTIFICATIONS);
			break;
		// TODO improve badges not updating after scroll
		case HISTORY:
			service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_RECENTS);
			break;
		case SETTINGS:
			service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_QUICK_SETTINGS);
			break;
		case GO_FORWARD:
			service.scroll(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD);
			break;
		case GO_BACKWARD:
			service.scroll(AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD);
			break;
		case LOUDER:
			app.volumeLouder();
			break;
		case QUIETER:
			app.volumeQuieter();
			break;
		case WAKE_OR_KEEP_WAKE:
			app.wakeupScreen();
			break;
		case RELEASE_KEEP_WAKE:
			app.releaseLockScreen();
			break;
		case REFRESH:
			service.activateBadges();
			service.updateBadgesView(null);
			break;
		case CLEAR:
			service.deactivateBadges();
		default:
			break;
		}

	}
	
	@Override
	public void onNavigationCommand(Command cmd) {
		service.showNotification(cmd.getLabel());
		if(cmd.equals(Command.WRITE)) {
			navigatorEngine.toIddle();
			service.startWrite();
		} else execute(cmd);
	}

	@Override
	public void onNavigationNumber(String number) {
		service.showNotification(number);
		ArrayList<AccessibilityNodeInfo> response = service.filterBadges(number);
		if (response.size() == 1) {
			if (service.isScrolling())
				service.scroll(response.get(0), service.getDirection());
			else
				service.clickActiveNode(response.get(0));
		}
	}

	@Override
	public void onNavigationError(String errorMessage) {
		service.showNotification(errorMessage);
	}

}