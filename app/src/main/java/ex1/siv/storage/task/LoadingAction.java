package ex1.siv.storage.task;

/**
 * R型のオブジェクトをロードする処理
 * @param <R>
 */
public interface LoadingAction<R> {
    void loadResult(ResultCallback<R> callback, LoadingCancellationToken token);
}
