package com.blacknebula.mousify.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.blacknebula.mousify.R;
import com.github.paolorotolo.appintro.ISlideBackgroundColorHolder;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class ServerInstallationInstructionSlide extends Fragment implements ISlideBackgroundColorHolder {

    protected static final String ARG_BG_COLOR = "bgColor";
    private static final String ARG_LAYOUT_RES_ID = "layoutResId";

    @InjectView(R.id.configuration_layout)
    RelativeLayout mainLayout;

    @InjectView(R.id.step2Description)
    TextView description;

    private int layoutResId;
    private int bgColor;

    public static ServerInstallationInstructionSlide newInstance(int layoutResId, int bgColor) {
        ServerInstallationInstructionSlide installationInstructionSlide = new ServerInstallationInstructionSlide();

        Bundle args = new Bundle();
        args.putInt(ARG_LAYOUT_RES_ID, layoutResId);
        args.putInt(ARG_BG_COLOR, bgColor);
        installationInstructionSlide.setArguments(args);

        return installationInstructionSlide;
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
        // enable link in text
        description.setMovementMethod(LinkMovementMethod.getInstance());
        return view;
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

}