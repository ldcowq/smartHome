package mqttclient.UiFragment.PublishFragment;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;


import com.hr.smarthome.R;

import org.eclipse.paho.client.mqttv3.MqttException;

import static mqttclient.SecondActivity.mqttClient;


public class PublishFragment extends Fragment {

    private EditText publish_topic_editText;
    private EditText publishMsg_editText;
    private RadioGroup publish_QosRadioGroup;
    private TextView publish_status_show_textView;
    private RadioButton publish_qos;
    private Button publish_message_btn;
    private View rootView;
    int count;
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.publish, container, false);
        initView();
        return rootView;
    }

    private void initView() {
        publish_topic_editText = rootView.findViewById(R.id.publish_topic_editText);
        publishMsg_editText = rootView.findViewById(R.id.publishMsg_editText);
        publish_status_show_textView = rootView.findViewById(R.id.publish_status_show_textView);
        publish_QosRadioGroup = rootView.findViewById(R.id.publish_QosRadioGroup);
        publish_qos = rootView.findViewById(R.id.qos_0);
        publish_message_btn = rootView.findViewById(R.id.publish_message_btn);

    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d("publish", "onStop:---------------- ");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d("publish", "onDestroy---------------- ");
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d("publish", "onStart:---------------- ");
        publish_QosRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                publish_qos = rootView.findViewById(checkedId);
            }
        });

        publish_message_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.getId() == R.id.publish_message_btn) {
                    try {
                        if (mqttClient!=null&&mqttClient.isConnected()) {
                            if (!TextUtils.isEmpty(publish_topic_editText.getText().toString().trim())) {
                                byte[] publishMessage = publishMsg_editText.getText().toString().trim().getBytes();
                                mqttClient.publish(publish_topic_editText.getText().toString().trim(),
                                        publishMessage, Integer.parseInt(publish_qos.getText().toString()), false);
                                publish_status_show_textView.setText("累计推送消息：" + ++count + " 条");
                            } else {
                                Toast.makeText(getActivity(), "主题未填写！", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(getActivity(), "未连接！", Toast.LENGTH_SHORT).show();
                            count=0;
                            publish_status_show_textView.setText("未连接!");
                        }
                    } catch (MqttException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    public void setPublish_status_show_textView(String s) {
        publish_status_show_textView.setText(s);
    }
}
