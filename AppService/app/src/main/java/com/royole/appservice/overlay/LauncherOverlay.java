package com.royole.appservice.overlay;

import android.view.WindowManager;

public interface LauncherOverlay {
    /**
     * Touch interaction leading to overscroll has begun
     */
    public void onScrollInteractionBegin();

    /**
     * Touch interaction related to overscroll has ended
     */
    public void onScrollInteractionEnd();

    /**
     * Touch interaction related to overscroll has ended
     */
    public void onScrollInteractionEnd(float velocityX);

    /**
     * Scroll progress, between 0 and 100, when the user scrolls beyond the leftmost
     * screen (or in the case of RTL, the rightmost screen).
     */
    public void onScrollChange(float progress, boolean rtl);

    /**
     * Called when the launcher is ready to use the overlay
     *
     * @param callbacks A set of callbacks provided by Launcher in relation to the overlay
     */
    public void setOverlayCallbacks(LauncherOverlayCallbacks callbacks);
}