/*
 * Copyright 2016 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ichen.chu.drawnsend;

import android.app.ActivityManager;
import android.app.Application;
import android.os.Build;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.X509TrustManager;

import androidx.core.content.ContextCompat;
import ichen.chu.drawnsend.HoverMenu.appstate.AppStateTracker;
import ichen.chu.drawnsend.HoverMenu.theme.HoverTheme;
import ichen.chu.drawnsend.HoverMenu.theme.HoverThemeManager;
import ichen.chu.drawnsend.api.DnsServerAgent;
import ichen.chu.drawnsend.util.MLog;
import ichen.chu.drawnsend.util.NullHostNameVerifier;
import ichen.chu.drawnsend.util.NullX509TrustManager;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Application class.
 */
public class App extends Application {

    private static final MLog mLog = new MLog(false);
    private final String TAG = getClass().getSimpleName() + "@" + Integer.toHexString(hashCode());

//    public static final String SERVER_SITE = "http://172.22.212.150:4009";
//    public static final String SERVER_SITE = "http://192.168.1.105:4009";
    public static final String SERVER_SITE = "https://dns.ichenprocin.dsmynas.com";

    @Override
    public void onCreate() {
        super.onCreate();
        mLog.i(TAG, "========== app start ==========");

        trustHost();

        // Task Schedule handle thread
        appThread = new Thread(appRunnable);
        appThread.start();

        setupTheme();
        setupAppStateTracking();
    }

    private void setupTheme() {
        mLog.d(TAG, "setupTheme");
        HoverTheme defaultTheme = new HoverTheme(
                ContextCompat.getColor(this, R.color.hover_accent),
                ContextCompat.getColor(this, R.color.hover_base));
        HoverThemeManager.init(Bus.getInstance(), defaultTheme);
    }

    private void setupAppStateTracking() {
        ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);

        AppStateTracker.init(this, Bus.getInstance());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (activityManager.getAppTasks().size() > 0) {
                AppStateTracker.getInstance().trackTask(activityManager.getAppTasks().get(0).getTaskInfo());
            }
        }
    }

    @Override
    public void onTerminate() {
        mLog.i(TAG, "========== app ShutDown ==========");

        if (appThread.isAlive()) {
            appThread.interrupt();
        }

        super.onTerminate();
    }

    /**
     * Trust all the https host.
     * //TODO it might be dangerous.
     */
    private void trustHost() {
        mLog.i(TAG, "trustHost()");
        HttpsURLConnection.setDefaultHostnameVerifier(new NullHostNameVerifier());
        SSLContext context = null;
        try {
            context = SSLContext.getInstance("TLS");
            context.init(null, new X509TrustManager[]{new NullX509TrustManager()}, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(context.getSocketFactory());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }
    }

    // Thread
    private Thread appThread;
    private Runnable appRunnable = new Runnable() {

        private static final long task_minimum_tick_time_msec = 1000; // 1 second

        @Override
        public void run() {
            long tick_count = 0;
            mLog.d(TAG, "task_minimum_tick_time_msec= " + (task_minimum_tick_time_msec));

            while (true) {
                try {
                    long start_time_tick = System.currentTimeMillis();
                    // real-time task

                    if (tick_count % 60 == 5) {
                        mLog.d(TAG, "heartBeat");
                    }

                    if (tick_count % 60 == 6) {
                        DnsServerAgent.getInstance(getApplicationContext()).getDNSServerStatus();
                    }

                    long end_time_tick = System.currentTimeMillis();

                    if (end_time_tick - start_time_tick > task_minimum_tick_time_msec) {
                        mLog.w(TAG, "Over time process " + (end_time_tick - start_time_tick));
                    } else {
                        Thread.sleep(task_minimum_tick_time_msec);
                    }
                    tick_count++;
                } catch (InterruptedException e) {
                    mLog.d(TAG, "appRunnable interrupted");
                }
            }
        }
    };



}
