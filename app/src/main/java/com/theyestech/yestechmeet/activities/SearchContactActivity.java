package com.theyestech.yestechmeet.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.theyestech.yestechmeet.R;
import com.theyestech.yestechmeet.adapters.UsersAdapter;
import com.theyestech.yestechmeet.interfaces.OnClickRecyclerView;
import com.theyestech.yestechmeet.models.Users;
import com.theyestech.yestechmeet.utils.GlideOptions;

import java.util.ArrayList;
import java.util.HashMap;

import es.dmoral.toasty.Toasty;

public class SearchContactActivity extends AppCompatActivity {
    private View view;
    private Context context;
    private TextInputEditText et_SearchUser;
    private ImageView iv_Back, iv_Close;

    private SwipeRefreshLayout swipe_Contacts;
    private ConstraintLayout indicator_empty_chat;
    private RecyclerView rv_Contacts;
    private ArrayList<Users> usersArrayList = new ArrayList<>();
    private UsersAdapter usersAdapter;
    private Users selectedUsers;

    private DatabaseReference reference, friendRequestRef, contactsRef;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private String currentUserId;
    private String currentState = "new";
    private AppCompatButton appCompatButton, appCompatButton2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_contact);
        context = this;
        initializeUI();
    }

    private void initializeUI() {

        firebaseAuth = FirebaseAuth.getInstance();
        currentUserId = firebaseAuth.getCurrentUser().getUid();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        friendRequestRef = FirebaseDatabase.getInstance().getReference().child("Friend Request");
        contactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts");

        et_SearchUser = findViewById(R.id.et_SearchUser);
        iv_Back = findViewById(R.id.iv_Back);
        iv_Close = findViewById(R.id.iv_Close);
        rv_Contacts = findViewById(R.id.rv_Contacts);
        swipe_Contacts = findViewById(R.id.swipe_Contacts);
        indicator_empty_chat = findViewById(R.id.view_EmptyRecord);
        et_SearchUser.requestFocus();
        rv_Contacts.setHasFixedSize(true);
        rv_Contacts.setLayoutManager(new LinearLayoutManager(context));

        readAllUsers();

        et_SearchUser.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                searchUsers(charSequence.toString().toLowerCase());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        iv_Back.setOnClickListener(v -> finish());

        iv_Close.setOnClickListener(v -> et_SearchUser.setText(""));
        swipe_Contacts.setOnRefreshListener(() -> readAllUsers());
    }

    private void searchUsers(String s) {

        final FirebaseUser fuser = FirebaseAuth.getInstance().getCurrentUser();
        Query query = FirebaseDatabase.getInstance().getReference("Users").orderByChild("search")
                .startAt(s)
                .endAt(s + "\uf8ff");

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                usersArrayList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Users users = snapshot.getValue(Users.class);

                    assert users != null;
                    assert fuser != null;
                    if (!users.getId().equals(fuser.getUid())) {
                        usersArrayList.add(users);
                    }
                }

                usersAdapter = new UsersAdapter(context, usersArrayList, false);
                rv_Contacts.setAdapter(usersAdapter);
                indicator_empty_chat.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void readAllUsers() {

        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (et_SearchUser.getText().toString().equals("")) {
                    usersArrayList.clear();
                    swipe_Contacts.setRefreshing(true);
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        swipe_Contacts.setRefreshing(false);
                        Users users = snapshot.getValue(Users.class);

                        if (!users.getId().equals(firebaseUser.getUid())) {
                            usersArrayList.add(users);
                        }

                    }

                    usersAdapter = new UsersAdapter(context, usersArrayList, false);
                    usersAdapter.setClickListener((view, position, fromButton) -> {
                        selectedUsers = usersArrayList.get(position);
                        openUsersProfile(selectedUsers.getId());
                    });
                    rv_Contacts.setAdapter(usersAdapter);
                    indicator_empty_chat.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void openUsersProfile(final String userid) {
        final Dialog dialog = new Dialog(context, android.R.style.Theme_Light_NoTitleBar);
        dialog.setContentView(R.layout.user_profile_dialog);
        final ImageView iv_Image, iv_userImage, iv_Back;
        final TextView tv_username, tv_email;

        iv_Image = dialog.findViewById(R.id.iv_Image);
        iv_userImage = dialog.findViewById(R.id.iv_userImage);
        iv_Back = dialog.findViewById(R.id.iv_Back);
        tv_username = dialog.findViewById(R.id.tv_username);
        tv_email = dialog.findViewById(R.id.tv_email);
        appCompatButton = dialog.findViewById(R.id.appCompatButton);
        appCompatButton2 = dialog.findViewById(R.id.appCompatButton2);
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

        friendRequestRef.child(currentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.hasChild(userid)) {
                    String requestType = snapshot.child(userid).child("request_type").getValue().toString();
                    if (requestType.equals("sent")) {
                        currentState = "request_sent";
                        appCompatButton.setText("CANCEL REQUEST");
                    } else if (requestType.equals("received")) {
                        currentState = "request_received";
                        appCompatButton.setText("ACCEPT REQUEST");
                        appCompatButton2.setVisibility(View.VISIBLE);
                        appCompatButton2.setOnClickListener(v -> cancelFriendRequest(userid));
                    }
                } else {
                    contactsRef.child(currentUserId)
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.hasChild(userid)) {
                                        currentState = "friends";
                                        appCompatButton.setText("DELETE CONTACT");
                                    } else {
                                        currentState = "new";
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        if (currentUserId.equals(userid)) {
            appCompatButton.setVisibility(View.GONE);
        } else {
            appCompatButton.setOnClickListener(v -> {
                if (currentState.equals("new")) {
                    sendFriendRequest(userid);
                }
                if (currentState.equals("request_sent")) {
                    cancelFriendRequest(userid);
                }
                if (currentState.equals("request_received")) {
                    acceptFriendRequest(userid);
                }
            });
        }


        dialog.show();
    }

    private void sendFriendRequest(final String userid) {
        friendRequestRef.child(currentUserId).child(userid).child("request_type").setValue("sent")
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        friendRequestRef.child(userid).child(currentUserId).child("request_type").setValue("received")
                                .addOnCompleteListener(task1 -> {
                                    if (task1.isSuccessful()) {
                                        currentState = "request_sent";
                                        appCompatButton.setText("CANCEL REQUEST");
                                        Toasty.success(context, "Friend request sent.").show();
                                    }
                                });
                    }
                });
    }

    private void cancelFriendRequest(final String userid) {
        friendRequestRef.child(currentUserId).child(userid).removeValue()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        friendRequestRef.child(userid).child(currentUserId).removeValue()
                                .addOnCompleteListener(task1 -> {
                                    currentState = "new";
                                    appCompatButton.setText("ADD FRIEND");
                                });
                    }
                });
    }

    private void acceptFriendRequest(final String userid) {
        contactsRef.child(currentUserId).child(userid).child("Contacts").setValue("Saved")
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        contactsRef.child(userid).child(currentUserId).child("Contacts").setValue("Saved")
                                .addOnCompleteListener(task13 -> {
                                    if (task13.isSuccessful()) {
                                        friendRequestRef.child(currentUserId).child(userid).removeValue()
                                                .addOnCompleteListener(task12 -> {
                                                    if (task12.isSuccessful()) {
                                                        friendRequestRef.child(userid).child(currentUserId).removeValue()
                                                                .addOnCompleteListener(task1 -> {
                                                                    if (task1.isSuccessful()) {
                                                                        currentState = "friends";
                                                                        appCompatButton.setText("REMOVE CONTACT");
                                                                        appCompatButton2.setVisibility(View.GONE);
                                                                    }
                                                                });
                                                    }
                                                });
                                    }
                                });
                    }
                });
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