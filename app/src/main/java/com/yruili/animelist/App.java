package com.yruili.animelist;

import android.app.Application;
import android.content.Context;

import com.bumptech.glide.Glide;
import com.bumptech.glide.MemoryCategory;
import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;
import com.yruili.animelist.Network.HttpClient;

/**
 * Created by rui on 18/07/17.
 */

public class App extends Application {
    private static HttpClient client;
    public static RefWatcher getRefWatcher(Context context) {
        App application = (App) context.getApplicationContext();
        return application.refWatcher;
    }

    private RefWatcher refWatcher;

    @Override
    public void onCreate() {
        super.onCreate();

        //LEAKCANARY
        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }
        refWatcher = LeakCanary.install(this);
        // Normal app init code...
        Glide.get(this).setMemoryCategory(MemoryCategory.LOW);
        client = new HttpClient(this);
    }

    public static HttpClient getClient() {
        return client;
    }
    public static void createClient(Context context){
        client = new HttpClient(context);
    }
}
