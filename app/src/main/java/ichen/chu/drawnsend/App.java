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
import android.util.Log;

import androidx.core.content.ContextCompat;
import ichen.chu.drawnsend.HoverMenu.appstate.AppStateTracker;
import ichen.chu.drawnsend.HoverMenu.theme.HoverTheme;
import ichen.chu.drawnsend.HoverMenu.theme.HoverThemeManager;

/**
 * Application class.
 */
public class App extends Application {

    public static final String TAG = "dns-";

    @Override
    public void onCreate() {
        super.onCreate();

        setupTheme();
        setupAppStateTracking();
    }

    private void setupTheme() {
        Log.d(TAG, "setupTheme");
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
}
