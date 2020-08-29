package com.theyestech.yestechmeet.fragments;

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

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.theyestech.yestechmeet.R;
import com.theyestech.yestechmeet.activities.MessageActivity;
import com.theyestech.yestechmeet.activities.SearchContactActivity;
import com.theyestech.yestechmeet.adapters.UsersAdapter;
import com.theyestech.yestechmeet.models.Users;
import com.theyestech.yestechmeet.utils.GlideOptions;

import java.util.ArrayList;

public class ContactsFragment extends Fragment {
    private View view;
    private Context context;
    private FirebaseUser firebaseUser;
    private DatabaseReference reference;

    private SwipeRefreshLayout swipe_Contacts;
    private ConstraintLayout indicator_empty_chat;
    private RecyclerView rv_Contacts;
    private FloatingActionButton fab_NewContact;
    private ProgressBar progress_Contact;
    private ArrayList<Users> usersArrayList = new ArrayList<>();
    private UsersAdapter usersAdapter;
    private Users selectedUsers;

    private DatabaseReference usersRef, friendRequestRef, contactsRef;
    private FirebaseAuth firebaseAuth;
    private String currentUserId;
    private String userName = "", profileImage = "", calledBy = "";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_contacts, container, false);
        context = getContext();
        initializeUI();
        readAllUsers();
        return view;
    }

    private void initializeUI() {
        swipe_Contacts = view.findViewById(R.id.swipe_Contacts);
        indicator_empty_chat = view.findViewById(R.id.view_EmptyChat);
        rv_Contacts = view.findViewById(R.id.rv_Contacts);
        fab_NewContact = view.findViewById(R.id.fab_NewContact);
        progress_Contact = view.findViewById(R.id.progress_Contact);

        firebaseAuth = FirebaseAuth.getInstance();
        currentUserId = firebaseAuth.getCurrentUser().getUid();
        friendRequestRef = FirebaseDatabase.getInstance().getReference().child("Friend Request");
        contactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts");
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        rv_Contacts.setHasFixedSize(true);
        rv_Contacts.setLayoutManager(new LinearLayoutManager(context));

        swipe_Contacts.setOnRefreshListener(() -> readAllUsers());
        fab_NewContact.setOnClickListener(v -> {
            Intent intent = new Intent(context, SearchContactActivity.class);
            startActivity(intent);
        });
    }

    private void readAllUsers() {

        FirebaseRecyclerOptions<Users> options = new FirebaseRecyclerOptions.Builder<Users>()
                .setQuery(contactsRef.child(currentUserId), Users.class).build();

        FirebaseRecyclerAdapter<Users, ContactsViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Users, ContactsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final ContactsViewHolder holder, int position, @NonNull Users model) {
                final String listUserId = getRef(position).getKey();

                usersRef.child(listUserId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        swipe_Contacts.setRefreshing(true);
                        if (snapshot.exists()) {
                            swipe_Contacts.setRefreshing(false);
                            userName = snapshot.child("name").getValue().toString();
                            profileImage = snapshot.child("profilePhoto").getValue().toString();

                            holder.username.setText(userName);
                            Glide.with(context)
                                    .load(profileImage)
                                    .apply(GlideOptions.getOptions())
                                    .into(holder.profile_image);
                            //holder.date.setText();
                            holder.constraint.setOnClickListener(v -> {
                                //openUsersProfile(listUserId);
                                Intent intent = new Intent(context, MessageActivity.class);
                                intent.putExtra("userid", listUserId);
                                startActivity(intent);
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }

            @NonNull
            @Override
            public ContactsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_chat_item, parent, false);
                ContactsViewHolder viewHolder = new ContactsViewHolder(view);
                return viewHolder;
            }
        };
        rv_Contacts.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
        indicator_empty_chat.setVisibility(View.GONE);
    }

    private static class ContactsViewHolder extends RecyclerView.ViewHolder {
        private TextView username;
        private ImageView profile_image;
        private ImageView img_on;
        private ImageView img_off;
        private ConstraintLayout constraint;

        public ContactsViewHolder(@NonNull View itemView) {
            super(itemView);
            username = itemView.findViewById(R.id.username);
            profile_image = itemView.findViewById(R.id.profile_image);
            img_on = itemView.findViewById(R.id.img_on);
            img_off = itemView.findViewById(R.id.img_off);
            constraint = itemView.findViewById(R.id.constraint);

        }
    }

}