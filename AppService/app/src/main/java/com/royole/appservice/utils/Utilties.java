package com.royole.appservice.utils;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Point;
import android.text.TextUtils;
import android.view.WindowManager;


public class Utilties {
    public static int getScreenMaxWidth(Context context) {
        boolean isPortrait = context.getApplicationContext().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;

        Point minSize = new Point();
        Point maxSize = new Point();
        ((WindowManager) context.getApplicationContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getCurrentSizeRange(minSize, maxSize);
        return isPortrait ? minSize.x : maxSize.x;
    }

    public static float mapRange(float value, float min, float max) {
        return min + (value * (max - min));
    }

}
