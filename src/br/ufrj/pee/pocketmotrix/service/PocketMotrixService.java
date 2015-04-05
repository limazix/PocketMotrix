package br.ufrj.pee.pocketmotrix.service;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Observable;
import java.util.Observer;
import java.util.Queue;
import java.util.concurrent.locks.ReentrantLock;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.App;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EService;

import android.accessibilityservice.AccessibilityService;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import br.ufrj.pee.pocketmotrix.app.PocketMotrixApp;
import br.ufrj.pee.pocketmotrix.badge.OverlayBadges;
import br.ufrj.pee.pocketmotrix.engine.GoogleSREngine;
import br.ufrj.pee.pocketmotrix.engine.SREngine;
import br.ufrj.pee.pocketmotrix.engine.TTSEngine;
import br.ufrj.pee.pocketmotrix.listener.NavigationListener;
import br.ufrj.pee.pocketmotrix.util.PocketMotrixUtils;

@EService
public class PocketMotrixService extends AccessibilityService implements
		NavigationListener, Observer {

	private static final String TAG = PocketMotrixService_.class.getName();

	public static enum MODE {
		IDDLE,
		NAVIGATION,
		WRITE
	};

	protected static ReentrantLock mActionLock;

	private OverlayBadges mBadges;

	private AccessibilityNodeInfo rootNode;
	private AccessibilityNodeInfo editableField;

	@Bean
	SREngine mSREngine;
	
	@Bean
	GoogleSREngine gSREngine;

	@Bean
	TTSEngine mTTSEngine;
	
	@App
	PocketMotrixApp app;

	private boolean isScrolling = false;
	private int direction;

	@Override
	public void onCreate() {

		mActionLock = new ReentrantLock();

		mBadges = new OverlayBadges(getApplicationContext());
		
	}

	private void activateBadges() {
		if (mBadges != null) {
			if (!mBadges.isVisible()) {
				mBadges.show();
			}
		}
	}

	private void deactivateBadges() {
		if (mBadges != null) {
			if (mBadges.isVisible()) {
				mBadges.hide();
			}
		}
	}

	@Override
	public void onServiceConnected() {
		super.onServiceConnected();
		activateBadges();
	}

	@AfterInject
	public void settingupEngines() {
		mSREngine.setNavigationListener(this);
		mTTSEngine.addObserver(this);
		gSREngine.addObserver(this);
	}

	@Override
	public void onAccessibilityEvent(AccessibilityEvent event) {
		int event_type = event.getEventType();

		AccessibilityNodeInfo node = event.getSource();
		if (node == null) {
			return;
		}

		if (event_type != AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {
			Log.d(TAG, AccessibilityEvent.eventTypeToString(event_type) + ": "
					+ event.getText());
		}

		if (event_type == AccessibilityEvent.TYPE_VIEW_SCROLLED) {
			if (!event.getText().isEmpty())
				updateBadgesView(node);
		} else if (event_type == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
			updateBadgesView(node);
		} else if (event_type == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {
			updateBadgesView(node);
		} else if (event_type == AccessibilityEvent.TYPE_VIEW_FOCUSED || event_type == AccessibilityEvent.TYPE_VIEW_CLICKED) {
			if (node.isVisibleToUser() && node.isEditable())
				editableField = node;
		}

	}

	private void updateBadgesView(AccessibilityNodeInfo node) {
		mBadges.clearBadges();
		AccessibilityNodeInfo root = getRootInActiveWindow();
		if (root != null)
			rootNode = root;
		else
			setRootNodeView(node);
		enumerate();
	}

	private void setRootNodeView(AccessibilityNodeInfo node) {

		if (node == null)
			return;

		AccessibilityNodeInfo current = AccessibilityNodeInfo.obtain(node);
		AccessibilityNodeInfo parent = current.getParent();

		if (parent == null) {
			if (rootNode == null)
				rootNode = current;
			else {
				AccessibilityNodeInfo oldRoot = rootNode;
				rootNode = current;
				if (!oldRoot.refresh())
					oldRoot.recycle();
			}
		} else
			setRootNodeView(parent);
	}

	@Background
	public void clickActiveNode(AccessibilityNodeInfo node) {
		node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
	}

	private void getScrollables(AccessibilityNodeInfo node,
			ArrayList<AccessibilityNodeInfo> list) {

		if (node == null)
			return;

		AccessibilityNodeInfo current = AccessibilityNodeInfo.obtain(node);

		if (current.isVisibleToUser() && current.isScrollable())
			list.add(current);

		for (int i = 0; i < current.getChildCount(); i++)
			getScrollables(current.getChild(i), list);

	}

	public void scroll(int direction) {
		ArrayList<AccessibilityNodeInfo> scrollables = new ArrayList<AccessibilityNodeInfo>();
		getScrollables(rootNode, scrollables);

		if (scrollables.size() == 1)
			scroll(scrollables.get(0), direction);
		else if (scrollables.size() > 1) {
			mBadges.addBadges(scrollables);
			isScrolling = true;
			this.direction = direction;
		}
	}

	@Background
	public void scroll(AccessibilityNodeInfo node, int direction) {
		boolean isLocked = mActionLock.tryLock();

		node.performAction(direction);

		if (isLocked)
			mActionLock.unlock();
	}

	private void enumerate() {
		ArrayList<AccessibilityNodeInfo> actionables = new ArrayList<AccessibilityNodeInfo>();
		Queue<AccessibilityNodeInfo> q = new LinkedList<AccessibilityNodeInfo>();
		q.add(rootNode);

		while (!q.isEmpty()) {
			AccessibilityNodeInfo thisnode = q.poll();
			if (thisnode == null)
				continue;
			if (isActionableForAccessibility(thisnode)) {
				actionables.add(thisnode);
			}
			for (int i = 0; i < thisnode.getChildCount(); ++i) {
				q.add(thisnode.getChild(i));
			}
		}

		mBadges.addBadges(actionables);
	}

	@Background
	public void writeText(String text) {
		boolean isLocked = mActionLock.tryLock();
		
		if (editableField == null || !editableField.isVisibleToUser())
			return;

		app.wrteOnClipboard(text.concat(" "));
		editableField.performAction(AccessibilityNodeInfo.ACTION_PASTE);

		if(isLocked) mActionLock.unlock();
	}

	/**
	 * Returns whether a node is actionable. That is, the node supports one of
	 * the following actions:
	 * <ul>
	 * <li>{@link AccessibilityNodeInfo#isClickable()}
	 * <li>{@link AccessibilityNodeInfo#isFocusable()}
	 * <li>{@link AccessibilityNodeInfo#isLongClickable()}
	 * </ul>
	 * This parities the system method View#isActionableForAccessibility(),
	 * which was added in JellyBean.
	 *
	 * @param node
	 *            The node to examine.
	 * @return {@code true} if node is actionable.
	 */
	public static boolean isActionableForAccessibility(
			AccessibilityNodeInfo node) {
		if (node == null)
			return false;

		if (!node.isVisibleToUser())
			return false;

		// Nodes that are clickable are always actionable.
		if (isClickable(node) || isLongClickable(node))
			return true;

		if (node.isFocusable())
			return true;

		return supportsAnyAction(node, AccessibilityNodeInfo.ACTION_FOCUS,
				AccessibilityNodeInfo.ACTION_NEXT_HTML_ELEMENT,
				AccessibilityNodeInfo.ACTION_PREVIOUS_HTML_ELEMENT);
	}

	/**
	 * Returns whether a node is clickable. That is, the node supports at least
	 * one of the following:
	 * <ul>
	 * <li>{@link AccessibilityNodeInfo#isClickable()}</li>
	 * <li>{@link AccessibilityNodeInfo#ACTION_CLICK}</li>
	 * </ul>
	 *
	 * @param node
	 *            The node to examine.
	 * @return {@code true} if node is clickable.
	 */
	public static boolean isClickable(AccessibilityNodeInfo node) {
		if (node == null) {
			return false;
		}

		if (node.isClickable()) {
			return true;
		}

		return supportsAnyAction(node, AccessibilityNodeInfo.ACTION_CLICK);
	}

	/**
	 * Returns whether a node is long clickable. That is, the node supports at
	 * least one of the following:
	 * <ul>
	 * <li>{@link AccessibilityNodeInfo#isLongClickable()}</li>
	 * <li>{@link AccessibilityNodeInfo#ACTION_LONG_CLICK}</li>
	 * </ul>
	 *
	 * @param node
	 *            The node to examine.
	 * @return {@code true} if node is long clickable.
	 */
	public static boolean isLongClickable(AccessibilityNodeInfo node) {
		if (node == null) {
			return false;
		}

		if (node.isLongClickable()) {
			return true;
		}

		return supportsAnyAction(node, AccessibilityNodeInfo.ACTION_LONG_CLICK);
	}

	/**
	 * Returns {@code true} if the node supports at least one of the specified
	 * actions. To check whether a node supports multiple actions, combine them
	 * using the {@code |} (logical OR) operator.
	 *
	 * @param node
	 *            The node to check.
	 * @param actions
	 *            The actions to check.
	 * @return {@code true} if at least one action is supported.
	 */
	public static boolean supportsAnyAction(AccessibilityNodeInfo node,
			int... actions) {
		if (node != null) {
			final int supportedActions = node.getActions();

			for (int action : actions) {
				if ((supportedActions & action) == action) {
					return true;
				}
			}
		}

		return false;
	}

	@Override
	public void onDestroy() {
		mSREngine.finishEngine();
		mTTSEngine.deleteObserver(this);
		mTTSEngine.finishEngine();
		deactivateBadges();
	}

	@Override
	public void update(Observable observable, Object data) {

//			mTTSEngine.speakToUser(mSREngine.getResultText());
			
		if (observable instanceof GoogleSREngine) {
			String text = gSREngine.getMatch();
			writeText(text);
		} else if (observable instanceof TTSEngine) {
			if (mTTSEngine.getIsInitialized()) {
				showNotification("TTS initialized");
				mSREngine.setupEngine();
				gSREngine.setupRecognitionEngine();
			}
		}
	}

	private void execute(Command cmd) {

		switch (cmd) {
		case GO_HOME:
			performGlobalAction(GLOBAL_ACTION_HOME);
			break;
		case GO_BACK:
			performGlobalAction(GLOBAL_ACTION_BACK);
			break;
		// TODO action has no effect
		case POWER:
			performGlobalAction(GLOBAL_ACTION_POWER_DIALOG);
			break;
		case NOTIFICATION:
			performGlobalAction(GLOBAL_ACTION_NOTIFICATIONS);
			break;
		// TODO improve badges not updating after scroll
		case HISTORY:
			performGlobalAction(GLOBAL_ACTION_RECENTS);
			break;
		case SETTINGS:
			performGlobalAction(GLOBAL_ACTION_QUICK_SETTINGS);
			break;
		case GO_FORWARD:
			scroll(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD);
			break;
		case GO_BACKWARD:
			scroll(AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD);
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
			activateBadges();
			updateBadgesView(rootNode);
			break;
		case CLEAR:
			deactivateBadges();
		default:
			break;
		}

	}

	@Override
	public void onInterrupt() {

	}

	private void showNotification(String message) {
		app.setNotificationContentText(message);
		app.setNotificationTicker(message);
		app.showNotification();
	}
	
	@Override
	public void onNavigationCommand(Command cmd) {
		showNotification(cmd.getLabel());
		if(cmd.equals(Command.WRITE)) {
			mSREngine.toIddle();
			gSREngine.startListening();
		} else execute(cmd);
	}

	@Override
	public void onNavigationNumber(String number) {
		showNotification(number);
		ArrayList<AccessibilityNodeInfo> response = mBadges
				.filterBadges(PocketMotrixUtils.numbers.get(number));
		if (response.size() == 1) {
			if (isScrolling)
				scroll(response.get(0), direction);
			else
				clickActiveNode(response.get(0));
		}
	}

	@Override
	public void onNavigationError(String errorMessage) {
		showNotification(errorMessage);
	}


}
