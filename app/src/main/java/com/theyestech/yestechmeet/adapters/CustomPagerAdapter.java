package com.theyestech.yestechmeet.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.bumptech.glide.Glide;
import com.theyestech.yestechmeet.R;
import com.theyestech.yestechmeet.utils.GlideOptions;

import static org.webrtc.ContextUtils.getApplicationContext;

public class CustomPagerAdapter extends PagerAdapter {
    private Context mContext;
    LayoutInflater mLayoutInflater;
    private int[] mResources;
    private String[] mTitle;
    private String[] mDescription;

    public CustomPagerAdapter(Context context, int[] resources, String[] title, String[] description) {
        mContext = context;
        mLayoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mResources = resources;
        mTitle = title;
        mDescription = description;

    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {

        View itemView = mLayoutInflater.inflate(R.layout.pager_item,container,false);
        ImageView imageView = (ImageView) itemView.findViewById(R.id.imageView);
        TextView textView1 = itemView.findViewById(R.id.textView1);
        TextView textView2 = itemView.findViewById(R.id.textView2);
        //imageView.setImageResource(mResources[position]);

        Glide.with(mContext)
                .load(mResources[position])
                .apply(GlideOptions.getOptions())
                .into(imageView);
        textView1.setText(mTitle[position]);
        textView2.setText(mDescription[position]);
           /* LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(950, 950);
            imageView.setLayoutParams(layoutParams);*/
        container.addView(itemView);
        return itemView;
    }

    @Override
    public void destroyItem(ViewGroup collection, int position, Object view) {
        collection.removeView((View) view);
    }

    @Override
    public int getCount() {
        return mResources.length;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }
}
