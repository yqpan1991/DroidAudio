package droidaudio.apollo.edus.com.droidaudio;

import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.edus.apollo.common.utils.log.LogUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import droidaudio.apollo.edus.com.droidaudio.adapter.MediaAdapter;
import droidaudio.apollo.edus.com.droidaudio.multimedia.IPlayNotifyListener;
import droidaudio.apollo.edus.com.droidaudio.multimedia.MediaManager;
import droidaudio.apollo.edus.com.droidaudio.multimedia.base.IPlayerListener;

public class MediaListActivity extends AppCompatActivity implements View.OnClickListener {
    private final String TAG = "["+this.getClass().getSimpleName()+"]";
    private ListView mLvContent;
    private List<MediaInfo> mFilePathList = new ArrayList<>();
    private MediaAdapter mMediaAdapter;
    private final boolean LOG_ENABLE = true;

    private LinearLayout mRlBottomOperation;
    private TextView mTvFileName;
    private LinearLayout mLlPlayInfo;
    private TextView mTvCurPos;
    private TextView mTvDuration;
    private SeekBar mSbProgress;
    private RelativeLayout mLlPlayOperation;
    private ImageView mIvPrevious;
    private ImageView mIvPlayOrPause;
    private ImageView mIvNext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_list);
        initView();
        initData();
    }

    private void initData() {
        List<String> dirList = new ArrayList<>();
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            dirList.add(getExternalCacheDir().getAbsolutePath());
        }
        dirList.add(getFilesDir().getAbsolutePath());
        for(String filePath: dirList){
            File file = new File(filePath);
            if(file.exists() && file.isDirectory()){
                File[] files = file.listFiles();
                for(File subFile : files){
                    if(subFile.isFile()){
                        MediaInfo mediaInfo = new MediaInfo();
                        mediaInfo.filePath = subFile.getAbsolutePath();
                        mFilePathList.add(mediaInfo);
                    }
                }
            }
        }
        mMediaAdapter = new MediaAdapter(this);
        mMediaAdapter.setDataList(mFilePathList);
        mLvContent.setAdapter(mMediaAdapter);
        mLvContent.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MediaInfo item = mMediaAdapter.getItem(position);
                if(item != null){
                    MediaManager.getInstance().play(item.filePath);
                    mMediaAdapter.setSelectedPos(position);
                }
            }
        });
        MediaManager.getInstance().addPlayListener(mIPlayerListener);

    }

    private void initView() {
        mLvContent = (ListView) findViewById(R.id.lv_content);
        mRlBottomOperation = (LinearLayout) findViewById(R.id.ll_bottom_operation);
        mTvFileName = (TextView) findViewById(R.id.tv_file_name);
        mLlPlayInfo = (LinearLayout) findViewById(R.id.ll_play_info);
        mTvCurPos = (TextView) findViewById(R.id.tv_cur_pos);
        mTvDuration = (TextView) findViewById(R.id.tv_duration);
        mSbProgress = (SeekBar) findViewById(R.id.sb_progress);
        mLlPlayOperation = (RelativeLayout) findViewById(R.id.ll_play_operation);
        mIvPrevious = (ImageView) findViewById(R.id.iv_previous);
        mIvPlayOrPause = (ImageView) findViewById(R.id.iv_play_or_pause);
        mIvNext = (ImageView) findViewById(R.id.iv_next);
    }

    @Override
    public void onClick(View v) {

    }

    private IPlayNotifyListener mIPlayerListener = new IPlayNotifyListener() {
        @Override
        public void notifyOnProgressChanged(String filePath, long progress, long duration) {
            logInfo("progress info: filePath:"+filePath+",progress: "+progress+",duration:"+duration);
        }

        @Override
        public void onPreparing(String filePath) {
            logInfo("onPreparing:"+filePath);
        }

        @Override
        public void onPrepared(String filePath) {
            logInfo("onPrepared:"+filePath);
        }

        @Override
        public void onPause(String filePath) {
            logInfo("onPause:"+filePath);
        }

        @Override
        public void onPlay(String filePath) {
            logInfo("onPlay:"+filePath);
        }

        @Override
        public void onError(String filePath, int what, int extra) {
            logInfo("onError:"+filePath);
        }

        @Override
        public void onStopped(String filePath) {
            logInfo("onStopped:"+filePath);
        }

        @Override
        public void onComplete(String filePath) {
            logInfo("onComplete:"+filePath);
        }
    };

    @Override
    protected void onPause() {
        super.onPause();
        MediaManager.getInstance().stop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MediaManager.getInstance().removePlayListener(mIPlayerListener);
    }

    private void logInfo(String info){
        if(!LOG_ENABLE){
            return;
        }
        if(TextUtils.isEmpty(info)){
            return;
        }
        LogUtils.e(TAG, info);
    }
}
