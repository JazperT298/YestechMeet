package com.theyestech.yestechmeet.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.theyestech.yestechmeet.R;
import com.theyestech.yestechmeet.interfaces.OnClickRecyclerView;
import com.theyestech.yestechmeet.models.Users;
import com.theyestech.yestechmeet.utils.GlideOptions;

import java.util.ArrayList;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.ViewHolder> {
    private View view;
    private Context context;
    private ArrayList<Users> usersArrayList;
    private boolean ischat;
    private LayoutInflater layoutInflater;
    private OnClickRecyclerView onClickRecyclerView;

    public UsersAdapter(Context context, ArrayList<Users> usersArrayList, boolean ischat){
        this.usersArrayList = usersArrayList;
        this.context = context;
        this.ischat = ischat;
        this.layoutInflater = LayoutInflater.from(context);
    }


    @NonNull
    @Override
    public UsersAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        layoutInflater = LayoutInflater.from(context);
        view = layoutInflater.inflate(R.layout.user_chat_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
        final Users users = usersArrayList.get(position);

        viewHolder.username.setText(users.getName());
        if(users.getProfilePhoto().equals("default")){
            Glide.with(context)
                    .load(R.drawable.ic_account)
                    .apply(GlideOptions.getOptions())
                    .into(viewHolder.profile_image);
        }else {
            Glide.with(context)
                    .load(users.getProfilePhoto())
                    .apply(GlideOptions.getOptions())
                    .into(viewHolder.profile_image);
        }
        if (users.getStatus().equals("online")){
            viewHolder.img_on.setVisibility(View.VISIBLE);
            viewHolder.img_off.setVisibility(View.GONE);
        } else {
            viewHolder.img_on.setVisibility(View.GONE);
            viewHolder.img_off.setVisibility(View.VISIBLE);
        }

    }

    @Override
    public int getItemCount() {
        return usersArrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView username;
        private ImageView profile_image;
        private ImageView img_on;
        private ImageView img_off;
        private ConstraintLayout constraint;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            username = itemView.findViewById(R.id.username);
            profile_image = itemView.findViewById(R.id.profile_image);
            img_on = itemView.findViewById(R.id.img_on);
            img_off = itemView.findViewById(R.id.img_off);
            constraint = itemView.findViewById(R.id.constraint);
            constraint.setOnClickListener(v -> {
                if (onClickRecyclerView != null)
                    onClickRecyclerView.onItemClick(v, getAdapterPosition(), 1);
            });
        }
    }
    public void setClickListener(OnClickRecyclerView onClickRecyclerView) {
        this.onClickRecyclerView = onClickRecyclerView;
    }
}
