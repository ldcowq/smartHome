package com.hr.smarthome.ui.temphumi;


import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;


import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;


import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hr.smarthome.R;
import com.hr.smarthome.okhttp.OkHttpUtil;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class TemphumiFragment extends Fragment {
    private List<TempHumiJavaBean> mTHdataList = new ArrayList<>();
    private TempHumiAdapter adapter;
    private SwipeRefreshLayout swipeRefresh;
    RecyclerView recyclerView;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_temphumi, container, false);

        recyclerView = view.findViewById(R.id.temp_recyclerview);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 1);
        recyclerView.setLayoutManager(gridLayoutManager);
        adapter = new TempHumiAdapter(mTHdataList);
        recyclerView.setAdapter(adapter);
        swipeRefresh = view.findViewById(R.id.temp_swipe_refresh);
        swipeRefresh.setColorScheme(R.color.colorPrimary);
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //从数据库获取最新的温湿度数据
                refresh();
            }
        });
        System.out.println("oncreateview()________");
        refresh();
        return view;
    }


    private void refresh() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(2000);

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (isAdded()) {//判断fragment是否已经添加到activity中
                    requireActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            OkHttpUtil.sendHttpRequest("http://103.152.132.235:8080/smartHome/th?pagers=1", new Callback() {
                                @Override
                                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                                    String responseData = response.body().string();
                                    System.out.println(responseData);
                                    Gson gson = new Gson();
                                    List<TempHumiJavaBean> tempHumiJavaBeans = gson.fromJson(responseData, new TypeToken<List<TempHumiJavaBean>>() {
                                    }.getType());
                                    for (TempHumiJavaBean t : tempHumiJavaBeans) {
                                        mTHdataList.add(new TempHumiJavaBean(t.getTemp(), t.getHumi()));
                                    }
                                    swipeRefresh.setRefreshing(false);
                                }

                                @Override
                                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                                    e.printStackTrace();
                                }
                            });
                            adapter.notifyDataSetChanged();

                        }
                    });
                }
            }
        }).start();
    }

    @Override
    public void onStart() {
        super.onStart();
        System.out.println("TH:  start()________");
    }

    @Override
    public void onResume() {
        super.onResume();
        System.out.println("TH:  onresume()________");
    }


    @Override
    public void onPause() {
        super.onPause();
        System.out.println("TH:  onpause()________");
    }

    @Override
    public void onStop() {
        super.onStop();
        System.out.println("TH:  onstop()________");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        System.out.println("TH:  ondestroy()________");
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }
}



