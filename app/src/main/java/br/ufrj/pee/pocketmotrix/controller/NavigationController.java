package br.ufrj.pee.pocketmotrix.controller;

import java.util.ArrayList;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;

import android.accessibilityservice.AccessibilityService;
import android.view.accessibility.AccessibilityNodeInfo;
import br.ufrj.pee.pocketmotrix.R;
import br.ufrj.pee.pocketmotrix.engine.CommanderEngine;
import br.ufrj.pee.pocketmotrix.listener.NavigationListener;
import br.ufrj.pee.pocketmotrix.service.Command;

@EBean
public class NavigationController extends AbstractController implements NavigationListener {

	private static final int ARRAY_WITH_ONE_ELEMENT_LENGTH = 1;

	private static final int ARRAY_START_POSITION = 0;

	@Bean
	CommanderEngine commanderEngine;
	
	@Override
	public void setup() {
		super.setup();
		commanderEngine.setNavigationListener(this);
		commanderEngine.setupEngine();
	}
	
	@Override
	public void startEngine() {
		// TODO Fix engine auto start
		
	}

	@Override
	public void stopEngine() {
		commanderEngine.finishEngine();
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
			break;
		case WRITE:
			commanderEngine.toIddle();
			service.startWrite();
			break;
		default:
			break;
		}
	}
	
	@Override
	public void onNavigationCommand(Command cmd) {
		app.showNotification(cmd.getLabel());
		execute(cmd);
	}

	@Override
	public void onNavigationNumber(String number) {
		app.showNotification(number);
		ArrayList<AccessibilityNodeInfo> response = service.filterBadges(number);
		if (response.size() == ARRAY_WITH_ONE_ELEMENT_LENGTH) {
			if (service.isScrolling())
				service.scroll(response.get(ARRAY_START_POSITION), service.getDirection());
			else
				service.clickActiveNode(response.get(ARRAY_START_POSITION));
		}
	}

	@Override
	public void onNavigationError(String errorMessage) {
		app.showNotification(errorMessage);
	}

	@Override
	public void onNavigatorReady() {
		app.showNotification(app.getString(R.string.commander_is_ready));		
	}

}
