package com.theyestech.yestechmeet.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.theyestech.yestechmeet.R;
import com.theyestech.yestechmeet.adapters.MessageAdapter;
import com.theyestech.yestechmeet.listeners.UsersListener;
import com.theyestech.yestechmeet.models.Chat;
import com.theyestech.yestechmeet.models.Users;
import com.theyestech.yestechmeet.notifications.Data;
import com.theyestech.yestechmeet.notifications.MyResponse;
import com.theyestech.yestechmeet.notifications.Sender;
import com.theyestech.yestechmeet.notifications.Token;
import com.theyestech.yestechmeet.services.ApiService;
import com.theyestech.yestechmeet.services.ApiClient;
import com.theyestech.yestechmeet.utils.GlideOptions;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import es.dmoral.toasty.Toasty;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MessageActivity extends AppCompatActivity implements UsersListener{
    private Context context;
    //Widgets
    private ImageView profile_image;
    private TextView username;
    private ImageView btn_send,iv_Back,iv_More,iv_Video,iv_File,iv_Audio;
    private EditText text_send;
    private RecyclerView recyclerView;
    private String role;

    //Array, Adapter, Variables
    private MessageAdapter messageAdapter;
    private ArrayList<Chat> chatArrayList;
    private String userid;
    private Date currentDate;

    private Intent intent;

    //Services
    private ApiService apiService;
    private ValueEventListener seenListener;

    //Firebase
    private FirebaseAuth firebaseAuth;
    private FirebaseUser fuser;
    private DatabaseReference reference;

    boolean notify = false;
    private UsersListener usersListener;
    private Users users;
    private String token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);
        context = this;

        initializeUI();
        setUserHeader();
    }
    private void initializeUI() {
        //Firebase Database
        apiService = ApiClient.getClient().create(ApiService.class);

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        profile_image = findViewById(R.id.iv_ProfileEducatorImage);
        username = findViewById(R.id.username);
        btn_send = findViewById(R.id.btn_send);
        text_send = findViewById(R.id.text_send);
        iv_Back = findViewById(R.id.iv_Back);
        iv_More = findViewById(R.id.iv_More);
        iv_Video = findViewById(R.id.iv_Video);
        iv_Audio = findViewById(R.id.iv_Audio);
        iv_File = findViewById(R.id.iv_File);

        intent = getIntent();
        userid = intent.getStringExtra("userid");

        firebaseAuth = FirebaseAuth.getInstance();
        fuser = FirebaseAuth.getInstance().getCurrentUser();

        currentDate = Calendar.getInstance().getTime();

        iv_Back.setOnClickListener(v -> finish());
        profile_image.setOnClickListener(v -> openUsersProfile(userid));
        iv_More.setOnClickListener(v -> openUsersProfile(userid));
        iv_Video.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (token == null || token.trim().isEmpty()) {
                    Toast.makeText(context, users.getUsername() + " " + "is not available for meeting", Toast.LENGTH_LONG).show();
                } else {
                    Intent intent = new Intent(context, OutgoingInvitationActivity.class);
                    intent.putExtra("users", users);
                    intent.putExtra("type", "video");
                    startActivity(intent);
                }
            }
        });
        iv_Audio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (users.getToken() == null || users.getToken().trim().isEmpty()) {
                    Toast.makeText(context, users.getUsername() + " " + "is not available for meeting", Toast.LENGTH_LONG).show();
                } else {
                    Intent intent = new Intent(context, OutgoingInvitationActivity.class);
                    intent.putExtra("userid", userid);
                    intent.putExtra("type", "audio");
                    startActivity(intent);
                }
            }
        });
        iv_File.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //openUsersProfile(userid);
            }
        });


        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                notify = true;
                String msg = text_send.getText().toString();
                if (!msg.equals("")){
                    sendUserMessage(fuser.getUid(), userid, msg, currentDate);
                } else {
                    Toasty.warning(context, "You can't send empty message").show();
                }
                text_send.setText("");
            }
        });

    }

    private void setUserHeader(){
        reference = FirebaseDatabase.getInstance().getReference("Users").child(userid);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                users = dataSnapshot.getValue(Users.class);
                username.setText(users.getName());
                Glide.with(getApplicationContext())
                        .load(users.getProfilePhoto())
                        .apply(GlideOptions.getOptions())
                        .into(profile_image);
                token = users.getToken();
                readUserMessages(fuser.getUid(), userid, users.getProfilePhoto());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        seenUserMessage(userid);
    }

    private void seenUserMessage(final String userid){
        reference = FirebaseDatabase.getInstance().getReference("Chats");
        seenListener = reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Chat chat = snapshot.getValue(Chat.class);
                    if (chat.getReceiverId().equals(fuser.getUid()) && chat.getSenderId().equals(userid)){
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("isseen", true);
                        snapshot.getRef().updateChildren(hashMap);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void sendUserMessage(String senderId, final String receiverId, String message, Date currentDate){

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("senderId", senderId);
        hashMap.put("receiverId", receiverId);
        hashMap.put("message", message);
        hashMap.put("messageDateCreated", currentDate);
        hashMap.put("isseen", false);

        reference.child("Chats").push().setValue(hashMap);


        // add user to chat fragment
        final DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference("Chatlist")
                .child(fuser.getUid())
                .child(userid);

        chatRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()){
                    chatRef.child("id").setValue(userid);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        final DatabaseReference chatRefReceiver = FirebaseDatabase.getInstance().getReference("Chatlist")
                .child(userid)
                .child(fuser.getUid());
        chatRefReceiver.child("id").setValue(fuser.getUid());

        final String msg = message;

        reference = FirebaseDatabase.getInstance().getReference("Users").child(fuser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Users users = dataSnapshot.getValue(Users.class);
                if (notify) {
                    //if (users.getName() == null){
                    sendUserNotification(receiverId, users.getName(), msg);
//                    }else{
//                        sendUserNotification(receiverId, users.getFullName(), msg);
//                    }
                }
                notify = false;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void readUserMessages(final String myid, final String userid, final String imageurl){
        chatArrayList = new ArrayList<>();

        reference = FirebaseDatabase.getInstance().getReference("Chats");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                chatArrayList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Chat chat = snapshot.getValue(Chat.class);
                    if (chat.getReceiverId().equals(myid) && chat.getSenderId().equals(userid) ||
                            chat.getReceiverId().equals(userid) && chat.getSenderId().equals(myid)){
                        chatArrayList.add(chat);
                    }

                    messageAdapter = new MessageAdapter(context, chatArrayList, imageurl);
                    recyclerView.setAdapter(messageAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void sendUserNotification(String receiver, final String username, final String message){
        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference("Tokens");
        Query query = tokens.orderByKey().equalTo(receiver);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Token token = snapshot.getValue(Token.class);
                    Data data = new Data(fuser.getUid(), R.mipmap.ic_launcher, username+": "+message, "New Message", currentDate,
                            userid);

                    Sender sender = new Sender(data, token.getToken());

                    apiService.sendNotification(sender)
                            .enqueue(new Callback<MyResponse>() {
                                @Override
                                public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                                    if (response.code() == 200){
                                        if (response.body().success != 1){
                                            Toasty.error(context, "Failed!").show();
                                        }
                                    }
                                }

                                @Override
                                public void onFailure(Call<MyResponse> call, Throwable t) {

                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void openUsersProfile(final String userid){
        final Dialog dialog=new Dialog(context,android.R.style.Theme_Light_NoTitleBar);
        dialog.setContentView(R.layout.user_friend_profile);
        final ImageView iv_Image, iv_userImage, iv_Back;
        final TextView tv_username, tv_email;

        iv_Image = dialog.findViewById(R.id.iv_Image);
        iv_userImage = dialog.findViewById(R.id.iv_userImage);
        iv_Back = dialog.findViewById(R.id.iv_Back);
        tv_username = dialog.findViewById(R.id.tv_username);
        tv_email = dialog.findViewById(R.id.tv_email);

        reference = FirebaseDatabase.getInstance().getReference("Users").child(userid);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Users user = dataSnapshot.getValue(Users.class);
                tv_username.setText(user.getUsername());
                tv_email.setText(user.getEmail());
                if (user.getProfilePhoto().equals("default")) {
                    //iv_ProfileImage.setImageResource(R.drawable.ai);
                    Glide.with(getApplicationContext()).load(R.drawable.ai).into(iv_Image);
                    Glide.with(getApplicationContext())
                            .load(R.drawable.ai)
                            .apply(GlideOptions.getOptions())
                            .into(iv_userImage);
                } else {
                    //change this
                    Glide.with(getApplicationContext()).load(user.getProfilePhoto()).into(iv_Image);
                    Glide.with(getApplicationContext())
                            .load(user.getProfilePhoto())
                            .apply(GlideOptions.getOptions())
                            .into(iv_userImage);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        iv_Back.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void currentUser(String userid){
        SharedPreferences.Editor editor = getSharedPreferences("PREFS", MODE_PRIVATE).edit();
        editor.putString("currentuser", userid);
        editor.apply();
    }

    private void status(String status){
        reference = FirebaseDatabase.getInstance().getReference("Users").child(fuser.getUid());

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("status", status);

        reference.updateChildren(hashMap);
    }

    @Override
    protected void onResume() {
        super.onResume();
        status("online");
        currentUser(userid);
        setUserHeader();
    }

    @Override
    protected void onPause() {
        super.onPause();
        reference.removeEventListener(seenListener);
        status("offline");
        currentUser("none");
        setUserHeader();
    }

    @Override
    public void initiateVideoMeeting(Users users) {
        if (users.getToken() == null || users.getToken().trim().isEmpty()) {
            Toast.makeText(this, users.getUsername() + " " + "is not available for meeting", Toast.LENGTH_LONG).show();
        } else {
            Intent intent = new Intent(context, OutgoingInvitationActivity.class);
            intent.putExtra("users", users);
            intent.putExtra("type", "video");
            startActivity(intent);
        }
    }

    @Override
    public void initiateAudioMeeting(Users users) {
        if (users.getToken() == null || users.getToken().trim().isEmpty()) {
            Toast.makeText(this, users.getUsername() + " " +  "is not available for meeting", Toast.LENGTH_LONG).show();
        } else {
            Intent intent = new Intent(context, OutgoingInvitationActivity.class);
            intent.putExtra("users", users);
            intent.putExtra("type", "audio");
            startActivity(intent);
        }
    }

    @Override
    public void onMultipleUsersAction(Boolean isMultipleUsersSelected) {
//        if (isMultipleUsersSelected) {
//            iv_Conference.setVisibility(View.VISIBLE);
//            iv_Conference.setOnClickListener(v -> {
//                Intent intent = new Intent(context, OutgoingInvitationActivity.class);
//                intent.putExtra("selectedUsers", new Gson().toJson(usersAdapter.getSelectedUsers()));
//                intent.putExtra("type", "video");
//                intent.putExtra("isMultiple", true);
//                startActivity(intent);
//            });
//        } else {
//            iv_Conference.setVisibility(View.GONE);
//        }
    }
}
