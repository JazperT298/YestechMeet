package com.theyestech.yestechmeet.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.theyestech.yestechmeet.R;
import com.theyestech.yestechmeet.models.Users;
import com.theyestech.yestechmeet.services.ApiClient;
import com.theyestech.yestechmeet.services.ApiService;
import com.theyestech.yestechmeet.services.ApiServices;
import com.theyestech.yestechmeet.utils.Constants;
import com.theyestech.yestechmeet.utils.GlideOptions;

import org.jitsi.meet.sdk.JitsiMeetActivity;
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions;
import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.net.URL;
import java.util.ArrayList;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OutgoingInvitationActivity extends AppCompatActivity {
    private View view;
    private Context context;

    private ImageView iV_MeetingType, iv_StopInvitation, iv_ProfileImage;
    private TextView tv_Username, tv_Email;
    private String meetingType = null;
    private Users users;
    private String userid, token;

    private String inviterToken = null, meetingRoom = null;
    private int rejectionCount = 0;
    private int totalReceivers = 0;
    private DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_outgoing_invitation);
        context = this;
        initializeUI();
    }

    private void initializeUI() {
        iV_MeetingType = findViewById(R.id.iV_MeetingType);
        iv_ProfileImage = findViewById(R.id.iv_ProfileImage);
        tv_Username = findViewById(R.id.tv_Username);
        tv_Email = findViewById(R.id.tv_Email);
        iv_StopInvitation = findViewById(R.id.iv_StopInvitation);

        //userid = getIntent().getStringExtra("userid");
        users = (Users) getIntent().getParcelableExtra("users");
        meetingType = getIntent().getStringExtra("type");

        if (meetingType != null) {
            if (meetingType.equals("video")) {
                iV_MeetingType.setImageResource(R.drawable.ic_video);
            } else {
                iV_MeetingType.setImageResource(R.drawable.ic_audio);
            }
        }
        if (users != null) {
            tv_Username.setText( users.getName());
            tv_Email.setText(users.getEmail());
            Glide.with(getApplicationContext())
                    .load(users.getProfilePhoto())
                    .apply(GlideOptions.getOptions())
                    .into(iv_ProfileImage);
//            reference = FirebaseDatabase.getInstance().getReference("Users").child(userid);
//            reference.addValueEventListener(new ValueEventListener() {
//                @Override
//                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                    users = dataSnapshot.getValue(Users.class);
//                    tv_Username.setText(users.getName());
//                    tv_Email.setText(users.getEmail());
//                    Glide.with(getApplicationContext())
//                            .load(users.getProfilePhoto())
//                            .apply(GlideOptions.getOptions())
//                            .into(iv_ProfileImage);
//                    token = users.getToken();
//                }
//
//                @Override
//                public void onCancelled(@NonNull DatabaseError databaseError) {
//
//                }
//            });
        }
        iv_StopInvitation.setOnClickListener(v -> {
            if (getIntent().getBooleanExtra("isMultiple", false)) {
                Type type = new TypeToken<ArrayList<Users>>() {
                }.getType();
                ArrayList<Users> receivers = new Gson().fromJson(getIntent().getStringExtra("selectedUsers"), type);
                cancelInvitation(null, receivers);
            } else {
                if (users != null) {
                    cancelInvitation(users.getToken(), null);
                }
            }
        });
        FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                inviterToken = task.getResult().getToken();

                if (meetingType != null) {
                    if (getIntent().getBooleanExtra("isMultiple", false)) {
                        Type type = new TypeToken<ArrayList<Users>>() {
                        }.getType();
                        ArrayList<Users> receivers = new Gson().fromJson(getIntent().getStringExtra("selectedUsers"), type);
                        if (receivers != null) {
                            totalReceivers = receivers.size();
                        }
                        initiateMeeting(meetingType, null, receivers);
                    } else {
                        if (users != null) {
                            totalReceivers = 1;
                            initiateMeeting(meetingType,users.getToken(), null);
                        }
                    }
                }

            }
        });

    }

    private void initiateMeeting(String meetingType, String receiverToken, ArrayList<Users> receivers) {
        try {
            JSONArray tokens = new JSONArray();
            if (receiverToken != null) {
                tokens.put(receiverToken);
            }

            if (receivers != null && receivers.size() > 0) {
                StringBuilder userNames = new StringBuilder();
                for (int i = 0; i < receivers.size(); i++) {
                    tokens.put(receivers.get(i).getToken());
                    userNames.append(receivers.get(i).getName()).append(" ").append("\n");
                }
                iv_ProfileImage.setVisibility(View.GONE);
                tv_Email.setVisibility(View.GONE);
                tv_Username.setText(userNames.toString());
            }


            JSONObject body = new JSONObject();
            JSONObject data = new JSONObject();

            data.put(Constants.REMOTE_MSG_TYPE, Constants.REMOTE_MSG_INVITATION);
            data.put(Constants.REMOTE_MSG_MEETING_TYPE, meetingType);
            data.put(Constants.KEY_FIRST_NAME, users.getName());
            data.put(Constants.KEY_PROFILE_IMAGE, users.getProfilePhoto());
            data.put(Constants.KEY_EMAIL, users.getEmail());
            data.put(Constants.REMOTE_MSG_INVITER_TOKEN, inviterToken);

            meetingRoom = users.getId() + "_" + UUID.randomUUID().toString().substring(0, 5);
            data.put(Constants.REMOTE_MSG_MEETING_ROOM, meetingRoom);

            body.put(Constants.REMOTE_MSG_DATA, data);
            body.put(Constants.REMOTE_MSG_REGISTRATION_IDS, tokens);

            sendRemoteMessage(body.toString(), Constants.REMOTE_MSG_INVITATION);
        } catch (Exception e) {
            Toast.makeText(context, "yawa " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void sendRemoteMessage(String remoteMessageBody, String type) {
        ApiClient.getClient().create(ApiServices.class).sendRemoteMessage(Constants.getRemoteMessageHeader(), remoteMessageBody)
                .enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                        if (response.isSuccessful()) {
                            if (type.equals(Constants.REMOTE_MSG_INVITATION)) {
                                Toast.makeText(context, "Invitation sent successfully", Toast.LENGTH_LONG).show();
                            } else if (type.equals(Constants.REMOTE_MSG_INVITATION_RESPONSE)) {
                                Toast.makeText(context, "Invitation cancelled", Toast.LENGTH_LONG).show();
                                finish();
                            }
                        } else {
                            Toast.makeText(context, "burikat " + response.message(), Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                        Toast.makeText(context, "piste " + t.getMessage(), Toast.LENGTH_LONG).show();
                        finish();
                    }
                });
    }

    private void cancelInvitation(String receiverToken, ArrayList<Users> receivers) {

        try {
            JSONArray tokens = new JSONArray();

            if (receiverToken != null) {
                tokens.put(receiverToken);
            }

            if (receivers != null && receivers.size() > 0) {
                for (Users users : receivers) {
                    tokens.put(users.getToken());
                }
            }


            JSONObject body = new JSONObject();
            JSONObject data = new JSONObject();

            data.put(Constants.REMOTE_MSG_TYPE, Constants.REMOTE_MSG_INVITATION_RESPONSE);
            data.put(Constants.REMOTE_MSG_INVITATION_RESPONSE, Constants.REMOTE_MSG_INVITATION_CANCELLED);

            body.put(Constants.REMOTE_MSG_DATA, data);
            body.put(Constants.REMOTE_MSG_REGISTRATION_IDS, tokens);

            sendRemoteMessage(body.toString(), Constants.REMOTE_MSG_INVITATION_RESPONSE);

        } catch (Exception e) {
            Toast.makeText(context, "animal" + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }

    }

    private BroadcastReceiver invitationResponseReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String type = intent.getStringExtra(Constants.REMOTE_MSG_INVITATION_RESPONSE);
            if (type != null) {
                if (type.equals(Constants.REMOTE_MSG_INVITATION_ACCEPTED)) {
                    try {
                        URL serverURL = new URL("https://meet.jit.si");
                        JitsiMeetConferenceOptions.Builder builder = new JitsiMeetConferenceOptions.Builder();
                        builder.setServerURL(serverURL);
                        builder.setWelcomePageEnabled(false);
                        builder.setRoom(meetingRoom);
                        if (meetingType.equals("audio")) {
                            builder.setVideoMuted(true);
                        }
                        JitsiMeetActivity.launch(OutgoingInvitationActivity.this, builder.build());
                        finish();
                    } catch (Exception e) {
                        Toast.makeText(context, "bwesit " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                } else if (type.equals(Constants.REMOTE_MSG_INVITATION_REJECTED)) {
                    rejectionCount += 1;
                    if (rejectionCount == totalReceivers) {
                        Toast.makeText(context, "Invitation Rejected", Toast.LENGTH_LONG).show();
                        finish();
                    }

                }
            }
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(context).registerReceiver(invitationResponseReceiver, new IntentFilter(Constants.REMOTE_MSG_INVITATION_RESPONSE));
    }

    @Override
    protected void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(context).unregisterReceiver(invitationResponseReceiver);
    }
}