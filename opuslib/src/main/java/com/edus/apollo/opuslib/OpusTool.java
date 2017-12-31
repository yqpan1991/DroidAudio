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

    private static final int START_RECORD_SUC = 1;
    private static final int WRITE_RECORD_FRAME_SUC = 1;

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

    public boolean startOpusRecord(String filePath){
        return startRecord(filePath) == START_RECORD_SUC;
    }

    public boolean writeOpusFrame(ByteBuffer frame, int len){
        return writeFrame(frame, len) == WRITE_RECORD_FRAME_SUC;
    }

    public void stopOpusRecord(){
        stopRecord();
    }

    public int openFile(String path){
        return openOpusFile(path);
    }

    public int seekFile(float position){
        return seekOpusFile(position);
    }

    public int isOpusFileFormat(String path){
        return isOpusFile(path);
    }

    public void closeFile(){
        closeOpusFile();
    }

    public void readFile(ByteBuffer buffer, int capacity, int[] args){
        readOpusFile(buffer, capacity, args);
    }

    public long getPcmDuration(){
        return getTotalPcmDuration();
    }

    public byte[] getPcmWaveForm(String path){
        return getWaveform(path);
    }

    public byte[] getPcmWaveform2(short[] array, int length){
        return getWaveform2(array, length);
    }


}
