package com.blacknebula.mousify.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

import com.blacknebula.mousify.R;
import com.blacknebula.mousify.dto.MotionHistory;
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
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            float x = event.getRawX();
            float y = event.getRawY();
            MotionHistory.getInstance().updateStartCoordinates(x, y);
        } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
            final MotionRequest motionRequest = getDistance(MotionHistory.getInstance().getStartX(), MotionHistory.getInstance().getStartY(), event);
            MotionHistory.getInstance().updateCurrentCoordinates(event.getRawX(), event.getRawY());
            send(motionRequest);
        }
        return true;
    }

    MotionRequest getDistance(float startX, float startY, MotionEvent ev) {
        // add distance from last historical point to event's point
        float dx = ev.getRawX() - startX;
        float dy = ev.getRawY() - startY;
        return MotionRequest.builder()
                .withDx(dx)
                .withDy(dy);
    }

    private void send(MotionRequest motionRequest) {
        final Intent intent = new Intent(this, RemoteMousifyIntentService.class);
        intent.setAction(RemoteMousifyIntentService.SEND_ACTION);
        Parcelable parcelable = Parcels.wrap(motionRequest);
        intent.putExtra(RemoteMousifyIntentService.COORDINATES_EXTRA, parcelable);
        startService(intent);
    }
}
