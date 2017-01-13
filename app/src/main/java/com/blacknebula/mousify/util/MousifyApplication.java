package com.blacknebula.mousify.util;

import android.app.Application;
import android.content.Context;

public class MousifyApplication extends Application {

    private static Application context;

    public static Context getAppContext() {
        return MousifyApplication.context;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        this.context = this;
    }
}