package br.ufrj.pee.pocketmotrix.badge;

import org.androidannotations.annotations.EView;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.view.View;
import android.view.accessibility.AccessibilityNodeInfo;

@EView
public class BadgeView extends View {

	private static final int BADGE_RADIUS = 20;
	private static final int DEFAULT_FONT_SIZE = 20;
	private boolean isBold;
	private float mFontSize;
	private final Paint circlePant = new Paint();
	private final Paint textPant = new Paint();
	private Rect nodeBounds = new Rect();
	private Rect badgeTextBounds = new Rect();
	private AccessibilityNodeInfo node;
	private int backgroundColor;
	private int textColor;
	private String label = "";
	
	public BadgeView(Context context) {
		super(context);
		
		circlePant.setStyle(Style.FILL);
		textPant.setStyle(Style.FILL);
		
		isBold = true;
		mFontSize = DEFAULT_FONT_SIZE;
		backgroundColor = Color.DKGRAY;
		textColor = Color.WHITE;
	}
	
	@Override
	protected void onDraw(Canvas c) {
		super.onDraw(c);
		
		circlePant.setColor(backgroundColor);
		textPant.setColor(textColor);
		
		if(node == null) return;
		node.getBoundsInScreen(nodeBounds);
		
		textPant.setTextSize(mFontSize);
		textPant.setFakeBoldText(isBold);
		
		int startPos = 0;
		int endPos = label.length() - 1;
		textPant.getTextBounds(label, startPos, endPos, badgeTextBounds);
		
		float circleCenterX = nodeBounds.left + BADGE_RADIUS;
		float circleCenterY = nodeBounds.top + BADGE_RADIUS;
		
		c.drawCircle(circleCenterX, circleCenterY, BADGE_RADIUS, circlePant);
		
		float textBadgePosX = circleCenterX - badgeTextBounds.width();
		float textBadgePosY = circleCenterY + badgeTextBounds.height()/2;
		
		c.drawText(label, textBadgePosX, textBadgePosY, textPant);
	}
	
	public void add(AccessibilityNodeInfo node, String label) {
		if(node == null) return;
		
		final AccessibilityNodeInfo clone = AccessibilityNodeInfo.obtain(node);
		
		this.node = clone;
		this.label = label;
	}
	
	public void clear() {
		if(node == null) return;
		if(!node.refresh()) node.recycle();
	}

	public boolean isBold() {
		return isBold;
	}

	public void setBold(boolean isBold) {
		this.isBold = isBold;
	}

	public float getFontSize() {
		return mFontSize;
	}

	public void setFontSize(float fontSize) {
		this.mFontSize = fontSize;
	}

	public int getBackgroundColor() {
		return backgroundColor;
	}

	public void setBackgroundColor(int backgroundColor) {
		this.backgroundColor = backgroundColor;
	}

	public int getTextColor() {
		return textColor;
	}

	public void setTextColor(int textColor) {
		this.textColor = textColor;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}
	
}
