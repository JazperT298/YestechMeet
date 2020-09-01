package com.theyestech.yestechmeet.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.theyestech.yestechmeet.R;
import com.theyestech.yestechmeet.activities.NewMessageActivity;
import com.theyestech.yestechmeet.adapters.UserChatListAdapter;
import com.theyestech.yestechmeet.models.Chatlist;
import com.theyestech.yestechmeet.models.Users;
import com.theyestech.yestechmeet.notifications.Token;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class ChatFragment extends Fragment {
    private View view;
    private Context context;
    private DatabaseReference reference;

    private TextView tvHeader,tv_UserName;
    private ImageView ivProfile,iv_UserImage;
    private SwipeRefreshLayout swipe_ChatThreads;
    private RecyclerView rv_ChatThreads, rv_Contacts;
    private ConstraintLayout emptyIndicator;
    private FloatingActionButton floatingActionButton;
    private ProgressBar progressBar;
    private UserChatListAdapter userChatListAdapter;

    private String role;

    //Firebase
    private FirebaseUser firebaseUser;
    private FirebaseAuth firebaseAuth;

    private ArrayList<Users> usersArrayList = new ArrayList<>();
    private ArrayList<Users> usersArrayList2 = new ArrayList<>();

    private ArrayList<Chatlist> chatlistArrayList;
    private Users selectedUsers;
    private DatabaseReference usersRef;
    private String currentUserId;
    private String calledBy="";
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_chat, container, false);
        context = getContext();

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        initializeUI();

        return view;
    }

    private void initializeUI(){
        rv_Contacts = view.findViewById(R.id.rv_Contacts);
        rv_ChatThreads = view.findViewById(R.id.rv_ChatThreads);
        swipe_ChatThreads = view.findViewById(R.id.swipe_ChatThreads);
        emptyIndicator = view.findViewById(R.id.view_EmptyChat);
        floatingActionButton = view.findViewById(R.id.fab_ChatThreadNew);
        progressBar = view.findViewById(R.id.progress_ChatThreads);

        updateToken(FirebaseInstanceId.getInstance().getToken());

        getAllChats();
        readAllUsers();

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, NewMessageActivity.class);
                intent.putParcelableArrayListExtra("USERARRAYLIST", usersArrayList2);
                startActivity(intent);
            }
        });
    }

    @SuppressLint("RestrictedApi")
    private void accessingServer(boolean isAccessing) {
        floatingActionButton.setVisibility(isAccessing ? View.GONE : View.VISIBLE);
        progressBar.setVisibility(isAccessing ? View.VISIBLE : View.GONE);
    }

    private void getAllChats(){
        accessingServer(true);
        chatlistArrayList = new ArrayList<>();

        reference = FirebaseDatabase.getInstance().getReference("Chatlist").child(firebaseUser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                chatlistArrayList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Chatlist chatlist = snapshot.getValue(Chatlist.class);
                    chatlistArrayList.add(chatlist);
                }
                Collections.reverse(chatlistArrayList);
                chatUserList();
                accessingServer(false);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                accessingServer(false);
            }
        });
        updateToken(FirebaseInstanceId.getInstance().getToken());
    }

    private void chatUserList() {
        usersArrayList = new ArrayList<>();
        reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                usersArrayList.clear();
                emptyIndicator.setVisibility(View.GONE);
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Users users = snapshot.getValue(Users.class);
                    for (Chatlist chatlist : chatlistArrayList){
                        if (users.getId().equals(chatlist.getId())){
                            usersArrayList.add(users);
                        }
                    }
                }
                Collections.reverse(usersArrayList);

                rv_ChatThreads.setLayoutManager(new LinearLayoutManager(context));
                rv_ChatThreads.setHasFixedSize(true);

                userChatListAdapter = new UserChatListAdapter(context, usersArrayList, true);
                rv_ChatThreads.setAdapter(userChatListAdapter);
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
                usersArrayList2.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Users users = snapshot.getValue(Users.class);

                    if (!users.getId().equals(firebaseUser.getUid())) {
                        usersArrayList2.add(users);
                    }

                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    private void updateToken(String token){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Tokens");
        Token token1 = new Token(token);
        reference.child(firebaseUser.getUid()).setValue(token1);
    }

}