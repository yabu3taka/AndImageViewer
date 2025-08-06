package ex1.siv.storage.task;

import android.util.Log;

public abstract class LoadingActionSimple<R> implements LoadingAction<R> {
    private final static String TAG = LoadingActionSimple.class.getSimpleName();

    protected final TaskId<R> id;

    public LoadingActionSimple(TaskId<R> id) {
        this.id = id;
    }

    @Override
    public void loadResult(ResultCallback<R> callback, LoadingCancellationToken token) {
        try {
            loadResultInternal(callback, token);
        } catch (Exception ex) {
            Log.e(TAG, "loadResult Exception", ex);
            callback.onResult(id.createResultForException(ex));
        }
    }

    /** @noinspection unused*/
    public abstract void loadResultInternal(ResultCallback<R> callback, LoadingCancellationToken token) throws Exception;
}
