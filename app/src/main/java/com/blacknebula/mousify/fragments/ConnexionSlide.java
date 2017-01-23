package com.blacknebula.mousify.fragments;

import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.ColorRes;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.blacknebula.mousify.R;
import com.blacknebula.mousify.activities.MousePadActivity;
import com.blacknebula.mousify.services.RemoteMousifyIntentService;
import com.blacknebula.mousify.util.ViewUtils;
import com.blacknebula.mousify.view.MaskedEditText;
import com.github.paolorotolo.appintro.ISlideBackgroundColorHolder;
import com.google.common.base.Strings;
import com.inthecheesefactory.thecheeselibrary.fragment.support.v4.app.StatedFragment;
import com.wang.avi.AVLoadingIndicatorView;

import org.parceler.Parcels;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import mehdi.sakout.fancybuttons.FancyButton;

import static com.blacknebula.mousify.services.RemoteMousifyIntentService.CONNECT_REQUEST_CODE;
import static com.blacknebula.mousify.services.RemoteMousifyIntentService.DISCOVER_REQUEST_CODE;

public class ConnexionSlide extends StatedFragment implements ISlideBackgroundColorHolder {

    protected static final String ARG_BG_COLOR = "bgColor";
    private static final String ARG_LAYOUT_RES_ID = "layoutResId";
    private static final String ICON_ENABLED_COLOR = "#7ab800";
    private static final String ICON_DISABLED_COLOR = "#000000";

    @InjectView(R.id.connexion_layout)
    RelativeLayout mainLayout;
    @InjectView(R.id.host_ip)
    MaskedEditText targetIpAddressEditText;
    @InjectView(R.id.connectButton)
    FancyButton connectButton;
    @InjectView(R.id.discoverButton)
    FancyButton discoverButton;
    @InjectView(R.id.loading)
    AVLoadingIndicatorView loading;

    private int layoutResId;
    private int bgColor;

    public static ConnexionSlide newInstance(int layoutResId, int bgColor) {
        ConnexionSlide connexionSlide = new ConnexionSlide();

        Bundle args = new Bundle();
        args.putInt(ARG_LAYOUT_RES_ID, layoutResId);
        args.putInt(ARG_BG_COLOR, bgColor);
        connexionSlide.setArguments(args);

        return connexionSlide;
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
        discoverButton.setIconColor(Color.parseColor(ICON_DISABLED_COLOR));
        connectButton.setIconColor(Color.parseColor(ICON_DISABLED_COLOR));
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
        discoverButton.setIconColor(Color.parseColor(ICON_ENABLED_COLOR));
        startLoading();

        final PendingIntent pendingResult = getActivity().createPendingResult(DISCOVER_REQUEST_CODE, new Intent(), 0);

        final Intent intent = new Intent(getActivity(), RemoteMousifyIntentService.class);
        intent.setAction(RemoteMousifyIntentService.DISCOVER_ACTION);
        intent.putExtra(RemoteMousifyIntentService.PENDING_RESULT_EXTRA, pendingResult);
        getActivity().startService(intent);
    }

    @OnClick(R.id.connectButton)
    public void connect(View view) {
        connectToHost();
    }

    private void connectToHost() {
        connectButton.setIconColor(Color.parseColor(ICON_ENABLED_COLOR));

        if (Strings.isNullOrEmpty(targetIpAddressEditText.getUnmaskedText())) {
            ViewUtils.showToast(getActivity(), "Should set ip address");
            connectButton.setIconColor(Color.parseColor(ICON_DISABLED_COLOR));
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
        if (Strings.isNullOrEmpty(host)) {
            discoverButton.setIconColor(Color.parseColor(ICON_ENABLED_COLOR));
        } else {
            targetIpAddressEditText.setText(host);
            // launch connection automatically
            connectToHost();
        }
    }

    private void handleConnectResponse(Intent data) {
        final Parcelable result = data.getParcelableExtra(RemoteMousifyIntentService.REPLY_EXTRA);
        final String host = Parcels.unwrap(result);
        if (host.contains("Connection failed")) {
            connectButton.setIconColor(Color.parseColor(ICON_DISABLED_COLOR));
        } else {
            openMousePad();
        }
    }

    @Override
    public int getDefaultBackgroundColor() {
        // Return the default background color of the slide.
        return Color.parseColor("#000000");
    }

    @Override
    public void setBackgroundColor(@ColorRes int backgroundColor) {
        // Set the background color of the view within your slide to which the transition should be applied.
        if (mainLayout != null) {
            mainLayout.setBackgroundResource(backgroundColor);
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

    private void openMousePad() {
        final Intent intent = new Intent(getActivity(), MousePadActivity.class);
        startActivity(intent);
    }
}