/*
 * Copyright (C) 2011 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

// package com.googlecode.eyesfree.widget;
package ca.idrc.tecla.framework;

import android.content.Context;
import android.graphics.PixelFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.view.accessibility.AccessibilityEvent;
import android.widget.FrameLayout;

/**
 * Provides a simple full-screen overlay. Behaves like a
 * {@link android.app.Dialog} but simpler.
 *
 * @author alanv@google.com (Alan Viverette)
 */
public class SimpleOverlay {
    private final Context mContext;
    private final WindowManager mWindowManager;
    private final ViewGroup mContentView;
    private final LayoutParams mParams;

    private SimpleOverlayListener mListener;
    private boolean mVisible;

    /**
     * Creates a new simple overlay.
     *
     * @param context The parent context.
     */
    public SimpleOverlay(Context context) {
        mContext = context;
        mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        mContentView = new SilentFrameLayout(context);

        mParams = new WindowManager.LayoutParams();
        mParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        mParams.format = PixelFormat.TRANSLUCENT;
        mParams.flags |= WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;

        mVisible = false;
    }

    /**
     * @return The overlay context.
     */
    public Context getContext() {
        return mContext;
    }

    /**
     * Sets the listener for overlay visibility callbacks.
     *
     * @param listener
     */
    public void setListener(SimpleOverlayListener listener) {
        mListener = listener;
    }

    /**
     * Shows the overlay. Calls the listener's
     * {@link SimpleOverlayListener#onHide(SimpleOverlay)} if available.
     */
    public final void show() {
        if (mVisible) {
            return;
        }

        mWindowManager.addView(mContentView, mParams);
        mVisible = true;

        if (mListener != null) {
            mListener.onShow(this);
        }

        onShow();
    }

    /**
     * Hides the overlay. Calls the listener's
     * {@link SimpleOverlayListener#onHide(SimpleOverlay)} if available.
     */
    public final void hide() {
        if (!mVisible) {
            return;
        }

        mWindowManager.removeViewImmediate(mContentView);
        mVisible = false;

        if (mListener != null) {
            mListener.onHide(this);
        }

        onHide();
    }

    /**
     * Called after {@link #show()}.
     */
    protected void onShow() {
        // Do nothing.
    }

    /**
     * Called after {@link #hide()}.
     */
    protected void onHide() {
        // Do nothing.
    }

    /**
     * @return A copy of the current layout parameters.
     */
    public LayoutParams getParams() {
        final LayoutParams copy = new LayoutParams();
        copy.copyFrom(mParams);
        return copy;
    }

    /**
     * Sets the current layout parameters and applies them immediately.
     *
     * @param params The layout parameters to use.
     */
    public void setParams(LayoutParams params) {
        mParams.copyFrom(params);

        if (mVisible) {
            mWindowManager.updateViewLayout(mContentView, mParams);
        }
    }

    /**
     * @return {@code true} if this overlay is visible.
     */
    public boolean isVisible() {
        return mVisible;
    }

    /**
     * Inflates the specified resource ID and sets it as the content view.
     *
     * @param layoutResId The layout ID of the view to set as the content view.
     */
    public void setContentView(int layoutResId) {
        final LayoutInflater inflater = LayoutInflater.from(mContext);
        inflater.inflate(layoutResId, mContentView);
    }

    /**
     * Sets the specified view as the content view.
     *
     * @param content The view to set as the content view.
     */
    public void setContentView(View content) {
        mContentView.removeAllViews();
        mContentView.addView(content);
    }

    /**
     * Returns the root {@link View} for this overlay. This is
     * <strong>not</strong> the content view.
     */
    public View getRootView() {
        return mContentView;
    }

    /**
     * Finds and returns the view within the overlay content.
     *
     * @param id The ID of the view to return.
     * @return The view with the specified ID, or {@code null} if not found.
     */
    public View findViewById(int id) {
        return mContentView.findViewById(id);
    }

    /**
     * Handles overlay visibility change callbacks.
     *
     * @author alanv@google.com (Alan Viverette)
     */
    public interface SimpleOverlayListener {
        /**
         * Called after the overlay is displayed.
         *
         * @param overlay The overlay that was displayed.
         */
        public void onShow(SimpleOverlay overlay);

        /**
         * Called after the overlay is hidden.
         *
         * @param overlay The overlay that was hidden.
         */
        public void onHide(SimpleOverlay overlay);
    }

    private static class SilentFrameLayout extends FrameLayout {
        public SilentFrameLayout(Context context) {
            super(context);
        }

        /**
         * In API 14+ this is an override.
         */
        @SuppressWarnings("unused")
        public boolean requestSendAccessibilityEvent(View view, AccessibilityEvent event) {
            // Never send accessibility events.
            return false;
        }
    }
}