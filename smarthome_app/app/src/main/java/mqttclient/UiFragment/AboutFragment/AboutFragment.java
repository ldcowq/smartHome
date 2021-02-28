package mqttclient.UiFragment.AboutFragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.hr.smarthome.R;


public class AboutFragment extends Fragment {

    private TextView developerName_textview;
    private TextView email;


    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.about, container, false);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

    }
}
