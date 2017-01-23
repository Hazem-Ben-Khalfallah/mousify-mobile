package com.blacknebula.mousify.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import com.blacknebula.mousify.R;
import com.blacknebula.mousify.fragments.ConnexionSlide;
import com.blacknebula.mousify.fragments.InstallationSlide;
import com.blacknebula.mousify.services.RemoteMousifyIntentService;
import com.blacknebula.mousify.util.ViewUtils;
import com.github.paolorotolo.appintro.AppIntro;
import com.inthecheesefactory.thecheeselibrary.fragment.support.v4.app.bus.ActivityResultBus;
import com.inthecheesefactory.thecheeselibrary.fragment.support.v4.app.bus.ActivityResultEvent;

public class IntroActivity extends AppIntro {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Note here that we DO NOT use setContentView();
        addSlide(InstallationSlide.newInstance(R.layout.installation_step, R.color.colorPrimary));
        addSlide(ConnexionSlide.newInstance(R.layout.connexion, R.color.colorPrimary));

        // OPTIONAL METHODS
        // Override bar/separator color.
        setBarColor(getResources().getColor(R.color.colorPrimary));
        setSeparatorColor(Color.WHITE);

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
        if (RemoteMousifyIntentService.isCoonected()) {
            final Intent intent = new Intent(this, MousePadActivity.class);
            startActivity(intent);
        } else {
            ViewUtils.showToast(this, "Client not connected");
        }
    }

}