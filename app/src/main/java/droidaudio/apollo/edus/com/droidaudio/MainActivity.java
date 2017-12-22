package droidaudio.apollo.edus.com.droidaudio;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    private final String TAG = this.getClass().getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.bt_media).setOnClickListener(this);
        findViewById(R.id.bt_media_v2).setOnClickListener(this);
        findViewById(R.id.bt_audio).setOnClickListener(this);
        findViewById(R.id.bt_pcm).setOnClickListener(this);
        findViewById(R.id.bt_media_test).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.bt_media:
                startActivity(new Intent(this, MediaActivity.class));
                break;
            case R.id.bt_media_v2:
                startActivity(new Intent(this, MediaV2Activity.class));
                break;
            case R.id.bt_audio:
                startActivity(new Intent(this, AudioActivity.class));
                break;
            case R.id.bt_pcm:
                startActivity(new Intent(this, PcmV2Activity.class));
                break;
            case R.id.bt_media_test:
                startActivity(new Intent(this, MediaPlayerWrapperTestActivity.class));
                break;
        }
    }

}
