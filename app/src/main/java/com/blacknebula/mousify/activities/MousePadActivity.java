package com.blacknebula.mousify.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.blacknebula.mousify.R;
import com.blacknebula.mousify.dto.MotionHistory;
import com.blacknebula.mousify.event.ClickEvent;
import com.blacknebula.mousify.event.MotionEvent;
import com.blacknebula.mousify.services.RemoteMousifyIntentService;

import org.parceler.Parcels;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class MousePadActivity extends AppCompatActivity implements View.OnTouchListener {

    @InjectView(R.id.mousePad)
    LinearLayout mousePadLayout;

    @InjectView(R.id.left_button)
    Button leftButton;

    @InjectView(R.id.right_button)
    Button rightButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mouse_pad);
        ButterKnife.inject(this);
        mousePadLayout.setOnTouchListener(this);
    }

    @Override
    public boolean onTouch(View v, android.view.MotionEvent event) {
        if (event.getAction() == android.view.MotionEvent.ACTION_DOWN) {
            float x = event.getRawX();
            float y = event.getRawY();
            MotionHistory.getInstance().updateDownCoordinates(x, y);
        } else if (event.getAction() == android.view.MotionEvent.ACTION_MOVE) {
            final MotionEvent motionEvent = getDistance(MotionHistory.getInstance().getStartX(), MotionHistory.getInstance().getStartY(), event);
            MotionHistory.getInstance().updateCurrentCoordinates(event.getRawX(), event.getRawY());
            sendMotion(motionEvent);
        } else if (event.getAction() == android.view.MotionEvent.ACTION_UP) {
            float x = event.getRawX();
            float y = event.getRawY();
            MotionHistory.getInstance().updateUpCoordinates(x, y);
            if (MotionHistory.getInstance().isClickEvent()) {
                final ClickEvent clickEvent = new ClickEvent(true);
                sendClick(clickEvent);
            }

        }
        return true;
    }

    @OnClick(R.id.left_button)
    public void leftClick(View view) {
        final ClickEvent clickEvent = new ClickEvent(true);
        sendClick(clickEvent);
    }

    @OnClick(R.id.right_button)
    public void rightClick(View view) {
        final ClickEvent clickEvent = new ClickEvent(false);
        sendClick(clickEvent);
    }

    private MotionEvent getDistance(float startX, float startY, android.view.MotionEvent ev) {
        // add distance from last historical point to event's point
        float dx = ev.getRawX() - startX;
        float dy = ev.getRawY() - startY;
        return MotionEvent.builder()
                .withDx(dx)
                .withDy(dy);
    }

    private void sendMotion(MotionEvent motionEvent) {
        final Intent intent = new Intent(this, RemoteMousifyIntentService.class);
        intent.setAction(RemoteMousifyIntentService.SEND_MOTION_ACTION);
        Parcelable parcelable = Parcels.wrap(motionEvent);
        intent.putExtra(RemoteMousifyIntentService.MOTION_EXTRA, parcelable);
        startService(intent);
    }

    private void sendClick(ClickEvent clickEvent) {
        final Intent intent = new Intent(this, RemoteMousifyIntentService.class);
        intent.setAction(RemoteMousifyIntentService.SEND_CLICK_ACTION);
        Parcelable parcelable = Parcels.wrap(clickEvent);
        intent.putExtra(RemoteMousifyIntentService.CLICK_EXTRA, parcelable);
        startService(intent);
    }
}
