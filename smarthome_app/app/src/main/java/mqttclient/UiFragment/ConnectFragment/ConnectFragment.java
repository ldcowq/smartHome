package mqttclient.UiFragment.ConnectFragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;


import com.hr.smarthome.R;

import java.util.concurrent.ScheduledExecutorService;


public class ConnectFragment extends Fragment {
    private View rootView;
    private ScheduledExecutorService scheduler;

    @SuppressLint({"UseSwitchCompatOrMaterialCode", "StaticFieldLeak"})
    private   Switch connect_switch;
    private TextView connect_status_show_textView;
    private EditText port_editText;
    private EditText host_editText;
    private EditText ClientId_editText;
    private EditText username_editText;
    private EditText password_editText;
    private Button clear_connect_btn;
    private TextView subscribe_status_show_textView;


    private void initView_connect() {
        host_editText = rootView.findViewById(R.id.host_editText);
        port_editText = rootView.findViewById(R.id.port_editText);
        ClientId_editText = rootView.findViewById(R.id.ClientId_editText);
        username_editText = rootView.findViewById(R.id.username_editText);
        password_editText = rootView.findViewById(R.id.password_editText);
        connect_status_show_textView = rootView.findViewById(R.id.connect_status_show_textView);
        connect_switch = rootView.findViewById(R.id.connect_switch);
        clear_connect_btn = rootView.findViewById(R.id.clear_connect_btn);

    }

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.connect, container, false);//加载布局，以便获取组件id
        initView_connect();
        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        clear_connect_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.getId() == R.id.clear_connect_btn) {
                    host_editText.setText("");
                    port_editText.setText("");
                    ClientId_editText.setText("");
                    username_editText.setText("");
                    password_editText.setText("");
                    host_editText.requestFocus();
                }
            }
        });

    }
}
