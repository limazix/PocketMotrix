package br.ufrj.pee.pocketmotrix.service;

import java.util.ArrayList;
import java.util.LinkedList;
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
import br.ufrj.pee.pocketmotrix.controller.NavigationController;
import br.ufrj.pee.pocketmotrix.engine.SpeakerEngine;
import br.ufrj.pee.pocketmotrix.engine.WriterEngine;
import br.ufrj.pee.pocketmotrix.listener.SpeakerListener;
import br.ufrj.pee.pocketmotrix.listener.WriterListener;

@EService
public class PocketMotrixService extends AccessibilityService implements SpeakerListener, WriterListener {

	private static final String TAG = PocketMotrixService_.class.getName();

	protected static ReentrantLock mActionLock;

	private OverlayBadges mBadges;

	private AccessibilityNodeInfo rootNode;
	private AccessibilityNodeInfo editableField;

	@Bean
	NavigationController navigationController;
	
	@Bean
	WriterEngine writerEngine;

	@Bean
	SpeakerEngine speakerEngine;
	
	@App
	PocketMotrixApp app;

	private boolean isScrolling = false;
	private int direction;

	@Override
	public void onCreate() {

		mActionLock = new ReentrantLock();
		mBadges = new OverlayBadges(getApplicationContext());
	}

	public void activateBadges() {
		if (mBadges != null) {
			if (!mBadges.isVisible()) {
				mBadges.show();
			}
		}
	}

	public void deactivateBadges() {
		if (mBadges != null) {
			if (mBadges.isVisible()) {
				mBadges.hide();
			}
		}
	}

	public ArrayList<AccessibilityNodeInfo> filterBadges(String filter) {
		return mBadges.filterBadges(filter);
	}
	
	@Override
	public void onServiceConnected() {
		super.onServiceConnected();
		activateBadges();
	}

	@AfterInject
	public void settingupEngines() {
		app.setPocketMotrixService(this);
		speakerEngine.setListener(this);
		writerEngine.setWriterListener(this);
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

	public void updateBadgesView(AccessibilityNodeInfo node) {
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
		boolean isLocked = mActionLock.tryLock();

		node.performAction(AccessibilityNodeInfo.ACTION_CLICK);

		if (isLocked) mActionLock.unlock();
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
			setScrolling(true);
			setDirection(direction);
		}
	}

	@Background
	public void scroll(AccessibilityNodeInfo node, int direction) {
		boolean isLocked = mActionLock.tryLock();

		node.performAction(direction);
		setScrolling(false);

		if (isLocked) mActionLock.unlock();
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
		navigationController.finish();
		speakerEngine.finishEngine();
		deactivateBadges();
	}

	public void startWrite() {
		writerEngine.startListening();
	}
	
	@Override
	public void onInterrupt() {

	}

	public boolean isScrolling() {
		return isScrolling;
	}

	public void setScrolling(boolean isScrolling) {
		this.isScrolling = isScrolling;
	}

	public int getDirection() {
		return direction;
	}

	public void setDirection(int direction) {
		this.direction = direction;
	}

	public void showNotification(String message) {
		speakerEngine.speakToUser(message);
		app.setNotificationContentText(message);
		app.setNotificationTicker(message);
		app.showNotification();
	}
	
	@Override
	public void onSpeakerReady() { 
		navigationController.setup();
		writerEngine.setupRecognitionEngine();
	}

	@Override
	public void onSpeakerError(String errorMessage) {
		showNotification(errorMessage);
	}

	@Override
	public void onWriterReady() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onWriterStoped() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onWriteText(String text) {
		writeText(text);
	}

	@Override
	public void onWriterError(String errorMessage) {
		showNotification(errorMessage);
	}


}
