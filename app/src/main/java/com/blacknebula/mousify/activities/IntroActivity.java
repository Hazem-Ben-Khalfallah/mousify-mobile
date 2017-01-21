package com.blacknebula.mousify.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import com.blacknebula.mousify.R;
import com.blacknebula.mousify.fragments.ConnectionSlide;
import com.github.paolorotolo.appintro.AppIntro;
import com.github.paolorotolo.appintro.AppIntroFragment;
import com.inthecheesefactory.thecheeselibrary.fragment.support.v4.app.bus.ActivityResultBus;
import com.inthecheesefactory.thecheeselibrary.fragment.support.v4.app.bus.ActivityResultEvent;

public class IntroActivity extends AppIntro {
    private final String bgColor = "#66ce91";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Note here that we DO NOT use setContentView();
        addSlide(AppIntroFragment.newInstance(getString(R.string.step1_title), getString(R.string.step1_description), R.mipmap.startup, Color.parseColor(bgColor)));
        addSlide(ConnectionSlide.newInstance(R.layout.configuration, Color.parseColor(bgColor)));

        // OPTIONAL METHODS
        // Override bar/separator color.
        setBarColor(Color.parseColor(bgColor));
        setSeparatorColor(Color.parseColor("#FFFFFF"));

        // Hide Skip/Done button.
        showSkipButton(false);
        setProgressButtonEnabled(true);

        // Turn vibration on and set intensity.
        // NOTE: you will probably need to ask VIBRATE permission in Manifest.
        setVibrate(true);
        setVibrateIntensity(30);
        setFlowAnimation();

        setSwipeLock(false);
        setNextPageSwipeLock(false);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        ActivityResultBus.getInstance().postQueue(
                new ActivityResultEvent(requestCode, resultCode, data));
    }

    @Override
    public void onDonePressed(Fragment currentFragment) {
        super.onDonePressed(currentFragment);
        // Do something when users tap on Done button.
        final Intent intent = new Intent(this, MousePadActivity.class);
        startActivity(intent);
    }

}