package com.android.tecla;

import br.ufrj.pee.pocketmotrix.R;
import ca.idrc.tecla.framework.SimpleOverlay;
import ca.idrc.tecla.highlighter.HighlightBoundsView;
import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityNodeInfo;

public class OverlayHighlighter extends SimpleOverlay {

	public static final String TAG = SimpleOverlay.class.getName();

    private final HighlightBoundsView mInnerBounds;
    private final HighlightBoundsView mOuterBounds;
    
	public OverlayHighlighter(Context context) {
		super(context);
		final WindowManager.LayoutParams params = getParams();
		params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;
		params.flags |= WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
		params.flags |= WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
		//params.flags |= WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD;
		params.flags |= WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED;
		setParams(params);
		
		setContentView(R.layout.tecla_highlighter);

		mInnerBounds = (HighlightBoundsView) findViewById(R.id.announce_bounds);
		mInnerBounds.setHighlightColor(Color.WHITE);
		
		
		mOuterBounds = (HighlightBoundsView) findViewById(R.id.bounds);
		mOuterBounds.setHighlightColor(Color.argb(0xdd, 0x38, 0x38, 0x38));
	}

	@Override
	protected void onShow() {
		Log.d(TAG, "Showing Highlighter");
	}

	@Override
	protected void onHide() {
		Log.d(TAG, "Hiding Highlighter");
        mOuterBounds.clear();
        mInnerBounds.clear();
	}
	

	public void clearHighlight() {
        mOuterBounds.clear();
        mInnerBounds.clear();
        mOuterBounds.postInvalidate();
        mInnerBounds.postInvalidate();
	}
	
    public void removeInvalidNodes() {

        mOuterBounds.removeInvalidNodes();
        mOuterBounds.postInvalidate();

        mInnerBounds.removeInvalidNodes();
        mInnerBounds.postInvalidate();
    }

    public void highlightNode(AccessibilityNodeInfo node) {

    	clearHighlight();
        if(node != null) {
            mOuterBounds.setStrokeWidth(20);
            mOuterBounds.add(node);
            mOuterBounds.postInvalidate();        	
            mInnerBounds.setStrokeWidth(6);
            mInnerBounds.add(node);
            mInnerBounds.postInvalidate();
        	
        }
    }
    
}
