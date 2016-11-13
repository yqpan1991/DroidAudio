package droidaudio.apollo.edus.com.droidaudio.thread;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by PandaPan on 2016/11/13.
 */

public class SingleExecutor {
    private String mGroupName;
    private ExecutorService mExecutorService;

    public SingleExecutor(String groupName){
        mGroupName = groupName;
        mExecutorService = Executors.newSingleThreadExecutor();
    }

    public void postRunnable(Runnable runnable){
        if(runnable != null && !mExecutorService.isShutdown() && !mExecutorService.isTerminated()){
            mExecutorService.submit(runnable);
        }
    }

    public void shutDown(){
        mExecutorService.shutdown();
    }
}
