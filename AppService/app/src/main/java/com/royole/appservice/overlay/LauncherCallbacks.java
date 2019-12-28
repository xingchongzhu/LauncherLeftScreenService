package com.royole.appservice.overlay;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;

import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;

/**
 * LauncherCallbacks is an interface used to extend the Launcher activity. It includes many hooks
 * in order to add additional functionality. Some of these are very general, and give extending
 * classes the ability to react to Activity life-cycle or specific user interactions. Others
 * are more specific and relate to replacing parts of the application, for example, the search
 * interface or the wallpaper picker.
 */
public interface LauncherCallbacks {

    /*
     * Activity life-cycle methods. These methods are triggered after
     * the code in the corresponding Launcher method is executed.
     */
    void onCreate(Bundle savedInstanceState);
    void onResume();
    void onStart();
    void onStop();
    void onPause();
    void onDestroy();
    void onSaveInstanceState(Bundle outState);
    void onActivityResult(int requestCode, int resultCode, Intent data);
    void onRequestPermissionsResult(int requestCode, String[] permissions,
                                    int[] grantResults);
    void onAttachedToWindow();
    void onDetachedFromWindow();
    void dump(String prefix, FileDescriptor fd, PrintWriter w, String[] args);
    void onHomeIntent(boolean internalStateHandled);
    /**
     * module TW_APP_LAUNCHER3
     * author 晏招弟
     * date 2018/9/27
     * description 新增几个接口方法
     */
    /** start */
    void onConfigurationChanged(Configuration newConfig);
    void onScreenModelChanged();
    void onStateChanged();
    /** end */
    boolean handleBackPressed();
    void onTrimMemory(int level);

    /*
     * Extension points for providing custom behavior on certain user interactions.
     */
    void onLauncherProviderChange();

    /**
     * Starts a search with {@param initialQuery}. Return false if search was not started.
     */
    boolean startSearch(
            String initialQuery, boolean selectInitialQuery, Bundle appSearchData);

    /*
     * Extensions points for adding / replacing some other aspects of the Launcher experience.
     */
    boolean hasSettings();
}
