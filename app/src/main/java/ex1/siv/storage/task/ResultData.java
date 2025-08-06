package ex1.siv.storage.task;

/**
 * タスクの結果を示すオブジェクト
 * タスクの返却や例外を保持する
 * @param <R>
 */
public class ResultData<R> {
    public final String id;
    public final R result;
    public final Exception exception;
    public final boolean loading;

    public ResultData() {
        this.id = "";
        this.result = null;
        this.exception = null;
        this.loading = true;
    }

    public ResultData(String id, R result, Exception ex, boolean loading) {
        this.id = id;
        this.result = result;
        this.exception = ex;
        this.loading = loading;
    }

    public boolean hasResult() {
        return result != null;
    }

    public boolean isDone() {
        return !this.loading;
    }

    public ResultData<R> getExceptionResult(Exception ex) {
        return new ResultData<>(id, null, ex, false);
    }
}
