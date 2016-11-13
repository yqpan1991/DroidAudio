package droidaudio.apollo.edus.com.droidaudio;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.bt_media).setOnClickListener(this);
        findViewById(R.id.bt_audio).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.bt_media:
                startActivity(new Intent(this, MediaActivity.class));
                break;
            case R.id.bt_audio:
                startActivity(new Intent(this, AudioActivity.class));
                break;
        }
    }
}
