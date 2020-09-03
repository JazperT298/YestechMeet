package com.theyestech.yestechmeet.fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.google.android.material.button.MaterialButton;
import com.theyestech.yestechmeet.R;
import com.theyestech.yestechmeet.adapters.CustomPagerAdapter;

import org.jitsi.meet.sdk.JitsiMeetActivity;
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions;

import java.net.URL;

public class MeetingFragment extends Fragment implements ViewPager.OnPageChangeListener, View.OnClickListener {

    private View view;
    private Context context;

    private Button btn_NewMeeting, btn_JoinMeeting;


    int[] mResources = {R.drawable.yestechsessions1, R.drawable.yestechsessions3
    };
    String[] mTitle = {"Create a link", "Secure Sessions"};
    String[] mDescription = {"Tap New session to create a link that you can share to friends or people you want to join the session", "Your session is safe and is restricted form others unless you have given access to the person entering the session"};

    ViewPager mViewPager;
    private CustomPagerAdapter mAdapter;
    private LinearLayout pager_indicator;
    private int dotsCount;
    private ImageView[] dots;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_meeting, container, false);
        context = getContext();
        initializeUI();
        return view;
    }

    private void initializeUI() {
        btn_NewMeeting = view.findViewById(R.id.btn_NewMeeting);
        btn_JoinMeeting = view.findViewById(R.id.btn_JoinMeeting);
        btn_NewMeeting.setOnClickListener(v -> {
            try {
                URL serverURL = new URL("https://meet.jit.si");
                JitsiMeetConferenceOptions.Builder builder = new JitsiMeetConferenceOptions.Builder();
                builder.setServerURL(serverURL);
                builder.setWelcomePageEnabled(true);
                JitsiMeetActivity.launch(getActivity(), builder.build());
            } catch (Exception e) {

            }
        });
        btn_JoinMeeting.setOnClickListener(v -> {
            try {
                URL serverURL = new URL("https://meet.jit.si");
                JitsiMeetConferenceOptions.Builder builder = new JitsiMeetConferenceOptions.Builder();
                builder.setServerURL(serverURL);
                builder.setWelcomePageEnabled(true);
                JitsiMeetActivity.launch(getActivity(), builder.build());
            } catch (Exception e) {

            }
        });

        mViewPager = (ViewPager) view.findViewById(R.id.viewpager);
        pager_indicator = (LinearLayout) view.findViewById(R.id.viewPagerCountDots);
        mAdapter = new CustomPagerAdapter(getContext(), mResources, mTitle, mDescription);
        mViewPager.setAdapter(mAdapter);
        mViewPager.setCurrentItem(0);
        mViewPager.setOnPageChangeListener(this);

        setPageViewIndicator();
    }

    private void setPageViewIndicator() {

        Log.d("###setPageViewIndicator", " : called");
        dotsCount = mAdapter.getCount();
        dots = new ImageView[dotsCount];

        for (int i = 0; i < dotsCount; i++) {
            dots[i] = new ImageView(getContext());
            dots[i].setImageDrawable(getResources().getDrawable(R.drawable.nonselecteditem_dot));

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );

            params.setMargins(4, 0, 4, 0);

            final int presentPosition = i;
            dots[presentPosition].setOnTouchListener(new View.OnTouchListener() {

                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    mViewPager.setCurrentItem(presentPosition);
                    return true;
                }

            });


            pager_indicator.addView(dots[i], params);
        }

        dots[0].setImageDrawable(getResources().getDrawable(R.drawable.selecteditem_dot));
    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        Log.d("###onPageSelected, pos ", String.valueOf(position));
        for (int i = 0; i < dotsCount; i++) {
            dots[i].setImageDrawable(getResources().getDrawable(R.drawable.nonselecteditem_dot));
        }

        dots[position].setImageDrawable(getResources().getDrawable(R.drawable.selecteditem_dot));

        if (position + 1 == dotsCount) {

        } else {

        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }
}