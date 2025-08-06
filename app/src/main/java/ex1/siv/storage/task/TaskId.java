package ex1.siv.storage.task;

import androidx.annotation.NonNull;

import ex1.siv.storage.data.FileInfo;
import ex1.siv.storage.data.FolderInfo;

public class TaskId<R> {
    private final String id;

    public TaskId(String id) {
        this.id = id;
    }

    public TaskId(@NonNull FileInfo id) {
        this.id = id.filename;
    }

    public TaskId(@NonNull FolderInfo id) {
        this.id = id.filename;
    }

    @NonNull
    @Override
    public String toString() {
        return id;
    }

    /** @noinspection unused*/
    public ResultData<R> createResultForLoading(R data){
        return new ResultData<>(this.id, data, null, true);
    }

    public ResultData<R> createResultForEnding(R data){
        return new ResultData<>(this.id, data, null, false);
    }

    public ResultData<R> createResultForException(Exception ex) {
        return new ResultData<>(this.id, null, ex, false);
    }
}
