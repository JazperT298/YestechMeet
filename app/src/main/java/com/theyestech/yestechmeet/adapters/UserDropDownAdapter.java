package com.theyestech.yestechmeet.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.theyestech.yestechmeet.R;
import com.theyestech.yestechmeet.interfaces.OnClickRecyclerView;
import com.theyestech.yestechmeet.models.Users;
import com.theyestech.yestechmeet.utils.GlideOptions;

import java.util.ArrayList;

public class UserDropDownAdapter extends ArrayAdapter {
    private ArrayList<Users> usersArrayList;
    private Context context;
    private int itemLayout;
    private ListFilter listFilter = new ListFilter();
    private ArrayList<Users> allUsersArrayList;

    public UserDropDownAdapter(Context context, int resource, ArrayList<Users> usersArrayList) {
        super(context, resource, usersArrayList);
        this.usersArrayList = usersArrayList;
        this.context = context;
        itemLayout = resource;
    }
    @Override
    public int getCount() {
        assert usersArrayList != null;
        return usersArrayList.size();
    }

    @Override
    public Users getItem(int position) {
        return usersArrayList.get(position);
    }

    @Override
    public View getView(int position, View view, @NonNull ViewGroup parent) {

        if (view == null) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(itemLayout, parent, false);
        }

        TextView username = view.findViewById(R.id.username);
        ImageView profile_image = view.findViewById(R.id.profile_image);

        Users users = usersArrayList.get(position);

        Glide.with(context)
                .load(users.getProfilePhoto())
                .apply(RequestOptions.placeholderOf(R.drawable.user1))
                .apply(GlideOptions.getOptions())
                .into(profile_image);

        username.setText(users.getName());


        return view;
    }

    @NonNull
    @Override
    public Filter getFilter() {
        return listFilter;
    }

    public class ListFilter extends Filter {
        private final Object lock = new Object();

        @Override
        protected FilterResults performFiltering(CharSequence prefix) {
            FilterResults results = new FilterResults();
            if (allUsersArrayList == null) {
                synchronized (lock) {
                    allUsersArrayList = new ArrayList<>(usersArrayList);
                }
            }

            if (prefix == null || prefix.length() == 0) {
                synchronized (lock) {
                    results.values = allUsersArrayList;
                    results.count = allUsersArrayList.size();
                }
            } else {
                final String searchStrLowerCase = prefix.toString().toLowerCase();

                ArrayList<Users> matchValues = new ArrayList<>();

                for (Users users : allUsersArrayList) {
                    if (users.getName().toLowerCase().contains(searchStrLowerCase)) {
                        matchValues.add(users);
                    }
                }

                results.values = matchValues;
                results.count = matchValues.size();
            }

            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            if (results.values != null) {
                usersArrayList = (ArrayList<Users>) results.values;
            } else {
                usersArrayList = null;
            }
            if (results.count > 0) {
                notifyDataSetChanged();
            } else {
                notifyDataSetInvalidated();
            }
        }

    }

}
