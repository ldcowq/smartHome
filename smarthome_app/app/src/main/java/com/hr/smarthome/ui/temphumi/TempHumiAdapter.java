package com.hr.smarthome.ui.temphumi;

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

public class TempHumiAdapter extends RecyclerView.Adapter<TempHumiAdapter.ViewHolder> {

    private Context mContext;
    private List<TempHumiJavaBean> mTempHumiList;

    static class ViewHolder extends RecyclerView.ViewHolder {

        CardView cardView;
        ImageView tempImage;
        TextView tempTextview;
        ImageView humiImage;
        TextView humiTextview;

        public ViewHolder(@NonNull View view) {
            super(view);
            cardView = (CardView) view;
            tempImage = view.findViewById(R.id.temp_image);
            tempTextview = view.findViewById(R.id.temp_textview);
            humiImage = view.findViewById(R.id.humi_image);
            humiTextview = view.findViewById(R.id.humi_textview);
        }
    }

    //不能返回0
    @Override
    public int getItemCount() {
        return mTempHumiList.size();
    }

    public TempHumiAdapter(List<TempHumiJavaBean> temphumiList) {
        mTempHumiList = temphumiList;
    }

    @NonNull
    @Override
    public TempHumiAdapter.ViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, int viewType) {
        if (mContext == null) {
            mContext = parent.getContext();
        }
        View view = LayoutInflater.from(mContext).inflate(R.layout.carview_item_temphumi, parent, false);
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
    public void onBindViewHolder(@NonNull TempHumiAdapter.ViewHolder holder, int position) {
        TempHumiJavaBean th = mTempHumiList.get(position);
        holder.tempTextview.setText("温度："+th.getTemp()+"℃");
        holder.humiTextview.setText("湿度：% "+th.getHumi());
        Glide.with(mContext).load(R.drawable.temp).into(holder.tempImage);
        Glide.with(mContext).load(R.drawable.humi).into(holder.humiImage);
    }


}