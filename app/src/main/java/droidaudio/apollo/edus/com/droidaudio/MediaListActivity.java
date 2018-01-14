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
import droidaudio.apollo.edus.com.droidaudio.multimedia.base.IPlay;

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
    private ImageView mIvPlay;
    private ImageView mIvPause;
    private ImageView mIvNext;
    private MediaManager mMediaManager;
    private MediaInfo mPlayingItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_list);
        initView();
        initData();
        showInitUi();
    }

    private void initData() {
        mMediaManager = MediaManager.getInstance();
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
                mPlayingItem = mMediaAdapter.getItem(position);
                if(mPlayingItem != null){
                    MediaManager.getInstance().play(mPlayingItem.filePath);
                    mMediaAdapter.setSelectedPos(position);
                }
            }
        });
        mMediaManager.addPlayListener(mIPlayerListener);
        showInitUi();
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
        mIvPlay = (ImageView) findViewById(R.id.iv_play);
        mIvPause = (ImageView) findViewById(R.id.iv_pause);
        mIvNext = (ImageView) findViewById(R.id.iv_next);

        mIvNext.setOnClickListener(this);
        mIvPlay.setOnClickListener(this);
        mIvPause.setOnClickListener(this);
        mIvPrevious.setOnClickListener(this);
        mSbProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(!fromUser){
                    return;
                }
                if(mMediaManager.isSupportSeekTo()){
                    int duration = mMediaManager.getDuration();
                    if(duration > 0){
                        mMediaManager.seekTo((int) (progress * 1.0f / 100 * duration));
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                if(mMediaManager.isSupportSeekTo() && mMediaManager.getDuration() > 0){
                    mMediaManager.pause();
                }
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.iv_next){
            handlePlayNext();
        }else if(v.getId() == R.id.iv_previous){
            handlePlayPrevious();
        }else if(v.getId() == R.id.iv_pause){
            handlePause();
        }else if(v.getId() == R.id.iv_play){
            handlePlay();
        }
    }

    private void handlePlay() {
        if(mMediaManager.getState() == IPlay.RUNNING && !mMediaManager.isPlaying()){
            mMediaManager.resume();
        }
    }

    private void handlePause() {
        if(mMediaManager.getState() == IPlay.RUNNING && mMediaManager.isPlaying()){
            mMediaManager.pause();
        }
    }

    private void handlePlayPrevious() {
        if(mMediaAdapter.getCount() <=  0){
            return;
        }
        if(mPlayingItem != null && !TextUtils.isEmpty(mPlayingItem.filePath)){
            int itemPosition = mMediaAdapter.getItemPosition(mPlayingItem);
            int playPos = itemPosition - 1;
            MediaInfo item = mMediaAdapter.getItem(playPos);
            if(item != null && !TextUtils.isEmpty(item.filePath)){
                mPlayingItem = item;
                MediaManager.getInstance().play(mPlayingItem.filePath);
                mMediaAdapter.setSelectedPos(playPos);
                return;
            }
        }
        int playPos = 0;
        MediaInfo item = mMediaAdapter.getItem(playPos);
        if(item != null && !TextUtils.isEmpty(item.filePath)){
            mPlayingItem = item;
            MediaManager.getInstance().play(mPlayingItem.filePath);
            mMediaAdapter.setSelectedPos(playPos);
        }
    }

    private void handlePlayNext() {
        if(mMediaAdapter.getCount() <=  0){
            return;
        }
        if(mPlayingItem != null && !TextUtils.isEmpty(mPlayingItem.filePath)){
            int itemPosition = mMediaAdapter.getItemPosition(mPlayingItem);
            int playPos = itemPosition + 1;
            MediaInfo item = mMediaAdapter.getItem(playPos);
            if(item != null && !TextUtils.isEmpty(item.filePath)){
                mPlayingItem = item;
                MediaManager.getInstance().play(mPlayingItem.filePath);
                mMediaAdapter.setSelectedPos(playPos);
                return;
            }
        }
        int playPos = 0;
        MediaInfo item = mMediaAdapter.getItem(playPos);
        if(item != null && !TextUtils.isEmpty(item.filePath)){
            mPlayingItem = item;
            MediaManager.getInstance().play(mPlayingItem.filePath);
            mMediaAdapter.setSelectedPos(playPos);
        }
    }

    private IPlayNotifyListener mIPlayerListener = new IPlayNotifyListener() {
        @Override
        public void notifyOnProgressChanged(String filePath, long progress, long duration) {
            logInfo("progress info: filePath:"+filePath+",progress: "+progress+",duration:"+duration);
            //更新进度
            mTvCurPos.setText(String.valueOf(progress/1000));
            mTvDuration.setText(String.valueOf(mMediaManager.getDuration()/1000));
            if(duration <= 0 || progress > duration){
                mSbProgress.setProgress(0);
                mSbProgress.setMax(0);
            }else{
                float displayProgress = progress * 1.0f / duration * 100;
                mSbProgress.setProgress((int) displayProgress);
                mSbProgress.setMax(100);
            }
        }

        @Override
        public void onPreparing(String filePath) {
            logInfo("onPreparing:"+filePath);
            mTvFileName.setText(filePath);
            //显示为加载中
            mTvCurPos.setText("unknown");
            mTvDuration.setText("unknown");
            mSbProgress.setEnabled(false);
            mSbProgress.setClickable(false);
            showPlay(false);
            showPause(false);
        }

        @Override
        public void onPrepared(String filePath) {
            logInfo("onPrepared:"+filePath);
            mTvFileName.setText(filePath);
            mTvCurPos.setText("0");
            mTvDuration.setText(String.valueOf(mMediaManager.getDuration()));
            if(mMediaManager.isSupportSeekTo()){
                mSbProgress.setClickable(true);
                mSbProgress.setEnabled(true);
            }else{
                mSbProgress.setClickable(true);
                mSbProgress.setEnabled(true);
            }
        }

        @Override
        public void onPause(String filePath) {
            logInfo("onPause:"+filePath);
            showPlay(true);
            showPause(false);
        }

        @Override
        public void onPlay(String filePath) {
            logInfo("onPlay:"+filePath);
            showPlay(false);
            showPause(true);
        }

        @Override
        public void onError(String filePath, int what, int extra) {
            logInfo("onError:"+filePath);
            handlePlayDone(true);
        }

        @Override
        public void onStopped(String filePath) {
            logInfo("onStopped:"+filePath);
            handlePlayDone(false);
        }

        @Override
        public void onComplete(String filePath) {
            logInfo("onComplete:"+filePath);
            handlePlayDone(true);
        }
    };

    private void showPlay(boolean visible) {
        mIvPlay.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
        mIvPlay.setClickable(visible);
    }

    private void showPause(boolean visible){
        mIvPause.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
        mIvPause.setClickable(visible);
    }

    private void handlePlayDone(boolean playNext) {
        showInitUi();
        if(playNext){
            handlePlayNext();
        }
    }

    private void showInitUi() {
        mTvFileName.setText(null);
        showPlay(false);
        showPause(false);

        mTvCurPos.setText("unknown");
        mTvDuration.setText("unknown");
        mSbProgress.setEnabled(false);
        mSbProgress.setClickable(false);
        mSbProgress.setMax(0);
        mSbProgress.setProgress(0);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MediaManager.getInstance().stop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMediaManager.removePlayListener(mIPlayerListener);
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
