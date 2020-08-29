package com.theyestech.yestechmeet.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.theyestech.yestechmeet.R;
import com.theyestech.yestechmeet.adapters.NotificationAdapter;
import com.theyestech.yestechmeet.models.Notification;
import com.theyestech.yestechmeet.models.Users;
import com.theyestech.yestechmeet.utils.GlideOptions;

import java.util.ArrayList;

import es.dmoral.toasty.Toasty;

public class NotificationActivity extends AppCompatActivity {

    private View view;
    private Context context;
    private ImageView iv_Back, iv_More;
    private SwipeRefreshLayout swipe_Notification;
    private ConstraintLayout indicator_empty_chat;
    private RecyclerView rv_Notification;

    private ArrayList<Notification> notificationArrayList;
    private NotificationAdapter notificationAdapter;
    private Notification selectedNotification;

    private DatabaseReference usersRef, friendRequestRef, contactsRef;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private String currentUserId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);
        context = this;

        initializeUI();
    }

    private void initializeUI(){

        firebaseAuth = FirebaseAuth.getInstance();
        currentUserId = firebaseAuth.getCurrentUser().getUid();
        friendRequestRef = FirebaseDatabase.getInstance().getReference().child("Friend Request");
        contactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts");
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        iv_Back = findViewById(R.id.iv_Back);
        iv_More = findViewById(R.id.iv_More);
        swipe_Notification = findViewById(R.id.swipe_Notification);
        indicator_empty_chat = findViewById(R.id.view_EmptyChat);
        rv_Notification = findViewById(R.id.rv_Notification);

        iv_Back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        swipe_Notification.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getAllNotifications();
            }
        });
        getAllNotifications();
    }

    private void getAllNotifications(){
        notificationArrayList = new ArrayList<>();
        rv_Notification.setLayoutManager(new LinearLayoutManager(context));

        FirebaseRecyclerOptions options = new FirebaseRecyclerOptions.Builder<Users>()
                .setQuery(friendRequestRef.child(currentUserId), Users.class).build();

        FirebaseRecyclerAdapter<Users, NotificationsViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Users, NotificationsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final NotificationsViewHolder holder, int position, @NonNull Users model) {
                holder.tv_DeclineRequest.setVisibility(View.VISIBLE);
                holder.tv_AcceptRequest.setVisibility(View.VISIBLE);

                final String listUserId =  getRef(position).getKey();

                DatabaseReference requestTypeRef = getRef(position).child("request_type").getRef();
                requestTypeRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        swipe_Notification.setRefreshing(true);
                        if(snapshot.exists()){
                            String type = snapshot.getValue().toString();
                            if(type.equals("received")){
                                swipe_Notification.setRefreshing(false);
                                holder.constraint.setVisibility(View.VISIBLE);
                                usersRef.child(listUserId).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        if(snapshot.hasChild("profilePhoto")){
                                            final String profilePhoto = snapshot.child("profilePhoto").getValue().toString();
                                            Glide.with(getApplicationContext())
                                                    .load(profilePhoto)
                                                    .apply(GlideOptions.getOptions())
                                                    .into(holder.profile_image);
                                        }
                                        final String name = snapshot.child("name").getValue().toString();
                                        holder.username.setText(name);

                                        holder.tv_AcceptRequest.setOnClickListener(v -> {
                                            swipe_Notification.setRefreshing(false);
                                            acceptFriendRequest(listUserId);
                                        });
                                        holder.tv_DeclineRequest.setOnClickListener(v -> {
                                            swipe_Notification.setRefreshing(false);
                                            cancelFriendRequest(listUserId);
                                        });
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                            }else{
                                holder.constraint.setVisibility(View.GONE);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }

            @NonNull
            @Override
            public NotificationsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.listrow_friend_request, parent,false);
                NotificationsViewHolder viewHolder = new NotificationsViewHolder(view);
                return viewHolder;
            }

        };
        rv_Notification.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
        indicator_empty_chat.setVisibility(View.GONE);
    }

    private static class NotificationsViewHolder extends RecyclerView.ViewHolder{
        private TextView username,tv_DeclineRequest,tv_AcceptRequest;
        private ImageView profile_image;
        private ImageView img_on;
        private ImageView img_off;
        private ConstraintLayout constraint;

        public NotificationsViewHolder(@NonNull View itemView) {
            super(itemView);
            username = itemView.findViewById(R.id.username);
            profile_image = itemView.findViewById(R.id.profile_image);
            img_on = itemView.findViewById(R.id.img_on);
            img_off = itemView.findViewById(R.id.img_off);
            tv_DeclineRequest = itemView.findViewById(R.id.tv_DeclineRequest);
            tv_AcceptRequest = itemView.findViewById(R.id.tv_AcceptRequest);
            constraint = itemView.findViewById(R.id.constraint);

        }
    }

    private void acceptFriendRequest(final String userid){
        contactsRef.child(currentUserId).child(userid).child("Contacts").setValue("Saved")
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        contactsRef.child(userid).child(currentUserId).child("Contacts").setValue("Saved")
                                .addOnCompleteListener(task13 -> {
                                    if(task13.isSuccessful()){
                                        friendRequestRef.child(currentUserId).child(userid).removeValue()
                                                .addOnCompleteListener(task12 -> {
                                                    if (task12.isSuccessful()){
                                                        friendRequestRef.child(userid).child(currentUserId).removeValue()
                                                                .addOnCompleteListener(task1 -> {
                                                                    if(task1.isSuccessful()){
                                                                        swipe_Notification.setRefreshing(false);
                                                                        Toasty.success(context, "Friend Request Accepted").show();
                                                                    }
                                                                });
                                                    }
                                                });
                                    }
                                });
                    }
                });
    }

    private void cancelFriendRequest(final String userid){
        friendRequestRef.child(currentUserId).child(userid).removeValue()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        friendRequestRef.child(userid).child(currentUserId).removeValue()
                                .addOnCompleteListener(task1 -> {
                                    swipe_Notification.setRefreshing(false);
                                    Toasty.warning(context, "Friend Request Cancelled").show();
                                });
                    }
                });
    }

}