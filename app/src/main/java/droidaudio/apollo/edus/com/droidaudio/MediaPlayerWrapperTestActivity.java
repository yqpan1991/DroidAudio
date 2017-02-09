package droidaudio.apollo.edus.com.droidaudio;

import android.database.Cursor;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import droidaudio.apollo.edus.com.droidaudio.media.IPlayerListener;
import droidaudio.apollo.edus.com.droidaudio.media.IRecorderListener;
import droidaudio.apollo.edus.com.droidaudio.media.MediaController;
import droidaudio.apollo.edus.com.droidaudio.media.audio.MediaPlayerWrapper;

/**
 * 学习使用系统Media相关的使用
 */
public class MediaPlayerWrapperTestActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = MediaPlayerWrapperTestActivity.class.getSimpleName();

    private List<MediaInfo> mMediaList = new ArrayList<>();
    private ListView mLvContent;
    private MediaAdapter mAdapter;
    private MediaPlayerWrapper mMediaPlayerWrapper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_test);
        mLvContent = (ListView) findViewById(R.id.lv_content);
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
            mMediaPlayerWrapper = new MediaPlayerWrapper();
            mMediaPlayerWrapper.setPlayerListener(new IPlayerListener() {
                @Override
                public void onPreparing(String filePath) {
                    Log.e(TAG, "onPreparing, filePath:"+filePath);
                }

                @Override
                public void onPrepared(String filePath) {
                    Log.e(TAG, "onPrepared, filePath:"+filePath);
                }

                @Override
                public void onStart(String filePath) {
                    Log.e(TAG, "onStart, filePath:"+filePath);
                }

                @Override
                public void onPause(String filePath) {
                    Log.e(TAG, "onPause, filePath:"+filePath);
                }

                @Override
                public void onResume(String filePath) {
                    Log.e(TAG, "onResume, filePath:"+filePath);
                }

                @Override
                public void onError(String filePath, int what, int extra) {
                    Log.e(TAG, "onError, filePath:"+filePath+"what:"+what+",extra:"+extra);
                }

                @Override
                public void onStopped(String filePath) {
                    Log.e(TAG, "onStopped, filePath:"+filePath);
                }

                @Override
                public void onComplete(String filePath) {
                    Log.e(TAG, "onComplete, filePath:"+filePath);
                }

                @Override
                public void onProgressChanged(String filePath, int curPosition, int duration) {
                    Log.e(TAG, "onProgressChanged, filePath:"+filePath+",curPosition:"+curPosition+",duration:"+duration);
                }
            });
        }
        mMediaPlayerWrapper.start(mediaInfo.path);
    }

    @Override
    public void onClick(View v) {

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
}
