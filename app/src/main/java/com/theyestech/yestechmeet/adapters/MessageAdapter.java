package com.theyestech.yestechmeet.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.theyestech.yestechmeet.R;
import com.theyestech.yestechmeet.interfaces.OnClickRecyclerView;
import com.theyestech.yestechmeet.models.Chat;
import com.theyestech.yestechmeet.utils.GlideOptions;

import java.util.ArrayList;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

    public static final int MSG_TYPE_LEFT = 0;
    public static final int MSG_TYPE_RIGHT = 1;

    private Context mContext;
    private ArrayList<Chat> chatArrayList;
    private String imageurl;
    private OnClickRecyclerView onClickRecyclerView;

    FirebaseUser fuser;

    public MessageAdapter(Context mContext, ArrayList<Chat> chatArrayList, String imageurl) {
        this.chatArrayList = chatArrayList;
        this.mContext = mContext;
        this.imageurl = imageurl;
    }

    @NonNull
    @Override
    public MessageAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == MSG_TYPE_RIGHT) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.chat_item_right, parent, false);
            return new MessageAdapter.ViewHolder(view);
        } else {
            View view = LayoutInflater.from(mContext).inflate(R.layout.chat_item_left, parent, false);
            return new MessageAdapter.ViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull MessageAdapter.ViewHolder holder, int position) {

        Chat chat = chatArrayList.get(position);

        holder.show_message.setText(chat.getMessage());
        //holder.date.setText(DateTimeHandler.getMessageDateDisplay(chat.getMessageDateCreated()));
        if (imageurl.equals("default")) {
            Glide.with(mContext).load(R.drawable.ic_account).into(holder.profile_image);
        } else {
            Glide.with(mContext)
                    .load(imageurl)
                    .apply(GlideOptions.getOptions())
                    .into(holder.profile_image);
        }


        if (position == chatArrayList.size() - 1) {
            if (chat.isIsseen()) {
                holder.txt_seen.setText("Seen");
            } else {
                holder.txt_seen.setText("Delivered");
            }
        } else {
            holder.txt_seen.setVisibility(View.GONE);
        }

    }

    @Override
    public int getItemCount() {
        return chatArrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView show_message;
        public ImageView profile_image;
        public TextView txt_seen;
        public ImageView iv_More;
        //private TextView date;

        public ViewHolder(View itemView) {
            super(itemView);

            show_message = itemView.findViewById(R.id.show_message);
            profile_image = itemView.findViewById(R.id.iv_ProfileEducatorImage);
            txt_seen = itemView.findViewById(R.id.txt_seen);
            iv_More = itemView.findViewById(R.id.iv_More);
            iv_More.setOnClickListener(v -> {
                if (onClickRecyclerView != null)
                    onClickRecyclerView.onItemClick(v, getAdapterPosition(), 1);
            });

            //date = itemView.findViewById(R.id.date);
        }
    }

    @Override
    public int getItemViewType(int position) {
        fuser = FirebaseAuth.getInstance().getCurrentUser();
        if (chatArrayList.get(position).getSenderId().equals(fuser.getUid())) {
            return MSG_TYPE_RIGHT;
        } else {
            return MSG_TYPE_LEFT;
        }
    }

    public void setClickListener(OnClickRecyclerView onClickRecyclerView) {
        this.onClickRecyclerView = onClickRecyclerView;
    }
}
