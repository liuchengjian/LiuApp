package com.liucj.liu_library.picture;

import android.content.Context;

public class PictureHelper {
    private static Context mContext;

    public static void init(Context context) {
        mContext = context;
    }
    public static Context getContext() {
        return mContext;
    }
}
