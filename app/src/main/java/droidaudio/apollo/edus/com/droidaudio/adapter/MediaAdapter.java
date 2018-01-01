package droidaudio.apollo.edus.com.droidaudio.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import droidaudio.apollo.edus.com.droidaudio.MediaInfo;
import droidaudio.apollo.edus.com.droidaudio.R;

/**
 * Created by panda on 2018/1/1.
 */

public class MediaAdapter extends CommonBaseAdapter<MediaInfo> {

    private int mSelectedPos;

    public MediaAdapter(Context context) {
        super(context);
        mSelectedPos = -1;
    }

    public void setSelectedPos(int pos){
        if(pos != mSelectedPos){
            mSelectedPos = pos;
            notifyDataSetChanged();
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        if(convertView == null){
            convertView = mInflater.inflate(R.layout.media_item, parent, false);
            viewHolder = new ViewHolder();
            convertView.setTag(viewHolder);
            viewHolder.tvContent = (TextView) convertView.findViewById(R.id.tv_path);
        }else{
            viewHolder = (ViewHolder) convertView.getTag();
        }
        MediaInfo item = getItem(position);
        if(item == null){
            viewHolder.tvContent.setText(null);
        }else{
            viewHolder.tvContent.setText(item.filePath);
        }
        if(mSelectedPos == position){
            convertView.setBackgroundColor(mContext.getResources().getColor(R.color.color_play_selected));
        }else{
            convertView.setBackgroundColor(mContext.getResources().getColor(R.color.color_play_normal));
        }

        return convertView;
    }

    public static class ViewHolder{
        TextView tvContent;
    }
}
