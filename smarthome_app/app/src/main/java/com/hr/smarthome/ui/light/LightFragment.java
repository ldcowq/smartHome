package com.hr.smarthome.ui.light;

import androidx.lifecycle.ViewModelProviders;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hr.smarthome.R;
import com.hr.smarthome.okhttp.OkHttpUtil;
import com.hr.smarthome.ui.mq2.Mq2JavaBean;
import com.hr.smarthome.ui.temphumi.TempHumiJavaBean;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class LightFragment extends Fragment {

    private LightViewModel mViewModel;
    private List<LightJavaBean> mLightdataList = new ArrayList<>();
    private LightAdapter adapter;
    private SwipeRefreshLayout swipeRefresh;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_light, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.light_recyclerview);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 1);
        recyclerView.setLayoutManager(gridLayoutManager);
        adapter = new LightAdapter(mLightdataList);
        recyclerView.setAdapter(adapter);
        swipeRefresh = view.findViewById(R.id.light_swipe_refresh);
        swipeRefresh.setColorScheme(R.color.colorPrimary);
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //从数据库获取最新的温湿度数据
                refresh();
            }
        });
        System.out.println("LI:  oncreateview()________");
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
                if (isAdded()) {
                    requireActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            OkHttpUtil.sendHttpRequest("http://103.152.132.235:8080/smartHome/li?pagers=1", new Callback() {
                                @Override
                                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                                    String responseData = response.body().string();
                                    System.out.println(responseData);
                                    Gson gson = new Gson();
                                    List<LightJavaBean> lightJavaBeans = gson.fromJson(responseData, new TypeToken<List<LightJavaBean>>() {
                                    }.getType());
                                    for (LightJavaBean t : lightJavaBeans) {
                                        mLightdataList.add(new LightJavaBean(t.getLight()));
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
        System.out.println("LI:  start()________");
    }

    @Override
    public void onResume() {
        super.onResume();
        System.out.println("LI:  onresume()________");
    }


    @Override
    public void onPause() {
        super.onPause();
        System.out.println("LI:  onpause()________");
    }

    @Override
    public void onStop() {
        super.onStop();
        System.out.println("LI:  onstop()________");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        System.out.println("LI:  ondestroy()________");
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = ViewModelProviders.of(this).get(LightViewModel.class);
    }

}