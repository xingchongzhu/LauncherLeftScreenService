package com.royole.appservice.views;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.FrameLayout;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.royole.appservice.R;
import com.royole.appservice.anim.Interpolators;
import com.royole.appservice.model.ScreenState;
import com.royole.appservice.overlay.LauncherCallbacks;
import com.royole.appservice.overlay.LauncherOverlay;
import com.royole.appservice.overlay.LauncherOverlayCallbacks;
import com.royole.appservice.utils.Utilties;

import java.io.FileDescriptor;
import java.io.PrintWriter;

import static com.royole.appservice.Messages.WHAT_HIDE;
import static com.royole.appservice.Messages.WHAT_UPDATE_SCROLL;

public class LeftmostWorkspace extends FrameLayout implements LauncherCallbacks , LauncherOverlay {

    private final static String TAG = LeftmostWorkspace.class.getSimpleName();
    private static final float RETURN_TO_ORIGINAL_PAGE_THRESHOLD = 0.12f;
    private Messenger mRemoteMessenger;
    private boolean canScroll = true;
    private int startX;
    public float curOffsetProgress;
    private int width;
    private boolean isLeftmostWorkspaceShown = false;

    ValueAnimator animator;

    boolean toDesktop = false;
    boolean onHomeKey = false;
    private int mLastXIntercept;
    private int mLastYIntercept;
    private long beginScrollTime;
    private ScreenState mScreenState;
    private String error = null;
    public LeftmostWorkspace(@NonNull Context context) {
        this(context, null);
    }

    public LeftmostWorkspace(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, -1);
    }

    public LeftmostWorkspace(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, -1);
    }

    public LeftmostWorkspace(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
        initView();
        mScreenState = new ScreenState();
    }

    public ScreenState getScreenState() {
        return mScreenState;
    }

    private void initView() {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.leftmost_workspace, this, true);
        Button btn = (Button) view.findViewById(R.id.btn);
        btn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                //Message msg = Message.obtain(null, WHAT_HIDE);
                //sendRemote(msg);
                if(error.equals("ddd")){

                }
            }
        });
    }

    private void init(){
        width = Utilties.getScreenMaxWidth(getContext());
    }

    public void setMessenger(Messenger mMessenger) {
        this.mRemoteMessenger = mMessenger;
    }

    public void setCanScroll(boolean canScroll) {
        this.canScroll = canScroll;
    }

    private boolean canScroll() {
        return canScroll;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        boolean intercepted = false;
        int x = (int) ev.getRawX();
        int y = (int) ev.getRawY();
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startX = x;
                //startY = y;
                intercepted = false;
                break;
            case MotionEvent.ACTION_MOVE:
                if (!canScroll()) {
                    return false;
                }

                int deltaX = x - mLastXIntercept;
                int deltaY = y - mLastYIntercept;
                intercepted = Math.abs(deltaX) > Math.abs(deltaY);
                if(intercepted){
                    onTouchEvent(ev);
                }
                break;
            case MotionEvent.ACTION_UP:
                intercepted = false;
                break;
            default:
                break;
        }
        mLastXIntercept = x;
        mLastYIntercept = y;
//        startX = x;
//        startY = y;
        return intercepted;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int x = (int) event.getRawX();
        int y = (int) event.getRawY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // yzd 只有当处于正常模式时才能滑动到负一屏
                if (!canScroll()) {
                    return false;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (x < startX /*&& (Math.abs(y - startY) < Math.abs(x - startX))*/) {
                    curOffsetProgress = (float) (1 - (startX - x) / (width * 1.0));
                    setTranslationX(-(1 - curOffsetProgress) * width);
                    updateViewAlpha();
                    Message msg = Message.obtain(null, WHAT_UPDATE_SCROLL);
                    msg.arg1 = Float.floatToIntBits(curOffsetProgress);
                    sendRemote(msg);
                }
                break;
            case MotionEvent.ACTION_UP:
                if (x < startX) {
                    if (curOffsetProgress <= 1 - RETURN_TO_ORIGINAL_PAGE_THRESHOLD) {
                        startProcessAnimator(curOffsetProgress, 0);
                    } else {
                        startProcessAnimator(curOffsetProgress, 1);
                    }
                }
                break;
            default:break;
        }
        return true;
    }

    private synchronized boolean sendRemote(Message msg) {
        if (this.mRemoteMessenger == null) {
            Log.d(TAG, "sendRemote mRemoteMessenger is null");
            return false;
        }
        try {
            this.mRemoteMessenger.send(msg);
            return true;
        } catch (RemoteException e) {
            handleConnectionError(e);
        }
        return false;
    }

    private synchronized void handleConnectionError(RemoteException e) {
         Log.e(TAG,  " handleConnectionError", e);
    }

    public void startProcessAnimator(float from, float to) {
        isLeftmostWorkspaceShown = to == 1;
        if (animator != null && animator.isRunning()) {
            animator.cancel();
        }
        animator = ValueAnimator.ofFloat(from, to);

        animator.setDuration(200);

        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                    float offset = (float)animation.getAnimatedValue();
                    curOffsetProgress = offset;
                    Message msg = Message.obtain(null, WHAT_UPDATE_SCROLL);
                    msg.arg1 = Float.floatToIntBits(curOffsetProgress);
                    sendRemote(msg);
                    setTranslationX(-(1 - curOffsetProgress) * width);
                    updateViewAlpha();
                    if (curOffsetProgress == 1) {
                        /***
                         * module TW_APP_LAUNCHER3
                         * author 胡冰
                         * date 2018/9/27
                         * description 校验授权
                         ***/
                        if (!toDesktop && !onHomeKey) {
                            //onLeftmostShow();
                        }
                        toDesktop = false;
                    }
                    if (curOffsetProgress == 0 && onHomeKey) {
                        onHomeKey = false;
                    }
                }
        });

        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if(curOffsetProgress == 0) {
                    setVisibility(GONE);
                }else{
                    setVisibility(VISIBLE);
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        animator.start();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

    }

    @Override
    public void onResume() {
        /*
         * module TW_APP_LAUNCHER3
         * author 胡冰
         * date 2019/5/15
         * description 负一屏
         */
        if (/*Launcher.curScreenModel != Launcher.SCREEN_MODEL_AUXILIARY &&*/ curOffsetProgress == 0) {
            width = Utilties.getScreenMaxWidth(getContext());
            setTranslationX(-width);
        } else {
            //onLeftmostShow();
            //sortCardsUI();
        }
    }

    @Override
    public void onStart() {

    }

    @Override
    public void onStop() {

    }

    @Override
    public void onPause() {
        hide();
    }

    @Override
    public void onDestroy() {

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }

    @Override
    public void dump(String prefix, FileDescriptor fd, PrintWriter w, String[] args) {

    }

    @Override
    public void onHomeIntent(boolean internalStateHandled) {

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        Log.d(TAG,"onConfigurationChanged");
        width = Utilties.getScreenMaxWidth(getContext());
        onScreenModelChanged();
    }

    public int lastScreenModel = 0;
    @Override
    public void onScreenModelChanged() {
        float lastOffsetProgress = curOffsetProgress;
        if (curOffsetProgress == 1) {
            /**
             * module TW_APP_LAUNCHER3
             * author 晏招弟
             * date 2019/7/19
             * description 只有当在NORMAL时才能显示负一屏
             */
            if (canScroll()) {
                show();
            } else {
                curOffsetProgress = 0;
            }
        } else if (curOffsetProgress == 0) {
            hide();
        }
    }

    /**
     * 隐藏负一屏
     */
    public void hide(){
        setTranslationX(-width);
        setVisibility(GONE);
        startProcessAnimator(curOffsetProgress, 0);
        curOffsetProgress = 0;
        Message msg = Message.obtain(null, WHAT_UPDATE_SCROLL);
        msg.arg1 = Float.floatToIntBits(curOffsetProgress);
        sendRemote(msg);
    }

    /**
     * 显示负一屏
     */
    public void show(){
        setTranslationX(0);
        setVisibility(VISIBLE);
        startProcessAnimator(curOffsetProgress, 1);
        curOffsetProgress = 1;
        Message msg = Message.obtain(null, WHAT_UPDATE_SCROLL);
        msg.arg1 = Float.floatToIntBits(curOffsetProgress);
        sendRemote(msg);
    }

    @Override
    public void onStateChanged() {

    }

    @Override
    public boolean handleBackPressed() {
        return false;
    }

    @Override
    public void onTrimMemory(int level) {

    }

    @Override
    public void onLauncherProviderChange() {

    }

    @Override
    public boolean startSearch(String initialQuery, boolean selectInitialQuery, Bundle appSearchData) {
        return false;
    }

    @Override
    public boolean hasSettings() {
        return false;
    }

    @Override
    public void onScrollInteractionBegin() {
        beginScrollTime = System.currentTimeMillis();
        setVisibility(VISIBLE);
    }

    @Override
    public void setVisibility(int visibility) {
        Log.d(TAG,"setVisibility visibility = "+visibility);
        super.setVisibility(visibility);
    }

    @Override
    public void onScrollInteractionEnd() {
        if (!canScroll()) {
            return;
        }

        long endScrollTime = System.currentTimeMillis();

        if (endScrollTime - beginScrollTime < 100) {
            startProcessAnimator(curOffsetProgress, 1);
        } else {
            if (isLeftmostWorkspaceShown) {
                if (curOffsetProgress <= 1 - RETURN_TO_ORIGINAL_PAGE_THRESHOLD) {
                    startProcessAnimator(curOffsetProgress, 0);
                } else {
                    startProcessAnimator(curOffsetProgress, 1);
                }
            } else {
                if (curOffsetProgress >= RETURN_TO_ORIGINAL_PAGE_THRESHOLD) {
                    startProcessAnimator(curOffsetProgress, 1);
                } else {
                    startProcessAnimator(curOffsetProgress, 0);
                }
            }
        }
    }

    @Override
    public void onScrollInteractionEnd(float velocityX) {
        onScrollInteractionEnd();
    }

    @Override
    public void onScrollChange(float progress, boolean rtl) {

        if (!canScroll()) {
            return;
        }
        curOffsetProgress = progress;
        Message msg = Message.obtain(null, WHAT_UPDATE_SCROLL);
        msg.arg1 = Float.floatToIntBits(curOffsetProgress);
        sendRemote(msg);
        setTranslationX(-(1 - curOffsetProgress) * width);
        updateViewAlpha();
    }

    /**
     * 负一屏透明度渐变
     */
    public void updateViewAlpha(){
        //float alpha = Interpolators.DEACCEL_3.getInterpolation(curOffsetProgress);
        //setAlpha(alpha);
    }

    @Override
    public void setOverlayCallbacks(LauncherOverlayCallbacks callbacks) {

    }
}
