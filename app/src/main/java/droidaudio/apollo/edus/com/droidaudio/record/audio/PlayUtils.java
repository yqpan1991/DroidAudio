package droidaudio.apollo.edus.com.droidaudio.record.audio;

import com.edus.apollo.common.biz.task.Priority;
import com.edus.apollo.common.biz.task.Task;
import com.edus.apollo.common.biz.task.TaskFactory;

/**
 * Created by panda on 2017/12/22.
 */

public class PlayUtils {
    private static final String GROUP_NAME = "AudioPlayerGroup";
    private static final int CONCURRENT_COUNT = 1;
    private static final String GROUP_PLAY_NAME = "AudioPlayerPlayGroup";
    private static final int CONCURRENT_PLAY_COUNT = 1;

    public static Task getPlayCmdTask(){
        Task task = TaskFactory.getInstance().getTask(GROUP_NAME, Priority.IMMEDIATE);
        task.setConcurrentCount(CONCURRENT_COUNT);
        return task;
    }

    public static Task getPlayTask(){
        Task task = TaskFactory.getInstance().getTask(GROUP_PLAY_NAME, Priority.IMMEDIATE);
        task.setConcurrentCount(CONCURRENT_PLAY_COUNT);
        return task;
    }
}
