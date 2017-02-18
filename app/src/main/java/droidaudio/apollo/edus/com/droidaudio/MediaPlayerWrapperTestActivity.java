package droidaudio.apollo.edus.com.droidaudio;

import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import droidaudio.apollo.edus.com.droidaudio.media.IPlayerListener;
import droidaudio.apollo.edus.com.droidaudio.media.audio.StatedMediaPlayer;

/**
 * 学习使用系统Media相关的使用
 */
public class MediaPlayerWrapperTestActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = MediaPlayerWrapperTestActivity.class.getSimpleName();

    private List<MediaInfo> mMediaList = new ArrayList<>();
    private ListView mLvContent;
    private MediaAdapter mAdapter;
    private StatedMediaPlayer mMediaPlayerWrapper;

    private Button mBtPlay;
    private Button mBtPause;
    private Button mBtResume;
    private Button mBtStopPlay;
    private MediaInfo mMediaInfo;
    private SeekBar mSbSeek;

    private TextView mTvTitle;
    private TextView mTvCurrentPosition;
    private TextView mTvTotalDuration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_test);
        mLvContent = (ListView) findViewById(R.id.lv_content);

        mBtPlay = (Button) findViewById(R.id.bt_play);
        mBtPlay.setOnClickListener(this);
        mBtPause = (Button) findViewById(R.id.bt_pause);
        mBtPause.setOnClickListener(this);
        mBtResume = (Button) findViewById(R.id.bt_resume);
        mBtResume.setOnClickListener(this);
        mBtStopPlay = (Button) findViewById(R.id.bt_stopPlay);
        mBtStopPlay.setOnClickListener(this);
        mSbSeek = (SeekBar) findViewById(R.id.sb_seek);
        mTvTitle = (TextView) findViewById(R.id.tv_playing_title);
        mTvCurrentPosition = (TextView) findViewById(R.id.tv_current_progress);
        mTvTotalDuration = (TextView) findViewById(R.id.tv_duration);

        mSbSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(fromUser){
                    if(mMediaInfo != null && mMediaPlayerWrapper != null && !TextUtils.isEmpty(mMediaPlayerWrapper.getPlayPath())){
                        mMediaPlayerWrapper.seekTo(progress);
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        initData();
    }

    private void initData() {
        //load data
        mMediaList = getMediaList();
        if(mMediaList != null){
            Log.e(TAG, "mediaList:"+mMediaList);
        }else{
            Log.e(TAG, "no media");
        }
        mAdapter = new MediaAdapter();
        mLvContent.setAdapter(mAdapter);
        mLvContent.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                handleClickItem(mMediaList.get(position));
            }
        });
    }

    private void handleClickItem(MediaInfo mediaInfo) {
        if(mMediaPlayerWrapper == null){
            mMediaPlayerWrapper = new StatedMediaPlayer();
            mMediaPlayerWrapper.setPlayerListener(new IPlayerListener() {
                @Override
                public void onPreparing(String filePath) {
                    Log.e(TAG, "onPreparing, filePath:"+filePath);
                    mTvTitle.setText(filePath);
                }

                @Override
                public void onPrepared(String filePath) {
                    Log.e(TAG, "onPrepared, filePath:"+filePath);
                }

                @Override
                public void onPause(String filePath) {
                    Log.e(TAG, "onPause, filePath:"+filePath);
                }

                @Override
                public void onPlay(String filePath) {
                    Log.e(TAG, "onPlay, filePath:"+filePath);
                }

                @Override
                public void onError(String filePath, int what, int extra) {
                    Log.e(TAG, "onError, filePath:"+filePath+"what:"+what+",extra:"+extra);
                    mMediaInfo = null;
                    clearPlayUI();
                }

                @Override
                public void onStopped(String filePath) {
                    Log.e(TAG, "onStopped, filePath:"+filePath);
                    clearPlayUI();
                }

                @Override
                public void onComplete(String filePath) {
                    Log.e(TAG, "onComplete, filePath:"+filePath);
                    mMediaInfo = null;
                    clearPlayUI();
                }

                private void clearPlayUI() {
                    mTvTitle.setText(null);
                    mTvCurrentPosition.setText(null);
                    mTvTotalDuration.setText(null);
                    mSbSeek.setProgress(0);
                    mSbSeek.setMax(100);
                }

                @Override
                public void onProgressChanged(String filePath, int curPosition, int duration) {
                    Log.e(TAG, "onProgressChanged, filePath:"+filePath+",curPosition:"+curPosition+",duration:"+duration);
                    mSbSeek.setMax(duration);
                    mSbSeek.setProgress(curPosition);
                    mTvCurrentPosition.setText(String.valueOf(curPosition));
                    mTvTotalDuration.setText(String.valueOf(duration));
                }
            });
        }
        mMediaInfo = mediaInfo;
        mMediaPlayerWrapper.start(mMediaInfo.path);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.bt_play:
                if(mMediaInfo != null){
                    mMediaPlayerWrapper.start(mMediaInfo.path);
                }
                break;
            case R.id.bt_pause:
                if(!TextUtils.isEmpty(mMediaPlayerWrapper.getPlayPath())){
                    if(mMediaPlayerWrapper.isPlaying()){
                        mMediaPlayerWrapper.pause();
                    }
                }
                break;
            case R.id.bt_resume:
                if(!TextUtils.isEmpty(mMediaPlayerWrapper.getPlayPath())){
                    if(!mMediaPlayerWrapper.isPlaying()){
                        mMediaPlayerWrapper.resume();
                    }
                }
                break;
            case R.id.bt_stopPlay:
                if(!TextUtils.isEmpty(mMediaPlayerWrapper.getPlayPath())){
                    mMediaPlayerWrapper.stop();
                }
                break;
        }

    }


    private List<MediaInfo> getMediaList(){
        Cursor cursor = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null,
                null, null, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
        if(cursor != null){
            List<MediaInfo> list = new ArrayList<>();
            try{
                if(cursor.getCount() > 0){
                    while(cursor.moveToNext()){
                        String path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
                        String title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
                        if(!TextUtils.isEmpty(path)){
                            MediaInfo info = new MediaInfo();
                            info.path = path;
                            info.title = title;
                            list.add(info);
                        }
                    }
                }
            }catch (Exception ex){

            }finally {
                cursor.close();
            }
            return list;
        }
        return null;
    }


    private class MediaInfo{
        String path;
        String title;

        @Override
        public String toString() {
            return "MediaInfo{" +
                    "path='" + path + '\'' +
                    ", title='" + title + '\'' +
                    '}';
        }
    }

    private class MediaAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return mMediaList.size();
        }

        @Override
        public MediaInfo getItem(int position) {
            return mMediaList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder = null;
            if(convertView == null){
                convertView = getLayoutInflater().inflate(R.layout.media_item, parent, false);
                viewHolder = new ViewHolder();
                convertView.setTag(viewHolder);
                viewHolder.tvPath = (TextView) convertView.findViewById(R.id.tv_path);
                viewHolder.tvTitle = (TextView) convertView.findViewById(R.id.tv_title);
            }else{
                viewHolder = (ViewHolder) convertView.getTag();
            }
            MediaInfo item = getItem(position);
            viewHolder.tvPath.setText(item.path);
            viewHolder.tvTitle.setText(item.title);
            return convertView;
        }
    }

    private class ViewHolder{
        TextView tvTitle;
        TextView tvPath;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mMediaPlayerWrapper != null){
            if(!TextUtils.isEmpty(mMediaPlayerWrapper.getPlayPath())){
                mMediaPlayerWrapper.stop();
            }
            mMediaPlayerWrapper = null;
        }
    }
}
