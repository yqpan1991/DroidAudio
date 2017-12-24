package droidaudio.apollo.edus.com.droidaudio.multimedia.base;

import com.edus.apollo.common.biz.task.Priority;
import com.edus.apollo.common.biz.task.Task;
import com.edus.apollo.common.biz.task.TaskFactory;

/**
 * Created by panda on 2017/12/17.
 */

public class RecordUtils {
    private static final String GROUP_NAME = "MediaRecordWrapper";
    private static final String RECORD_REAL_GROUP_NAME = "RecordRealGroup";
    private static final int CONCURRENT_COUNT = 1;

    public static Task getRecordCmdTask(){
        Task task = TaskFactory.getInstance().getTask(GROUP_NAME, Priority.IMMEDIATE);
        task.setConcurrentCount(1);
        return task;
    }

    public static Task getSingleRealRecordTask(){
        Task task = TaskFactory.getInstance().getTask(RECORD_REAL_GROUP_NAME, Priority.IMMEDIATE);
        task.setConcurrentCount(1);
        return task;
    }

}
