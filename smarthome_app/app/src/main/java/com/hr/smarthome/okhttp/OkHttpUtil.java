package com.hr.smarthome.okhttp;

import okhttp3.OkHttpClient;
import okhttp3.Request;

public class OkHttpUtil {
    public static void sendHttpRequest(String address,okhttp3.Callback callback) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(address)
                .build();
        client.newCall(request).enqueue(callback);//enqueue()方法内部已经开好了子线程去执行http请求，结果返回到callback中
    }
}
