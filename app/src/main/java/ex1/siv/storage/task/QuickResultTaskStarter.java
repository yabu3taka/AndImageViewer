package ex1.siv.storage.task;

import androidx.annotation.NonNull;

import ex1.siv.storage.StorageContext;

public class QuickResultTaskStarter<R> implements ResultTaskStarter<R>, ResultTask {
    private final ResultData<R> result;

    public QuickResultTaskStarter(TaskId<R> id, R result) {
        this.result = id.createResultForEnding(result);
    }

    public QuickResultTaskStarter(TaskId<R> id, int ignored, Exception ex) {
        this.result = id.createResultForException(ex);
    }

    @Override
    public ResultTask startTask(@NonNull StorageContext context, @NonNull ResultCallback<R> callback) {
        callback.onResult(result);
        return this;
    }

    @Override
    public void startTaskSync(@NonNull StorageContext context, @NonNull ResultCallback<R> callback) {
        startTask(context, callback);
    }

    @Override
    public void cancelMe(StorageContext context) {
    }
}
