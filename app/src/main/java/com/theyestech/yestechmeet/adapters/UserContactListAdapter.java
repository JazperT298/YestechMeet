package com.theyestech.yestechmeet.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.ValueEventListener;
import com.theyestech.yestechmeet.R;
import com.theyestech.yestechmeet.activities.MessageActivity;
import com.theyestech.yestechmeet.listeners.UsersListener;
import com.theyestech.yestechmeet.models.Users;
import com.theyestech.yestechmeet.utils.GlideOptions;

import java.util.ArrayList;
import java.util.List;

public class UserContactListAdapter extends RecyclerView.Adapter<UserContactListAdapter.UsersViewHolder> {
    private List<Users> users;
    private UsersListener usersListener;
    private List<Users> selectedUsers;
    private Context context;

    public UserContactListAdapter(Context context, List<Users> users, UsersListener usersListener) {
        this.context = context;
        this.users = users;
        this.usersListener = usersListener;
        selectedUsers = new ArrayList<>();
    }


    public List<Users> getSelectedUsers() {
        return selectedUsers;
    }

    @NonNull
    @Override
    public UsersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new UsersViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.user_contact_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull UsersViewHolder holder, int position) {
        holder.setUsersData(users.get(position));
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public class UsersViewHolder extends RecyclerView.ViewHolder {
        private TextView username;
        private ImageView profile_image, iv_VideoMeeting, iv_AudioMeeting,iv_ImageSelected;
        private ImageView img_on;
        private ImageView img_off;
        private ConstraintLayout constraint;
        public UsersViewHolder(@NonNull View itemView) {
            super(itemView);
            username = itemView.findViewById(R.id.username);
            profile_image = itemView.findViewById(R.id.profile_image);
            img_on = itemView.findViewById(R.id.img_on);
            img_off = itemView.findViewById(R.id.img_off);
            constraint = itemView.findViewById(R.id.constraint);
            iv_VideoMeeting = itemView.findViewById(R.id.iv_VideoMeeting);
            iv_AudioMeeting = itemView.findViewById(R.id.iv_AudioMeeting);
            iv_ImageSelected = itemView.findViewById(R.id.iv_ImageSelected);
        }
        void setUsersData(Users users) {
            Glide.with(context)
                    .load(users.getProfilePhoto())
                    .apply(GlideOptions.getOptions())
                    .into(profile_image);
            username.setText(users.getName());
            iv_AudioMeeting.setOnClickListener(v -> usersListener.initiateAudioMeeting(users));
            iv_VideoMeeting.setOnClickListener(v -> usersListener.initiateVideoMeeting(users));

            profile_image.setOnLongClickListener(v -> {
                if (iv_ImageSelected.getVisibility() != View.VISIBLE) {
                    selectedUsers.add(users);
                    iv_ImageSelected.setVisibility(View.VISIBLE);
                    iv_VideoMeeting.setVisibility(View.GONE);
                    iv_AudioMeeting.setVisibility(View.GONE);
                    usersListener.onMultipleUsersAction(true);
                }
                return true;
            });
            profile_image.setOnClickListener(v -> {
                if (iv_ImageSelected.getVisibility() == View.VISIBLE) {
                    selectedUsers.remove(users);
                    iv_ImageSelected.setVisibility(View.GONE);
                    iv_VideoMeeting.setVisibility(View.VISIBLE);
                    iv_AudioMeeting.setVisibility(View.VISIBLE);
                    if (selectedUsers.size() == 0) {
                        usersListener.onMultipleUsersAction(false);
                    }
                } else {
                    if (selectedUsers.size() > 0) {
                        selectedUsers.add(users);
                        iv_ImageSelected.setVisibility(View.VISIBLE);
                        iv_VideoMeeting.setVisibility(View.GONE);
                        iv_AudioMeeting.setVisibility(View.GONE);
                    }
                }
            });
            constraint.setOnClickListener(v -> {
                Intent intent = new Intent(context, MessageActivity.class);
                intent.putExtra("userid", users.getId());
                context.startActivity(intent);
            });
        }
    }
}
