package droidaudio.apollo.edus.com.droidaudio.file;

import android.text.TextUtils;

import java.io.File;

/**
 * Created by PandaPan on 2016/11/13.
 */

public class FileUtils {
    public static boolean isFileExists(String filePath){
        if(!TextUtils.isEmpty(filePath)){
            return new File(filePath).exists();
        }
        return false;
    }

    public static String getFileSuffix(String url, boolean withDot){
        if(TextUtils.isEmpty(url)){
            return null;
        }
        if(!url.contains(".")){
            return null;
        }
        int lastDotPos = url.lastIndexOf(".");
        if(lastDotPos < 0){
            return null;
        }else if(lastDotPos == url.length()-1){
            return null;
        }
        return  url.substring(withDot ? lastDotPos : lastDotPos+1);
    }
}
