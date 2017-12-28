package droidaudio.apollo.edus.com.droidaudio;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.edus.apollo.opuslib.OpusTool;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    private final String TAG = this.getClass().getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.bt_media_v2).setOnClickListener(this);
        findViewById(R.id.bt_pcm).setOnClickListener(this);
        Toast.makeText(this, "isSucceed?"+ OpusTool.isLoadSucceed(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.bt_media_v2:
                startActivity(new Intent(this, MediaV2Activity.class));
                break;
            case R.id.bt_pcm:
                startActivity(new Intent(this, PcmActivity.class));
                break;
        }
    }

}
