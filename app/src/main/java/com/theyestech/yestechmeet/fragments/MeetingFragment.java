package com.theyestech.yestechmeet.fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.android.material.button.MaterialButton;
import com.theyestech.yestechmeet.R;

import org.jitsi.meet.sdk.JitsiMeetActivity;
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions;

import java.net.URL;

public class MeetingFragment extends Fragment {

    private View view;
    private Context context;

    private Button btn_NewMeeting, btn_JoinMeeting;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_meeting, container, false);
        context = getContext();
        initializeUI();
        return view;
    }

    private void initializeUI(){
        btn_NewMeeting = view.findViewById(R.id.btn_NewMeeting);
        btn_JoinMeeting = view.findViewById(R.id.btn_JoinMeeting);
        btn_NewMeeting.setOnClickListener(v -> {
            try {
                URL serverURL = new URL("https://meet.jit.si");
                JitsiMeetConferenceOptions.Builder builder = new JitsiMeetConferenceOptions.Builder();
                builder.setServerURL(serverURL);
                builder.setWelcomePageEnabled(true);
                JitsiMeetActivity.launch(getActivity(), builder.build());
            }catch (Exception e){

            }
        });
        btn_JoinMeeting.setOnClickListener(v -> {
            try {
                URL serverURL = new URL("https://meet.jit.si");
                JitsiMeetConferenceOptions.Builder builder = new JitsiMeetConferenceOptions.Builder();
                builder.setServerURL(serverURL);
                builder.setWelcomePageEnabled(true);
                JitsiMeetActivity.launch(getActivity(), builder.build());
            }catch (Exception e){

            }
        });
    }
}