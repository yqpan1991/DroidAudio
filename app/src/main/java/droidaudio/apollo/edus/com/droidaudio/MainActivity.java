package droidaudio.apollo.edus.com.droidaudio;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.edus.apollo.opuslib.OpusTool;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private final String TAG = this.getClass().getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.bt_media).setOnClickListener(this);
        findViewById(R.id.bt_pcm).setOnClickListener(this);
        findViewById(R.id.bt_opus).setOnClickListener(this);
        findViewById(R.id.bt_media_by_manager).setOnClickListener(this);
        findViewById(R.id.bt_pcm_by_manager).setOnClickListener(this);
        findViewById(R.id.bt_opus_by_manager).setOnClickListener(this);
        findViewById(R.id.bt_media_list).setOnClickListener(this);
        Toast.makeText(this, "isSucceed?" + OpusTool.isLoadSucceed(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_media:
                startActivity(new Intent(this, MediaActivity.class));
                break;
            case R.id.bt_pcm:
                startActivity(new Intent(this, PcmActivity.class));
                break;
            case R.id.bt_opus:
                startActivity(new Intent(this, OpusActivity.class));
                break;
            case R.id.bt_media_by_manager:
                startActivity(new Intent(this, MediaByManagerActivity.class));
                break;
            case R.id.bt_pcm_by_manager:
                startActivity(new Intent(this, PcmByManagerActivity.class));
                break;
            case R.id.bt_opus_by_manager:
                startActivity(new Intent(this, OpusByManagerActivity.class));
                break;
            case R.id.bt_media_list:
                startActivity(new Intent(this, MediaListActivity.class));
                break;
        }
    }

}
