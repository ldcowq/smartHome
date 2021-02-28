package mqttclient;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.hr.smarthome.R;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import mqttclient.UiFragment.AboutFragment.AboutFragment;
import mqttclient.UiFragment.ConnectFragment.ConnectFragment;
import mqttclient.UiFragment.PublishFragment.PublishFragment;
import mqttclient.UiFragment.SubscribeFragment.SubscribeFragment;


public class SecondActivity extends AppCompatActivity {
    private static final int STRATCHECK = 1;
    private static final int BACK = 2;
    private static final int RECEIVERBACK = 3;
    private static final int CONNECTFAILED = 30;
    private static final int CONNECTSUCCEED = 31;

    private ScheduledExecutorService scheduler;
    private MqttConnectOptions connectOptions;
    public static MqttClient mqttClient;
    private BottomNavigationView bottomNavigationView;
    private ConnectFragment connectFragment;
    private SubscribeFragment subscribeFragment;
    private PublishFragment publishFragment;
    private AboutFragment aboutFragment;
    private Fragment[] fragments;
    private int lastfragment;//用于记录上个选择的Fragment
    public static Handler handler;
    private Switch connect_switch;
    private EditText port_editText;
    private EditText host_editText;
    private EditText ClientId_editText;
    private EditText username_editText;
    private EditText password_editText;
    private TextView connect_status_show_textView;
    FragmentTransaction transaction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);
        initFragment();
    }

    @Override
    @SuppressLint("HandlerLeak")
    protected void onStart() {
        super.onStart();
        Log.d("MainActivity", "onStart:---------------- ");

        connect_switch = connectFragment.getActivity().findViewById(R.id.connect_switch);
        connect_status_show_textView = connectFragment.getActivity().findViewById(R.id.connect_status_show_textView);
        host_editText = connectFragment.getActivity().findViewById(R.id.host_editText);
        port_editText = connectFragment.getActivity().findViewById(R.id.port_editText);
        ClientId_editText = connectFragment.getActivity().findViewById(R.id.ClientId_editText);
        username_editText = connectFragment.getActivity().findViewById(R.id.username_editText);
        password_editText = connectFragment.getActivity().findViewById(R.id.password_editText);

        handler = new Handler() {
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case STRATCHECK://开机校验更新回传
                        break;
                    case BACK://反馈回传
                        Toast.makeText(SecondActivity.this, "反馈回传", Toast.LENGTH_SHORT).show();
                        break;
                    case RECEIVERBACK:  //MQTT收到消息回传:
                        String message = msg.obj.toString();
                        String topicName = message.substring(0, message.indexOf(","));
                        String topicContent = message.substring(message.indexOf(",") + 1, message.length());
                        SubscribeFragment.receiveMsg_text.append(" 收到“" + topicName + "”主题发来消息：" + topicContent + "\r\n");
                        break;
                    case CONNECTFAILED:  //连接失败
                        Toast.makeText(SecondActivity.this, "连接失败", Toast.LENGTH_SHORT).show();
                        connect_status_show_textView.setText("未连接！");
                        break;
                    case CONNECTSUCCEED: //连接成功
                        Toast.makeText(SecondActivity.this, "连接成功", Toast.LENGTH_SHORT).show();
                        connect_status_show_textView.setText("已连接！");
                        publishFragment.setPublish_status_show_textView("已连接！");
                        subscribeFragment.setSubscribe_status_show_textView("已连接！");
                        break;
                    default:
                        break;
                }
            }
        };

        connect_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    if (!TextUtils.isEmpty(host_editText.getText().toString().trim())) {
                        if (TextUtils.isEmpty(port_editText.getText().toString().trim())) {
                            port_editText.setText("1883");
                        }
                        if (TextUtils.isEmpty(ClientId_editText.getText().toString().trim())) {
                            Random random = new Random(System.currentTimeMillis());
                            String id = "" + random.nextInt(100000);
                            ClientId_editText.setText(id);
                        }
                        mqttInit();
                        Mqtt_connect();
                    } else {
                        Toast.makeText(SecondActivity.this, "服务器地址不能为空!", Toast.LENGTH_SHORT).show();
                        connect_switch.setChecked(false);
                    }
                } else {
                    disconnect();
                }
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d("MainActivity", "onStop:---------------- ");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeMessages(CONNECTSUCCEED);
        handler.removeMessages(RECEIVERBACK);

        Log.d("MainActivity", "onDestroy:---------------- ");
    }

    private void startReconnect() {
        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                if (!mqttClient.isConnected()) {
                    Mqtt_connect();
                }
            }
        }, 0, 10 * 1000, TimeUnit.MILLISECONDS);
    }

    private void Mqtt_connect() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (!(mqttClient.isConnected())) {//如果还没有连接
                        mqttClient.connect(connectOptions);
                        Message msg = new Message();
                        msg.what = 31;
                        SecondActivity.handler.sendMessage(msg);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Message msg = new Message();
                    msg.what = 30;
                    SecondActivity.handler.sendMessage(msg);
                }
            }
        }).start();
    }

    private void disconnect() {
        if (mqttClient.isConnected() && mqttClient != null) {
            try {
                mqttClient.disconnect();
                connect_status_show_textView.setText("未连接！");
                subscribeFragment.setSubscribe_status_show_textView("未连接！");
                subscribeFragment.setSubscribe_switch(false);
                publishFragment.setPublish_status_show_textView("未连接！");
                Toast.makeText(SecondActivity.this, "连接已断开！", Toast.LENGTH_SHORT).show();
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
    }

    private void mqttInit() {
        try {
            //host为服务器主机名，ClientId_editText获取客户端的id，MemoryPersistence设置客户端id的保存形式，默认为内存保存
            String serverURI = "tcp://" + host_editText.getText().toString().trim() + ":" + port_editText.getText().toString().trim();
            Log.d("publish", "mqttURL: " + serverURI);
            mqttClient = new MqttClient(serverURI, ClientId_editText.getText().toString().trim(), new MemoryPersistence());
            //Mqtt的连接配置
            connectOptions = new MqttConnectOptions();

            //设置是否清空session，这里如果设置force，表示服务器会保留客户端的连接记录，设置为true表示每次连接到服务器都以新的身份连接.
            connectOptions.setCleanSession(false);

            //设置连接的用户名
            connectOptions.setUserName(username_editText.getText().toString().trim());

            //设置连接的密码
            connectOptions.setPassword(password_editText.getText().toString().trim().toCharArray());

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
                }

                @Override
                public void messageArrived(String topicName, MqttMessage message) throws Exception {
                    //订阅后得到的消息会执行到这里
                    Message msg = new Message();
                    msg.what = 3;//收到消息标志位
                    msg.obj = topicName + "," + message.toString();
                    SecondActivity.handler.sendMessage(msg);//回传
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

    /**
     * 初始化四个导航栏的fragment
     */
    private void initFragment() {
        connectFragment = new ConnectFragment();
        subscribeFragment = new SubscribeFragment();
        publishFragment = new PublishFragment();
        aboutFragment = new AboutFragment();
        fragments = new Fragment[]{connectFragment, subscribeFragment, publishFragment, aboutFragment};
        lastfragment = 0;
        transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_main, connectFragment).commit();
        bottomNavigationView = findViewById(R.id.nav_view);
        bottomNavigationView.setOnNavigationItemSelectedListener(changeFragment);
        transaction.add(R.id.fragment_main, subscribeFragment);
        transaction.add(R.id.fragment_main, publishFragment);
        transaction.add(R.id.fragment_main, aboutFragment);
        transaction.hide(connectFragment);
        transaction.show(subscribeFragment);
        transaction.hide(subscribeFragment);
        transaction.show(publishFragment);
        transaction.hide(publishFragment);
        transaction.show(aboutFragment);
        transaction.hide(aboutFragment);
        transaction.show(connectFragment);
    }


    /**
     * 设置底部导航栏监听，避免重复创建相同的fragment
     */
    private BottomNavigationView.OnNavigationItemSelectedListener changeFragment = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_connect: {
                    if (lastfragment != 0) {
                        switchFragment(lastfragment, 0);
                        lastfragment = 0;
                    }
                    return true;
                }
                case R.id.navigation_subscribe: {
                    if (lastfragment != 1) {
                        switchFragment(lastfragment, 1);
                        lastfragment = 1;
                    }

                    return true;
                }

                case R.id.navigation_publish: {
                    if (lastfragment != 2) {
                        switchFragment(lastfragment, 2);
                        lastfragment = 2;
                    }

                    return true;
                }
                case R.id.navigation_about: {
                    if (lastfragment != 3) {
                        switchFragment(lastfragment, 3);
                        lastfragment = 3;
                    }
                    return true;
                }

            }
            return false;
        }
    };


    /**
     * 切换底部导航栏的fragment
     *
     * @param lastfragment 上一个fregment
     * @param index        需要跳转的fragment索引
     */
    private void switchFragment(int lastfragment, int index) {
        transaction = getSupportFragmentManager().beginTransaction();
        transaction.hide(fragments[lastfragment]);
        if (!fragments[index].isAdded()) {
            transaction.add(R.id.fragment_main, fragments[index]);
        }
        transaction.show(fragments[index]).commitAllowingStateLoss();
    }

}