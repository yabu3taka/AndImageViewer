package ex1.siv.storage.task;

import androidx.annotation.NonNull;

import ex1.siv.storage.StorageContext;

/**
 * タスクを開始する
 * @param <R> タスクの結果の型
 */
public interface ResultTaskStarter<R> {
    ResultTask startTask(@NonNull StorageContext context, @NonNull ResultCallback<R> callback);
    void startTaskSync(@NonNull StorageContext context, @NonNull ResultCallback<R> callback);
}
