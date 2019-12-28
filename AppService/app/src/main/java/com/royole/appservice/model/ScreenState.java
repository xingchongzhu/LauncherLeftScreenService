package com.royole.appservice.model;

import android.content.Intent;

public class ScreenState {
    private boolean mIsRtl;
    private boolean mIsLightStatusBar;
    private boolean mIsStatusBarHidden;
    private boolean mIsLightNavigationBar;
    private boolean mIsTranslucentNavigationBar;

    public boolean isIsRtl() {
        return mIsRtl;
    }

    public void setIsRtl(boolean mIsRtl) {
        this.mIsRtl = mIsRtl;
    }

    public boolean isIsLightStatusBar() {
        return mIsLightStatusBar;
    }

    public void setIsLightStatusBar(boolean mIsLightStatusBar) {
        this.mIsLightStatusBar = mIsLightStatusBar;
    }

    public boolean isIsStatusBarHidden() {
        return mIsStatusBarHidden;
    }

    public void setIsStatusBarHidden(boolean mIsStatusBarHidden) {
        this.mIsStatusBarHidden = mIsStatusBarHidden;
    }

    public boolean isIsLightNavigationBar() {
        return mIsLightNavigationBar;
    }

    public void setIsLightNavigationBar(boolean mIsLightNavigationBar) {
        this.mIsLightNavigationBar = mIsLightNavigationBar;
    }

    public boolean isIsTranslucentNavigationBar() {
        return mIsTranslucentNavigationBar;
    }

    public void setIsTranslucentNavigationBar(boolean mIsTranslucentNavigationBar) {
        this.mIsTranslucentNavigationBar = mIsTranslucentNavigationBar;
    }
}
