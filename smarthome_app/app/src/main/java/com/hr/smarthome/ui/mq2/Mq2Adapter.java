package com.hr.smarthome.ui.mq2;

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

public class Mq2Adapter extends RecyclerView.Adapter<Mq2Adapter.ViewHolder> {

    private Context mContext;
    private List<Mq2JavaBean> mMq2List;

    static class ViewHolder extends RecyclerView.ViewHolder {

        CardView cardView;
        ImageView mq2Image;
        TextView mq2Textview;


        public ViewHolder(@NonNull View view) {
            super(view);
            cardView = (CardView) view;
            mq2Image = view.findViewById(R.id.mq2_image);
            mq2Textview = view.findViewById(R.id.mq2_textview);

        }
    }

    public Mq2Adapter(List<Mq2JavaBean> mq2List) {
        mMq2List = mq2List;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (mContext == null) {
            mContext = parent.getContext();
        }
        View view = LayoutInflater.from(mContext).inflate(R.layout.carview_item_mq2, parent, false);
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
        Mq2JavaBean mq2JavaBean = mMq2List.get(position);
        holder.mq2Textview.setText("烟雾浓度：% "+mq2JavaBean.getMq2());
        Glide.with(mContext).load(R.drawable.mq2).into(holder.mq2Image);
    }

    @Override
    public int getItemCount() {
        return mMq2List.size();
    }
}