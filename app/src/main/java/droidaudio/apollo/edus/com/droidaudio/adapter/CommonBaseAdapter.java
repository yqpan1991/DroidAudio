package droidaudio.apollo.edus.com.droidaudio.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by panda on 2018/1/1.
 */

public abstract class CommonBaseAdapter<T> extends BaseAdapter {
    protected LayoutInflater mInflater;
    protected Context mContext;
    private List<T> mDataList;

    public CommonBaseAdapter(Context context){
        if(context == null){
            throw new RuntimeException("CommonBaseAdapter params cannot be null");
        }
        mContext = context;
        mInflater = LayoutInflater.from(mContext);
        mDataList = new ArrayList<>();
    }

    public void setDataList(List<T> dataList){
        mDataList.clear();
        if(dataList != null && !dataList.isEmpty()){
            mDataList.addAll(dataList);
        }
        notifyDataSetChanged();
    }

    public void addDataList(List<T> dataList){
        if(dataList == null || dataList.isEmpty()){
            return;
        }
        mDataList.addAll(dataList);
        notifyDataSetChanged();
    }

    @Override
    public final int getCount() {
        return mDataList.size();
    }

    @Override
    public final T getItem(int position) {
        if(position < 0 || position >= mDataList.size()){
            return null;
        }
        return mDataList.get(position);
    }

    @Override
    public final long getItemId(int position) {
        return position;
    }

}
