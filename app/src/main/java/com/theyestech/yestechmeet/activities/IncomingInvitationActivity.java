package com.theyestech.yestechmeet.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
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

import java.net.URL;
import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class IncomingInvitationActivity extends AppCompatActivity {
    private View view;
    private Context context;

    private ImageView iV_MeetingType, iv_AcceptInvitation, iv_RejectInvitation,iv_ProfileImage;
    private TextView  tv_Username, tv_Email;
    private String meetingType = null;
    private Users users;

    private String inviterToken = null;
    private DatabaseReference reference,usersRef;
    private FirebaseUser firebaseUser;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incoming_invitation);
        context = this;
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        initializeUI();
    }
    private void initializeUI() {
        iV_MeetingType = findViewById(R.id.iV_MeetingType);
        iv_AcceptInvitation = findViewById(R.id.iv_AcceptInvitation);
        iv_RejectInvitation = findViewById(R.id.iv_RejectInvitation);
        iv_ProfileImage = findViewById(R.id.iv_ProfileImage);
        tv_Username = findViewById(R.id.tv_Username);
        tv_Email = findViewById(R.id.tv_Email);

        meetingType = getIntent().getStringExtra(Constants.REMOTE_MSG_MEETING_TYPE);
        if (meetingType != null) {
            if (meetingType.equals("video")) {
                iV_MeetingType.setImageResource(R.drawable.ic_video);
            }else{
                iV_MeetingType.setImageResource(R.drawable.ic_audio);
            }
        }

        FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                inviterToken = task.getResult().getToken();
            }
        });
        Glide.with(getApplicationContext())
                .load(getIntent().getStringExtra(Constants.KEY_PROFILE_IMAGE))
                .apply(GlideOptions.getOptions())
                .into(iv_ProfileImage);
        tv_Username.setText(getIntent().getStringExtra(Constants.KEY_FIRST_NAME));
        tv_Email.setText(getIntent().getStringExtra(Constants.KEY_EMAIL));

        iv_AcceptInvitation.setOnClickListener(v -> sendInvitationResponse(Constants.REMOTE_MSG_INVITATION_ACCEPTED, getIntent().getStringExtra(Constants.REMOTE_MSG_INVITER_TOKEN)));
        iv_RejectInvitation.setOnClickListener(v -> sendInvitationResponse(Constants.REMOTE_MSG_INVITATION_REJECTED, getIntent().getStringExtra(Constants.REMOTE_MSG_INVITER_TOKEN)));
    }

    private void sendInvitationResponse(String type, String receiverToken) {

        try {
            JSONArray tokens = new JSONArray();
            tokens.put(receiverToken);

            JSONObject body = new JSONObject();
            JSONObject data = new JSONObject();

            data.put(Constants.REMOTE_MSG_TYPE, Constants.REMOTE_MSG_INVITATION_RESPONSE);
            data.put(Constants.REMOTE_MSG_INVITATION_RESPONSE, type);

            body.put(Constants.REMOTE_MSG_DATA, data);
            body.put(Constants.REMOTE_MSG_REGISTRATION_IDS, tokens);

            sendRemoteMessage(body.toString(), type);

        } catch (Exception e) {
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }

    }

    private void sendRemoteMessage(String remoteMessageBody, String type) {
        ApiClient.getClient().create(ApiServices.class).sendRemoteMessage(Constants.getRemoteMessageHeader(), remoteMessageBody)
                .enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                        if (response.isSuccessful()) {
                            if (type.equals(Constants.REMOTE_MSG_INVITATION_ACCEPTED)) {
                                try {
                                    URL serverURL = new URL("https://meet.jit.si");
                                    JitsiMeetConferenceOptions.Builder builder = new JitsiMeetConferenceOptions.Builder();
                                    builder.setServerURL(serverURL);
                                    builder.setWelcomePageEnabled(false);
                                    builder.setRoom(getIntent().getStringExtra(Constants.REMOTE_MSG_MEETING_ROOM));
                                    if(meetingType.equals("audio")){
                                        builder.setVideoMuted(true);
                                    }
                                    JitsiMeetActivity.launch(context, builder.build());
                                    finish();
                                }catch (Exception e){
                                    Toast.makeText(context,"FUCK " + e.getMessage(), Toast.LENGTH_LONG).show();
                                }
                            } else {
                                Toast.makeText(context, "Invitation rejected", Toast.LENGTH_LONG).show();
                                finish();
                            }
                        } else {
                            Toast.makeText(context, response.message(), Toast.LENGTH_LONG).show();
                            finish();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                        Toast.makeText(context, t.getMessage(), Toast.LENGTH_LONG).show();
                        finish();
                    }
                });
    }


    private BroadcastReceiver invitationResponseReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String type = intent.getStringExtra(Constants.REMOTE_MSG_INVITATION_RESPONSE);
            if(type != null){
                if(type.equals(Constants.REMOTE_MSG_INVITATION_CANCELLED)){
                    Toast.makeText(context, "Invitation Cancelled", Toast.LENGTH_LONG).show();
                    finish();
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

    private void status(String status){
        reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("status", status);

        reference.updateChildren(hashMap);
    }

    @Override
    protected void onResume() {
        super.onResume();
        status("online");
    }

    @Override
    protected void onPause() {
        super.onPause();
        status("offline");
    }

}