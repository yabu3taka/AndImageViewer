package ex1.siv.storage.task;

/**
 * タスクの結果を得た後のコールバック
 * @param <R>
 */
public interface ResultCallback<R> {
    void onResult(ResultData<R> result);
}
