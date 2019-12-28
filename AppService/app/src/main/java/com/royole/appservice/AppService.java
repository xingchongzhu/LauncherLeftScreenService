package com.royole.appservice;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.royole.appservice.views.LeftmostWorkspace;

import static com.royole.appservice.Messages.FLAG_HIDDEN_STATUS_BAR;
import static com.royole.appservice.Messages.FLAG_LIGHT_NAVIGATION_BAR;
import static com.royole.appservice.Messages.FLAG_LIGHT_STATUS_BAR;
import static com.royole.appservice.Messages.FLAG_RTL;
import static com.royole.appservice.Messages.FLAG_TRANSLUCENT_NAVIGATION_BAR;
import static com.royole.appservice.Messages.WHAT_END_SCROLL;
import static com.royole.appservice.Messages.WHAT_HIDE;
import static com.royole.appservice.Messages.WHAT_ON_ATTACHED_TO_WINDOW;
import static com.royole.appservice.Messages.WHAT_ON_PAUSE;
import static com.royole.appservice.Messages.WHAT_ON_RESUME;
import static com.royole.appservice.Messages.WHAT_SIDE_SCREEN_ATTACHED;
import static com.royole.appservice.Messages.WHAT_START_SCROLL;
import static com.royole.appservice.Messages.WHAT_UPDATE_SCROLL;

public class AppService extends Service {

    private static final String TAG = "AppService";
    private WindowManager mWindowManager;
    private WindowManager.LayoutParams mParams;
    private LeftmostWorkspace mLeftmostWorkspace;
    private Context mContext;
    private Handler mHandler = new Handler();
    private Messenger mRemoteMessenger;

    @Override
    public void onCreate() {
        super.onCreate();
        initApplicationWindow();
        mLeftmostWorkspace = (LeftmostWorkspace) LayoutInflater.from(this).inflate(R.layout.window_view, null);
    }

    private void initApplicationWindow() {
        try{
            mContext = this.createPackageContext("com.royole.appservice", Context.CONTEXT_IGNORE_SECURITY);
            Log.i(TAG,"mContext =" +mContext.getApplicationInfo().toString());
            mWindowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);

            mParams = new WindowManager.LayoutParams();
            //View以外的区域可以响应点击和触摸事件
            //mParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;

            //设置window type 下面变量2002是在屏幕区域显示，2003则可以显示在状态栏之上
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                mParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
            } else {
                mParams.type = WindowManager.LayoutParams.TYPE_PHONE;
            }
            //mParams.type = WindowManager.LayoutParams.FIRST_SUB_WINDOW;
            mParams.format = PixelFormat.RGBA_8888;
            mParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;
            mParams.gravity = Gravity.TOP | Gravity.LEFT;
            mParams.width = WindowManager.LayoutParams.MATCH_PARENT;
            mParams.height = WindowManager.LayoutParams.MATCH_PARENT;
        } catch (PackageManager.NameNotFoundException n){
            n.printStackTrace();
        }
    }

    private void showWindow(){
        Log.d(TAG,"showWindow");
        try {
            Log.d(TAG,"hideWindow");
            mWindowManager.addView(mLeftmostWorkspace, mParams);
        }catch (Exception e){
            e.printStackTrace();
            Log.e(TAG,"showWindow e = "+e);
        }
    }

    private void hideWindow(){
        try {
            Log.d(TAG,"hideWindow");
            mWindowManager.removeView(mLeftmostWorkspace);
        }catch (Exception e){
            e.printStackTrace();
            Log.e(TAG,"hideWindow e = "+e);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mMessenger.getBinder();
    }

    private Messenger mMessenger = new Messenger(new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Log.d(TAG,"mMessenger handleMessage msg.what = "+msg.what);
            Message toClient = Message.obtain();
            super.handleMessage(msg);
            switch (msg.what) {
                case WHAT_ON_ATTACHED_TO_WINDOW:
                    WindowManager.LayoutParams layoutParams = (WindowManager.LayoutParams)msg.obj;
                    if(layoutParams != null){
                        mParams.token = layoutParams.token;
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                showWindow();
                            }
                        });
                    }
                    toClient.what = WHAT_SIDE_SCREEN_ATTACHED;
                    mRemoteMessenger = msg.replyTo;
                    setScreenState(msg.arg1);
                    mLeftmostWorkspace.setMessenger(mRemoteMessenger);
                    try {
                        //回复客户端窗口已经绑定
                        msg.replyTo.send(toClient);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    break;
                case WHAT_HIDE:
                    mLeftmostWorkspace.hide();
                    break;
                case WHAT_START_SCROLL:
                    mLeftmostWorkspace.onScrollInteractionBegin();
                    break;
                case WHAT_UPDATE_SCROLL:
                    mLeftmostWorkspace.onScrollChange(Float.intBitsToFloat(msg.arg1),mLeftmostWorkspace.getScreenState().isIsRtl());
                    break;
                case WHAT_END_SCROLL:
                    mLeftmostWorkspace.onScrollInteractionEnd(Float.intBitsToFloat(msg.arg1));
                    break;
                case WHAT_ON_PAUSE:
                    mLeftmostWorkspace.onPause();
                    break;
                case WHAT_ON_RESUME:
                    mLeftmostWorkspace.onResume();
                    break;
            }
        }
    });

    private void setScreenState(int arg){
        if(mLeftmostWorkspace != null) {
            mLeftmostWorkspace.getScreenState().setIsRtl((arg & FLAG_RTL) == 1);
            mLeftmostWorkspace.getScreenState().setIsLightNavigationBar((arg & FLAG_LIGHT_NAVIGATION_BAR) == 1);
            mLeftmostWorkspace.getScreenState().setIsLightStatusBar((arg & FLAG_LIGHT_STATUS_BAR) == 1);
            mLeftmostWorkspace.getScreenState().setIsTranslucentNavigationBar((arg & FLAG_TRANSLUCENT_NAVIGATION_BAR) == 1);
            mLeftmostWorkspace.getScreenState().setIsStatusBarHidden((arg & FLAG_HIDDEN_STATUS_BAR) == 1);
        }
    }
}
