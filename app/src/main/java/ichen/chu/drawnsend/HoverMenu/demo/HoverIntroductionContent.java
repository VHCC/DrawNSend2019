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
package ichen.chu.drawnsend.HoverMenu.demo;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import de.greenrobot.event.EventBus;
import ichen.chu.drawnsend.App;
import ichen.chu.drawnsend.BusEvent;
import ichen.chu.drawnsend.HoverMenu.theme.HoverTheme;
import ichen.chu.drawnsend.R;
import io.mattcarroll.hover.Content;

/**
 * {@link Content} that displays an introduction to Hover.
 */
public class HoverIntroductionContent extends FrameLayout implements Content {

    private final EventBus mBus;
    private View mLogo;
    private HoverMotion mHoverMotion;
    private TextView mHoverTitleTextView;
    private TextView mGoalsTitleTextView;

    public HoverIntroductionContent(@NonNull Context context, @NonNull EventBus bus) {
        super(context);
        Log.d(App.TAG, " * HoverIntroductionContent");
        mBus = bus;
        init();
    }

    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.view_content_introduction, this, true);

        mLogo = findViewById(R.id.imageview_logo);
        mHoverMotion = new HoverMotion();
        mHoverTitleTextView = (TextView) findViewById(R.id.textview_hover_title);
        mGoalsTitleTextView = (TextView) findViewById(R.id.textview_goals_title);
    }

    @Override
    protected void onAttachedToWindow() {
        Log.d(App.TAG, " * onAttachedToWindow()");
        super.onAttachedToWindow();
        mBus.registerSticky(this);


    }

    @Override
    protected void onDetachedFromWindow() {
        Log.d(App.TAG, " * onDetachedFromWindow()");
        mBus.unregister(this);
        super.onDetachedFromWindow();
    }

    @NonNull
    @Override
    public View getView() {
        return this;
    }

    @Override
    public boolean isFullscreen() {
        Log.d(App.TAG, " * isFullscreen()");
        mBus.post(new BusEvent("test"));
        return true;
    }

    @Override
    public void onShown() {
        Log.d(App.TAG, " * onShown()");
        mHoverMotion.start(mLogo);
    }

    @Override
    public void onHidden() {
        Log.d(App.TAG, " * onHidden()");
        mHoverMotion.stop();
    }

    public void onEventMainThread(@NonNull HoverTheme newTheme) {
        mHoverTitleTextView.setTextColor(newTheme.getAccentColor());
        mGoalsTitleTextView.setTextColor(newTheme.getAccentColor());
    }
}
