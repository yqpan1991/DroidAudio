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
}
