package br.ufrj.pee.pocketmotrix.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Observable;
import java.util.Observer;
import java.util.Queue;
import java.util.concurrent.locks.ReentrantLock;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EService;

import android.accessibilityservice.AccessibilityService;
import android.app.KeyguardManager;
import android.app.KeyguardManager.KeyguardLock;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import br.ufrj.pee.pocketmotrix.badge.OverlayBadges;
import br.ufrj.pee.pocketmotrix.engine.SREngine;
import br.ufrj.pee.pocketmotrix.engine.TTSEngine;

@EService
public class PocketMotrixService extends AccessibilityService implements
		Observer {

	private static final String TAG = PocketMotrixService_.class.getName();

	private final static HashMap<String, String> numbers = new HashMap<String, String>();
	static {
		numbers.put("one",		"1");
		numbers.put("two",		"2");
		numbers.put("three",	"3");
		numbers.put("four", 	"4");
		numbers.put("five", 	"5");
		numbers.put("six", 		"6");
		numbers.put("seven",	"7");
		numbers.put("eight", 	"8");
		numbers.put("nine", 	"9");
		numbers.put("zero", 	"0");
	};
	
	private Context context;
	private AudioManager audioManager;
	private PowerManager powerManager;
	private WakeLock wakeLock;
	private KeyguardLock keyguardLock;
	private KeyguardManager keyguardManager;

	protected static ReentrantLock mActionLock;

	private OverlayBadges mBadges;

	private AccessibilityNodeInfo rootNode;

	@Bean
	SREngine mSREngine;

	@Bean
	TTSEngine mTTSEngine;

	@Override
	public void onCreate() {
		Log.i(TAG, "PocketMotrixService Created");

		init();
	}

	private void init() {
		mActionLock = new ReentrantLock();
		

		context = getApplicationContext();
		
		mBadges = new OverlayBadges(context);
		
		audioManager = (AudioManager) context
				.getSystemService(Context.AUDIO_SERVICE);
		
		powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
		wakeLock = powerManager.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP|PowerManager.FULL_WAKE_LOCK|PowerManager.ON_AFTER_RELEASE, TAG);
		
		keyguardManager = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
		keyguardLock = keyguardManager.newKeyguardLock(TAG);
		
		registerReceiver(mReceiver, new IntentFilter(Intent.ACTION_SCREEN_OFF));
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
		Log.i(TAG, "PocketMotrixService Connected");
		activateBadges();
	}

	@AfterInject
	public void settingupEngines() {
		mTTSEngine.addObserver(this);
		mSREngine.addObserver(this);
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
			if(!event.getText().isEmpty()) updateBadgesView(node);
		} else if (event_type == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
			updateBadgesView(node);
		} else if (event_type == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {
			updateBadgesView(node);
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
		
		if(node == null) return;
		
		AccessibilityNodeInfo current = AccessibilityNodeInfo.obtain(node);
		AccessibilityNodeInfo parent = current.getParent();
		
		if(parent == null) {
			if(rootNode == null) rootNode = current;
			else {
				AccessibilityNodeInfo oldRoot = rootNode;
				rootNode = current;
				if(!oldRoot.refresh()) oldRoot.recycle();
			}
		}
		else setRootNodeView(parent);
	}

	@Background
	public void clickActiveNode(AccessibilityNodeInfo node) {
		node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
	}

	public void scroll(int direction) {
		//scroll(focusedNode, direction);
	}

	@Background
	public void scroll(AccessibilityNodeInfo node, int direction) {
		boolean isLocked = mActionLock.tryLock();
		
		boolean actionPerformed = false;
		
		if (node.isScrollable() && node.isVisibleToUser()) {
			actionPerformed = node.performAction(direction);
		}
		
		if (actionPerformed || node.getParent() == null) return;
		else scroll(node.getParent(), direction);
		
		if(isLocked) mActionLock.unlock();
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

	/**
     * Returns whether a node is actionable. That is, the node supports one of
     * the following actions:
     * <ul>
     * <li>{@link AccessibilityNodeInfo#isClickable()}
     * <li>{@link AccessibilityNodeInfo#isFocusable()}
     * <li>{@link AccessibilityNodeInfo#isLongClickable()}
     * </ul>
     * This parities the system method View#isActionableForAccessibility(), which
     * was added in JellyBean.
     *
     * @param node The node to examine.
     * @return {@code true} if node is actionable.
     */
    public static boolean isActionableForAccessibility(AccessibilityNodeInfo node) {
        if (node == null) return false;
        
        if(!node.isVisibleToUser()) return false;

        // Nodes that are clickable are always actionable.
        if (isClickable(node) || isLongClickable(node)) return true;

        if (node.isFocusable()) return true;

        return supportsAnyAction(node, AccessibilityNodeInfo.ACTION_FOCUS,
                AccessibilityNodeInfo.ACTION_NEXT_HTML_ELEMENT,
                AccessibilityNodeInfo.ACTION_PREVIOUS_HTML_ELEMENT);
    }
    
    /**
     * Returns whether a node is clickable. That is, the node supports at least one of the
     * following:
     * <ul>
     * <li>{@link AccessibilityNodeInfo#isClickable()}</li>
     * <li>{@link AccessibilityNodeInfo#ACTION_CLICK}</li>
     * </ul>
     *
     * @param node The node to examine.
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
     * Returns whether a node is long clickable. That is, the node supports at least one of the
     * following:
     * <ul>
     * <li>{@link AccessibilityNodeInfo#isLongClickable()}</li>
     * <li>{@link AccessibilityNodeInfo#ACTION_LONG_CLICK}</li>
     * </ul>
     *
     * @param node The node to examine.
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
     * @param node The node to check.
     * @param actions The actions to check.
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
		mSREngine.deleteObserver(this);
		mSREngine.finishEngine();
		mTTSEngine.deleteObserver(this);
		mTTSEngine.finishEngine();
		deactivateBadges();
	}

	@Override
	public void update(Observable observable, Object data) {
		if (observable instanceof SREngine) {

			mTTSEngine.speakToUser(mSREngine.getResultText());
			
			String cmd = mSREngine.getResultText().replaceAll("\\s", "");
			
			if(numbers.containsKey(cmd)) { 
				ArrayList<AccessibilityNodeInfo> response = mBadges.filterBadges(numbers.get(cmd));
				if(response.size() == 1) clickActiveNode(response.get(0));
			} else execute(Command.get(cmd));
			
		} else if (observable instanceof TTSEngine) {
			if (mTTSEngine.getIsInitialized())
				mSREngine.setupEngine();
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
		case GO_RIGHT:
			scroll(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD);
			break;
		case GO_DOWN:
			scroll(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD);
			break;
		case GO_LEFT:
			scroll(AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD);
			break;
		case GO_UP:
			scroll(AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD);
			break;
		case LOUDER:
			audioManager.adjustVolume(AudioManager.ADJUST_RAISE,
					AudioManager.FLAG_SHOW_UI | AudioManager.FLAG_PLAY_SOUND);
			break;
		case QUIETER:
			audioManager.adjustVolume(AudioManager.ADJUST_LOWER,
					AudioManager.FLAG_SHOW_UI | AudioManager.FLAG_PLAY_SOUND);
			break;
		case WAKE_OR_KEEP_WAKE:
			keyguardLock.disableKeyguard();
			wakeLock.acquire();
			break;
		case RELEASE_KEEP_WAKE:
			if(wakeLock.isHeld()) wakeLock.release();
			break;
		case REFRESH:
			updateBadgesView(rootNode);
		default:
			break;
		}

	}

	@Override
	public void onInterrupt() {
		// TODO Auto-generated method stub

	}
	
	private BroadcastReceiver mReceiver = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			if(intent.getAction().equals(Intent.ACTION_SCREEN_OFF))
				keyguardLock.reenableKeyguard();
		}
	}; 

}
