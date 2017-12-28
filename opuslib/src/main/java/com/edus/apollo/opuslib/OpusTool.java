package com.edus.apollo.opuslib;

import java.nio.ByteBuffer;

/**
 * Created by panda on 2017/12/27.
 */

public class OpusTool {
    private static boolean mLoadSucceed;
    static {
        try{
            System.loadLibrary("opustool");
            mLoadSucceed = true;
        }catch (Throwable ex){
            ex.printStackTrace();
            mLoadSucceed = false;
        }
    }
    private native int startRecord(String path);
    private native int writeFrame(ByteBuffer frame, int len);
    private native void stopRecord();
    private native int openOpusFile(String path);
    private native int seekOpusFile(float position);
    private native int isOpusFile(String path);
    private native void closeOpusFile();
    private native void readOpusFile(ByteBuffer buffer, int capacity, int[] args);
    private native long getTotalPcmDuration();
    public native byte[] getWaveform(String path);
    public native byte[] getWaveform2(short[] array, int length);

    public static boolean isLoadSucceed(){
        return mLoadSucceed;
    }
}
