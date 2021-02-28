package mqttclient.UiFragment.SubscribeFragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;


import com.hr.smarthome.R;

import org.eclipse.paho.client.mqttv3.MqttException;

import static mqttclient.SecondActivity.mqttClient;


public class SubscribeFragment extends Fragment {

    private View rootView;
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private Switch subscribe_switch;
    private RadioGroup subscribe_QosRadioGroup;
    private RadioButton subscribe_qos;
    private Button clear_subscribe_btn;
    private TextView subscribe_status_show_textView;
    private EditText subscribe_topic_editText;
    public static TextView receiveMsg_text;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.subscribe, container, false);
        initView_subscribe();
        return rootView;
    }

    private void initView_subscribe() {
        subscribe_switch = rootView.findViewById(R.id.subscribe_switch);
        subscribe_QosRadioGroup = rootView.findViewById(R.id.subscribe_QosRadioGroup);
        clear_subscribe_btn = rootView.findViewById(R.id.clear_subscribe_btn);
        subscribe_status_show_textView = rootView.findViewById(R.id.subscribe_status_show_textView);
        subscribe_topic_editText = rootView.findViewById(R.id.subscribe_topic_editText);
        receiveMsg_text = rootView.findViewById(R.id.receiveMsg_text);
        subscribe_qos = rootView.findViewById(R.id.qos_0);
    }

    public void setSubscribe_status_show_textView(String s) {
        subscribe_status_show_textView.setText(s);
    }

    public void setSubscribe_switch(boolean b) {
        subscribe_switch.setChecked(b);
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d("subscribe", "onStop:---------------- ");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d("subscribe", "onDestroy---------------- ");
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d("subscribe", "onStart:---------------- ");

        clear_subscribe_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                receiveMsg_text.setText("");
            }
        });

        subscribe_QosRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                subscribe_qos = rootView.findViewById(checkedId);
            }
        });

        subscribe_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    if (mqttClient != null && mqttClient.isConnected()) {
                        try {
                            if (!TextUtils.isEmpty(subscribe_topic_editText.getText().toString().trim())) {
                                mqttClient.subscribe(subscribe_topic_editText.getText().toString().trim(),
                                        Integer.parseInt(subscribe_qos.getText().toString()));
                                subscribe_status_show_textView.setText("订阅主题“" + subscribe_topic_editText.getText().toString() + "”" + "成功");
                            } else {
                                Toast.makeText(getActivity(), "主题未填写！", Toast.LENGTH_SHORT).show();
                                subscribe_switch.setChecked(false);
                            }
                        } catch (MqttException e) {
                            e.printStackTrace();
                        }
                    } else {
                        Toast.makeText(getActivity(), "未连接！", Toast.LENGTH_SHORT).show();
                        subscribe_switch.setChecked(false);
                    }
                } else {
                    try {
                        mqttClient.unsubscribe(subscribe_topic_editText.getText().toString().trim());
                        subscribe_status_show_textView.setText("断开连接！");
                    } catch (MqttException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

    }
}
