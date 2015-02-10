package br.ufrj.pee.pocketmotrix.service;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Observable;
import java.util.Observer;
import java.util.Queue;
import java.util.concurrent.locks.ReentrantLock;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EService;

import android.accessibilityservice.AccessibilityService;
import android.content.Context;
import android.media.AudioManager;
import android.util.Log;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import br.ufrj.pee.pocketmotrix.engine.SREngine;
import br.ufrj.pee.pocketmotrix.engine.TTSEngine;

import com.android.tecla.OverlayHighlighter;

@EService
public class PocketMotrixService extends AccessibilityService implements
		Observer {

	private static final String TAG = PocketMotrixService_.class.getName();

	private Context context;
	private AudioManager audioManager;

	protected static ReentrantLock mActionLock;
	
	private OverlayHighlighter mHighlighter;
	
	protected AccessibilityNodeInfo mSelectedNode;

	private ArrayList<AccessibilityNodeInfo> mActiveNodes;

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
		mActiveNodes = new ArrayList<AccessibilityNodeInfo>();
		mActionLock = new ReentrantLock();
		
		mHighlighter = new OverlayHighlighter(this);

		context = getApplicationContext();
		audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
	}
	
	private void showHighlighter() {
		if (mHighlighter != null) {
			if (!mHighlighter.isVisible()) {
				mHighlighter.show();
			}
		}
	}
	
	private void hideHighlighter() {
		if (mHighlighter != null) {
			if (mHighlighter.isVisible()) {
				mHighlighter.hide();
			}
		}
	}
	
	public void setFocusedNode(AccessibilityNodeInfo focusedNode) {
		this.focusedNode = focusedNode;
		this.focusedNode.performAction(AccessibilityNodeInfo.ACTION_FOCUS);
	}
	
	@Override
	public void onServiceConnected() {
		super.onServiceConnected();		
		Log.i(TAG, "PocketMotrixService Connected");
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
		
		Log.d(TAG, AccessibilityEvent.eventTypeToString(event_type) + ": " + event.getText());
		
		node = event.getSource();
			if (event_type == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
				setActiveNodes(node);
			}
	}
	
	private void setActiveNodes(AccessibilityNodeInfo root) {
		mActiveNodes.clear();
		AccessibilityNodeInfo fNode = null;
		Queue<AccessibilityNodeInfo> q = new LinkedList<AccessibilityNodeInfo>();
		q.add(root);
		
		while (!q.isEmpty()) {
			AccessibilityNodeInfo thisnode = q.poll();
			if(thisnode == null) continue;
			if(isActive(thisnode) && !thisnode.isScrollable()) {
				mActiveNodes.add(thisnode);
				if(thisnode.isFocused())
					fNode = thisnode;
			}
			for (int i=0; i<thisnode.getChildCount(); ++i) {
				q.add(thisnode.getChild(i));
			}
		}
		
		if(fNode == null) setFocusedNode( mActiveNodes.get(0));
		else setFocusedNode(fNode);
		
	}	
		
	private AccessibilityNodeInfo focusedNode;


	public void selectNode(int direction ) {
		selectNode(focusedNode,  direction );
	}

	public void selectNode(AccessibilityNodeInfo refnode, int direction ) {
		NodeSelectionThread thread = new NodeSelectionThread(this, refnode, direction);
		thread.start();	
	}
	
	protected class NodeSelectionThread extends Thread {
		PocketMotrixService pms;
		AccessibilityNodeInfo current_node;
		int direction; 
		public NodeSelectionThread(PocketMotrixService inst, AccessibilityNodeInfo node, int dir) {
			pms = inst;
			current_node = node;
			direction = dir;
		}
		public void run() {
			mActionLock.lock();
			
			AccessibilityNodeInfo nextNode = current_node.focusSearch(direction);
			if(nextNode != null) {
				pms.setFocusedNode(nextNode);
			}
			mActionLock.unlock();
		}
	}

	public void clickActiveNode() {
		
		focusedNode.performAction(AccessibilityNodeInfo.ACTION_CLICK);
		if(mHighlighter.isVisible()) 
			mHighlighter.clearHighlight();
	}

	private boolean isActive(AccessibilityNodeInfo node) {
		return (node.isVisibleToUser() && node.isFocusable())? true:false;
	}

	@Override
	public void onDestroy() {
		mSREngine.deleteObserver(this);
		mSREngine.finishEngine();
		mTTSEngine.deleteObserver(this);
		mTTSEngine.finishEngine();
		hideHighlighter();
	}

	@Override
	public void update(Observable observable, Object data) {
		if (observable instanceof SREngine) {

			mTTSEngine.speakToUser(mSREngine.getResultText());

			String cmd = mSREngine.getResultText().replaceAll("\\s", "");
			execute(Command.get(cmd));

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
		case ENTER:
			clickActiveNode();
			break;
		case RIGHT:
			selectNode(View.FOCUS_RIGHT);
			break;
		case LEFT:
			selectNode(View.FOCUS_LEFT);
			break;
		case UP:
			selectNode(View.FOCUS_UP);
			break;
		case DOWN:
			selectNode(View.FOCUS_DOWN);
			break;
		case LOUDER:
			audioManager.adjustVolume(AudioManager.ADJUST_RAISE,
					AudioManager.FLAG_SHOW_UI | AudioManager.FLAG_PLAY_SOUND);
			break;
		case QUIETER:
			audioManager.adjustVolume(AudioManager.ADJUST_LOWER,
					AudioManager.FLAG_SHOW_UI | AudioManager.FLAG_PLAY_SOUND);
			break;

		default:
			break;
		}

	}

	@Override
	public void onInterrupt() {
		// TODO Auto-generated method stub
		
	}

}
