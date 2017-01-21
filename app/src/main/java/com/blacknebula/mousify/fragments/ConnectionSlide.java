package com.blacknebula.mousify.fragments;

import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.blacknebula.mousify.R;
import com.blacknebula.mousify.services.RemoteMousifyIntentService;
import com.blacknebula.mousify.util.ViewUtils;
import com.blacknebula.mousify.view.MaskedEditText;
import com.blacknebula.mousify.view.SwitchIconView;
import com.github.paolorotolo.appintro.ISlideBackgroundColorHolder;
import com.github.paolorotolo.appintro.ISlidePolicy;
import com.google.common.base.Strings;
import com.inthecheesefactory.thecheeselibrary.fragment.support.v4.app.StatedFragment;
import com.wang.avi.AVLoadingIndicatorView;

import org.parceler.Parcels;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

import static com.blacknebula.mousify.services.RemoteMousifyIntentService.CONNECT_REQUEST_CODE;
import static com.blacknebula.mousify.services.RemoteMousifyIntentService.DISCOVER_REQUEST_CODE;

public class ConnectionSlide extends StatedFragment implements ISlideBackgroundColorHolder, ISlidePolicy {

    protected static final String ARG_BG_COLOR = "bgColor";
    private static final String ARG_LAYOUT_RES_ID = "layoutResId";

    @InjectView(R.id.configuration_layout)
    RelativeLayout mainLayout;
    @InjectView(R.id.targetIpAddressEditText)
    MaskedEditText targetIpAddressEditText;
    @InjectView(R.id.connectButton)
    SwitchIconView connectButton;
    @InjectView(R.id.discoverButton)
    SwitchIconView discoverButton;
    @InjectView(R.id.loading)
    AVLoadingIndicatorView loading;

    private int layoutResId;
    private int bgColor;
    private boolean isConnected;

    public static ConnectionSlide newInstance(int layoutResId, int bgColor) {
        ConnectionSlide connectionSlide = new ConnectionSlide();

        Bundle args = new Bundle();
        args.putInt(ARG_LAYOUT_RES_ID, layoutResId);
        args.putInt(ARG_BG_COLOR, bgColor);
        connectionSlide.setArguments(args);

        return connectionSlide;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            if (getArguments().containsKey(ARG_LAYOUT_RES_ID)) {
                layoutResId = getArguments().getInt(ARG_LAYOUT_RES_ID);
            }

            if (getArguments().containsKey(ARG_BG_COLOR)) {
                bgColor = getArguments().getInt(ARG_BG_COLOR);
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(layoutResId, container, false);
        ButterKnife.inject(this, view);
        setBackgroundColor(bgColor);
        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        stopLoading();

        if (requestCode == DISCOVER_REQUEST_CODE) {
            handleDiscoveryResponse(data);
        } else if (requestCode == CONNECT_REQUEST_CODE) {
            handleConnectResponse(data);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @OnClick(R.id.discoverButton)
    public void discover(View view) {
        discoverButton.setIconEnabled(true);
        startLoading();

        final PendingIntent pendingResult = getActivity().createPendingResult(DISCOVER_REQUEST_CODE, new Intent(), 0);

        final Intent intent = new Intent(getActivity(), RemoteMousifyIntentService.class);
        intent.setAction(RemoteMousifyIntentService.DISCOVER_ACTION);
        intent.putExtra(RemoteMousifyIntentService.PENDING_RESULT_EXTRA, pendingResult);
        getActivity().startService(intent);
    }

    @OnClick(R.id.connectButton)
    public void connect(View view) {
        connectButton.setIconEnabled(true);

        if (Strings.isNullOrEmpty(targetIpAddressEditText.getText().toString())) {
            ViewUtils.showToast(getActivity(), "Should set ip address");
            connectButton.setIconEnabled(false, true);
            return;
        }

        startLoading();

        final PendingIntent pendingResult = getActivity().createPendingResult(CONNECT_REQUEST_CODE, new Intent(), 0);

        final String ip = targetIpAddressEditText.getText().toString();

        final Intent intent = new Intent(getActivity(), RemoteMousifyIntentService.class);
        intent.setAction(RemoteMousifyIntentService.CONNECT_ACTION);
        intent.putExtra(RemoteMousifyIntentService.IP_EXTRA, ip);
        intent.putExtra(RemoteMousifyIntentService.PENDING_RESULT_EXTRA, pendingResult);
        getActivity().startService(intent);
    }

    private void handleDiscoveryResponse(Intent data) {
        final Parcelable result = data.getParcelableExtra(RemoteMousifyIntentService.REPLY_EXTRA);
        final String host = Parcels.unwrap(result);
        if (host.contains("not found")) {
            discoverButton.setIconEnabled(false);
            targetIpAddressEditText.setText("");
        }
        targetIpAddressEditText.setText(host);
    }

    private void handleConnectResponse(Intent data) {
        final Parcelable result = data.getParcelableExtra(RemoteMousifyIntentService.REPLY_EXTRA);
        final String host = Parcels.unwrap(result);
        if (host.contains("Connection failed")) {
            connectButton.setIconEnabled(false);
        }
        isConnected = true;
    }

    @Override
    public boolean isPolicyRespected() {
        return isConnected; // If user should be allowed to leave this slide
    }

    @Override
    public void onUserIllegallyRequestedNextPage() {
        ViewUtils.showToast(getActivity(), "Not connected to Mousify Server");
    }

    @Override
    public int getDefaultBackgroundColor() {
        // Return the default background color of the slide.
        return Color.parseColor("#000000");
    }

    @Override
    public void setBackgroundColor(@ColorInt int backgroundColor) {
        // Set the background color of the view within your slide to which the transition should be applied.
        if (mainLayout != null) {
            mainLayout.setBackgroundColor(backgroundColor);
        }
    }

    private void startLoading() {
        loading.setVisibility(View.VISIBLE);
        loading.show();
    }

    private void stopLoading() {
        loading.hide();
        loading.setVisibility(View.GONE);
    }
}