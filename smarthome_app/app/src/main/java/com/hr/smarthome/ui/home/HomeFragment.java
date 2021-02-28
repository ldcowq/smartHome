package com.hr.smarthome.ui.home;

import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.alibaba.fastjson.JSONObject;
import com.hr.smarthome.MyApplication;
import com.hr.smarthome.R;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class HomeFragment extends Fragment {

    private TextView temp_num_tv;
    private TextView humi_num_tv;
    private TextView mq2_num_tv;
    private TextView light_num_tv;

    private Switch getdata_swith;
    private MqttAsyncClient mqttClient;
    private MqttConnectOptions connectOptions;

    String topic = "smartHome/ldc";

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);
        temp_num_tv = view.findViewById(R.id.temp_num_show_tv);
        humi_num_tv = view.findViewById(R.id.humi_num_show_tv);
        mq2_num_tv = view.findViewById(R.id.mq2_num_show_tv);
        light_num_tv = view.findViewById(R.id.light_num_show_tv);
        getdata_swith = view.findViewById(R.id.getdata_home);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        getdata_swith.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mqttInit();
                    Mqtt_connect();
                    try {
                        while (!mqttClient.isConnected()) ;
                        if (mqttClient != null && mqttClient.isConnected()) {
                            mqttClient.subscribe(topic, 0);
                            //System.out.println("订阅成功");
                            //Toast.makeText(MyApplication.getContext(), "连接并订阅成功！", Toast.LENGTH_SHORT).show();
                        } else {
                            System.out.println("主题订阅失败！");
                            System.out.println(mqttClient.toString());
                            System.out.println(mqttClient.isConnected());
                        }
                    } catch (MqttException e) {
                        e.printStackTrace();
                        getdata_swith.setChecked(false);
                        //Toast.makeText(MyApplication.getContext(), "主题订阅失败！", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    if (mqttClient.isConnected() && mqttClient != null) {
                        try {
                            mqttClient.unsubscribe(topic);
                            disconnect();

                        } catch (MqttException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
    }

    private void mqttInit() {
        try {
            //host为服务器主机名，ClientId_editText获取客户端的id，MemoryPersistence设置客户端id的保存形式，默认为内存保存
            String serverURI = "tcp://103.152.132.235:1883";
            Toast.makeText(MyApplication.getContext(), serverURI, Toast.LENGTH_SHORT).show();

            mqttClient = new MqttAsyncClient(serverURI, "hostPhone", new MemoryPersistence());
            //Mqtt的连接配置
            connectOptions = new MqttConnectOptions();
            //设置是否清空session，这里如果设置force，表示服务器会保留客户端的连接记录，设置为true表示每次连接到服务器都以新的身份连接.
            connectOptions.setCleanSession(false);
            //设置连接的用户名
            connectOptions.setUserName("host");

            //设置连接的密码
            connectOptions.setPassword("root".toCharArray());

            //设置超时时间，单位为秒
            connectOptions.setConnectionTimeout(10);

            //设置会话心跳时间,单位为秒,服务器会每隔1.5×20秒的时间向客户端发送一个消息，判断客户端是否在线，但这个方法没有重连的机制.
            connectOptions.setKeepAliveInterval(20);

            //设置回调函数
            mqttClient.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                    //连接丢失后，一般在这里进行重连.
                    //Toast.makeText(MainActivity.this, "连接已断开！", Toast.LENGTH_SHORT).show();
                    //startReconnect();
                    System.out.println("mqtt连接断开");
                }

                @Override
                public void messageArrived(String topicName, MqttMessage message) throws Exception {
                    //订阅后得到的消息会执行到这里
                    System.out.println(message);
                    final JSONObject jsonObject = (JSONObject) JSONObject.parse(String.valueOf(message));
                    String t = (String) jsonObject.get("t");
                    System.out.println(t);
                    switch (t) {
                        case "th":
                            requireActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    temp_num_tv.setText((String) jsonObject.get("temp")+" ℃");
                                    humi_num_tv.setText((String) jsonObject.get("humi")+" %");
                                }
                            });
                            break;
                        case "mq2":
                            requireActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mq2_num_tv.setText((String) jsonObject.get("mq2")+" %");//不能在mqttClient.setCallback中修改ui，需要回到主线程修改
                                }
                            });
                            break;
                        case "light":
                            requireActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    light_num_tv.setText((String) jsonObject.get("light")+" %");
                                }
                            });
                            break;
                    }
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                    //推送消息后会执行到这个函数
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void disconnect() {
        if ( mqttClient != null) {
            try {
                Toast.makeText(MyApplication.getContext(), "断开成功！", Toast.LENGTH_SHORT).show();
                mqttClient.disconnect();
                getdata_swith.setChecked(false);

            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        disconnect();
    }

    private void Mqtt_connect() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    //mqttClient.disconnect();
                    if (!(mqttClient.isConnected())) {//如果还没有连接
                        IMqttToken token = mqttClient.connect(connectOptions);
                        token.waitForCompletion();
                        System.out.println("连接成功");
                        //System.out.println(mqttClient.isConnected());
                        //Toast.makeText(MyApplication.getContext(), "连接成功！", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    //System.out.println("连接失败！");
                }
            }
        }).start();
    }

}