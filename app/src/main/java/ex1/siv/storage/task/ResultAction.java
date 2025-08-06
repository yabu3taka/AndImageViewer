package ex1.siv.storage.task;

/**
 * R型のオブジェクトを得る処理
 * @param <R>
 */
public interface ResultAction<R> {
    ResultData<R> getResult();
}
