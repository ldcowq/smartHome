package com.hr.smarthome.ui.light;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.hr.smarthome.R;

import java.util.List;

public class LightAdapter extends RecyclerView.Adapter<LightAdapter.ViewHolder> {

    private Context mContext;
    private List<LightJavaBean> mLightList;

    static class ViewHolder extends RecyclerView.ViewHolder {

        CardView cardView;
        ImageView lightImage;
        TextView lightTextview;


        public ViewHolder(@NonNull View view) {
            super(view);
            cardView = (CardView) view;
            lightImage = view.findViewById(R.id.light_image);
            lightTextview = view.findViewById(R.id.light_textview);

        }
    }

    public LightAdapter(List<LightJavaBean> lightList) {
        mLightList = lightList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (mContext == null) {
            mContext = parent.getContext();
        }
        View view = LayoutInflater.from(mContext).inflate(R.layout.carview_item_light, parent, false);
        final ViewHolder holder = new ViewHolder(view);
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(mContext, "下拉可获取最新数据！", Toast.LENGTH_SHORT).show();
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        LightJavaBean lightJavaBean = mLightList.get(position);
        holder.lightTextview.setText("光照强度：% "+lightJavaBean.getLight());
        Glide.with(mContext).load(R.drawable.light).into(holder.lightImage);
    }

    @Override
    public int getItemCount() {
        return mLightList.size();
    }
}