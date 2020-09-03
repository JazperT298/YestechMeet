package com.theyestech.yestechmeet.adapters;

import android.app.Dialog;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.theyestech.yestechmeet.R;
import com.theyestech.yestechmeet.activities.MessageActivity;
import com.theyestech.yestechmeet.interfaces.OnClickRecyclerView;
import com.theyestech.yestechmeet.models.Chat;
import com.theyestech.yestechmeet.models.Users;
import com.theyestech.yestechmeet.utils.DateTimeHandler;
import com.theyestech.yestechmeet.utils.GlideOptions;

import java.util.ArrayList;

public class UserChatListAdapter extends RecyclerView.Adapter<UserChatListAdapter.ViewHolder> {
    private View view;
    private Context context;
    private ArrayList<Users> usersArrayList;
    private boolean ischat;
    private LayoutInflater layoutInflater;
    private OnClickRecyclerView onClickRecyclerView;
    private DatabaseReference reference;

    String theLastMessage;

    public UserChatListAdapter(Context context, ArrayList<Users> usersArrayList, boolean ischat) {
        this.context = context;
        this.usersArrayList = usersArrayList;
        this.ischat = ischat;
        this.layoutInflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        layoutInflater = LayoutInflater.from(context);
        view = layoutInflater.inflate(R.layout.user_chat_list_item, viewGroup, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        final Users users = usersArrayList.get(i);
//        if (contact.getFullName().equals("  ") || contact.getFullName() == null || contact.getFullName().isEmpty()){
//            viewHolder.name.setText(contact.getEmail());
//        }else{
        viewHolder.name.setText(users.getName());
//        }
        //viewHolder.date.setText(DateTimeHandler.getMessageDateDisplay(users.getMessageDateCreated()));
        if (ischat) {
            lastMessage(users.getId(), viewHolder.last_msg, viewHolder.date);
        } else {
            viewHolder.last_msg.setVisibility(View.GONE);
        }


        if (users.getProfilePhoto().equals("default")) {
            viewHolder.profile_image.setImageResource(R.drawable.ic_account);
        } else {
            Glide.with(context)
                    .load(users.getProfilePhoto())
                    .apply(GlideOptions.getOptions())
                    .into(viewHolder.profile_image);
        }
        if (ischat) {
            if (users.getStatus().equals("online")) {
                viewHolder.img_on.setVisibility(View.VISIBLE);
                viewHolder.img_off.setVisibility(View.GONE);
            } else {
                viewHolder.img_on.setVisibility(View.GONE);
                viewHolder.img_off.setVisibility(View.VISIBLE);
            }
        } else {
            viewHolder.img_on.setVisibility(View.GONE);
            viewHolder.img_off.setVisibility(View.GONE);
        }

        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, MessageActivity.class);
                intent.putExtra("userid", users.getId());
                context.startActivity(intent);
            }
        });
//        viewHolder.constraint2.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent intent = new Intent(context, MessageActivity.class);
//                intent.putExtra("userid", users.getId());
//                context.startActivity(intent);
//            }
//        });
//        viewHolder.constraint1.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                openUsersProfile(users.getId());
//            }
//        });
    }


    @Override
    public int getItemCount() {
        return usersArrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView name;
        private TextView date;
        public ImageView profile_image;
        private ImageView img_on;
        private ImageView img_off;
        private TextView last_msg;
        private ConstraintLayout constraint1, constraint2;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.username);
            date = itemView.findViewById(R.id.date);
            profile_image = itemView.findViewById(R.id.profile_image);
            img_on = itemView.findViewById(R.id.img_on);
            img_off = itemView.findViewById(R.id.img_off);
            last_msg = itemView.findViewById(R.id.last_msg);
            constraint1 = itemView.findViewById(R.id.constraint1);
            constraint2 = itemView.findViewById(R.id.constraint2);
        }
    }

    public void setClickListener(OnClickRecyclerView onClickRecyclerView) {
        this.onClickRecyclerView = onClickRecyclerView;
    }

    //check for last message
    private void lastMessage(final String userid, final TextView last_msg, final TextView date) {
        theLastMessage = "default";
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Chats");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Chat chat = snapshot.getValue(Chat.class);
                    if (firebaseUser != null && chat != null) {
                        if (chat.getReceiverId().equals(firebaseUser.getUid()) && chat.getSenderId().equals(userid) ||
                                chat.getReceiverId().equals(userid) && chat.getSenderId().equals(firebaseUser.getUid())) {
                            theLastMessage = chat.getMessage();
                            date.setText(DateTimeHandler.getMessageDateDisplay(chat.getMessageDateCreated()));
                        }
                    }
                }

                switch (theLastMessage) {
                    case "default":
                        last_msg.setText("No Message");
                        break;

                    default:
                        if (theLastMessage.length() <= 30) {
                            last_msg.setText(theLastMessage);
                        } else {
                            last_msg.setText(theLastMessage.substring(0, 30) + "...");
                        }
                        break;
                }

                theLastMessage = "default";
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

        reference = FirebaseDatabase.getInstance().getReference("Users").child(userid);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Users user = dataSnapshot.getValue(Users.class);
                tv_username.setText(user.getUsername());
                tv_email.setText(user.getEmail());
                if (user.getProfilePhoto().equals("default")) {
                    //iv_ProfileImage.setImageResource(R.drawable.ai);
                    Glide.with(context).load(R.drawable.ai).into(iv_Image);
                    Glide.with(context)
                            .load(R.drawable.ai)
                            .apply(GlideOptions.getOptions())
                            .into(iv_userImage);
                } else {
                    //change this
                    Glide.with(context).load(user.getProfilePhoto()).into(iv_Image);
                    Glide.with(context)
                            .load(user.getProfilePhoto())
                            .apply(GlideOptions.getOptions())
                            .into(iv_userImage);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        iv_Back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

}


