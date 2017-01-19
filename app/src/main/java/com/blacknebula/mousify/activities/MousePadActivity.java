package com.blacknebula.mousify.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

import com.blacknebula.mousify.R;
import com.blacknebula.mousify.dto.MotionRequest;
import com.blacknebula.mousify.services.RemoteMousifyIntentService;

import org.parceler.Parcels;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class MousePadActivity extends AppCompatActivity implements View.OnTouchListener {

    @InjectView(R.id.mousePad)
    RelativeLayout mousePadLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mouse_pad);
        ButterKnife.inject(this);
        mousePadLayout.setOnTouchListener(this);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_MOVE) {
            float x = event.getRawX();
            float y = event.getRawY();
            //  Code to display x and y go here
            send(x, y);
        }
        return true;
    }

    private void send(float x, float y) {
        final Intent intent = new Intent(this, RemoteMousifyIntentService.class);
        intent.setAction(RemoteMousifyIntentService.SEND_ACTION);
        Parcelable parcelable = Parcels.wrap(new MotionRequest(Math.round(x), Math.round(y)));
        intent.putExtra(RemoteMousifyIntentService.COORDINATES_EXTRA, parcelable);
        startService(intent);
    }
}
