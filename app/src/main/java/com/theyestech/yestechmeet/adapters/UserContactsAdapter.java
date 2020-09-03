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

public class UserContactsAdapter extends RecyclerView.Adapter<UserContactsAdapter.ViewHolder> {
    private View view;
    private Context context;
    private ArrayList<Users> usersArrayList;
    private boolean ischat;
    private LayoutInflater layoutInflater;
    private OnClickRecyclerView onClickRecyclerView;

    public UserContactsAdapter(Context context, ArrayList<Users> usersArrayList, boolean ischat){
        this.usersArrayList = usersArrayList;
        this.context = context;
        this.ischat = ischat;
        this.layoutInflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public UserContactsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        layoutInflater = LayoutInflater.from(context);
        view = layoutInflater.inflate(R.layout.suggested_user_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserContactsAdapter.ViewHolder holder, int position) {
        final Users users = usersArrayList.get(position);

        holder.username.setText(users.getName());
        if(users.getProfilePhoto().equals("default")){
            Glide.with(context)
                    .load(R.drawable.ic_account)
                    .apply(GlideOptions.getOptions())
                    .into(holder.profile_image);
        }else {
            Glide.with(context)
                    .load(users.getProfilePhoto())
                    .apply(GlideOptions.getOptions())
                    .into(holder.profile_image);
        }
    }

    @Override
    public int getItemCount() {
        return  usersArrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView username;
        private ImageView profile_image;
        private ConstraintLayout constraint;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            username = itemView.findViewById(R.id.username);
            profile_image = itemView.findViewById(R.id.profile_image);

            constraint = itemView.findViewById(R.id.constraint);
            constraint.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onClickRecyclerView != null)
                        onClickRecyclerView.onItemClick(v, getAdapterPosition(), 1);
                }
            });
        }
    }
    public void setClickListener(OnClickRecyclerView onClickRecyclerView) {
        this.onClickRecyclerView = onClickRecyclerView;
    }
}
