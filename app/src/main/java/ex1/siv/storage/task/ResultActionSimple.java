package ex1.siv.storage.task;

import android.util.Log;

public abstract class ResultActionSimple<R> implements ResultAction<R> {
    private final static String TAG = ResultActionSimple.class.getSimpleName();

    private final TaskId<R> id;

    public ResultActionSimple(TaskId<R> id) {
        this.id = id;
    }

    @Override
    public ResultData<R> getResult() {
        try {
            return id.createResultForEnding(getResultInternal());
        } catch (Exception ex) {
            Log.e(TAG, "getResult Exception", ex);
            return id.createResultForException(ex);
        }
    }

    protected abstract R getResultInternal() throws Exception;
}
