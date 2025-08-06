package ex1.siv.storage.task;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ex1.siv.storage.StorageContext;

/**
 * 別スレッドで R 型のオブジェクトを得るタスクを開始する。
 * 途中経過も得る。
 */
public class LoadingResultTaskStarter<R> implements ResultTaskStarter<R> {
    private final static String TAG = LoadingResultTaskStarter.class.getSimpleName();

    private final LoadingAction<R> mAction;

    public LoadingResultTaskStarter(LoadingAction<R> action) {
        mAction = action;
    }

    @Override
    public ResultTask startTask(@NonNull StorageContext context, @NonNull ResultCallback<R> callback) {
        MyTask<R> task = new MyTask<>(mAction, callback);
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(task);
        return task;
    }

    @Override
    public void startTaskSync(@NonNull StorageContext context, @NonNull ResultCallback<R> callback) {
        try {
            Log.i(TAG, "startTaskSync Start");
            mAction.loadResult(callback, new MyToken());
        } finally {
            Log.i(TAG, "startTaskSync End");
        }
    }

    private static class MyTask<IR>
            implements ResultTask, Runnable, ResultCallback<IR>, LoadingCancellationToken {
        private final LoadingAction<IR> mAction;
        private volatile ResultCallback<IR> mCallback;

        private MyTask(LoadingAction<IR> action, ResultCallback<IR> callback) {
            mAction = action;
            mCallback = callback;
        }

        @Override
        public void cancelMe(StorageContext context) {
            Log.i(TAG, "cancelMe");
            mCallback = null;
        }

        @Override
        public boolean isCanceledRequested() {
            return mCallback == null;
        }

        @Override
        public void onResult(ResultData<IR> result) {
            ResultCallback<IR> callback = mCallback;
            if (callback == null) {
                return;
            }
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(() -> {
                Log.i(TAG, "onResult");
                callback.onResult(result);
                Log.i(TAG, "onResult End");
            });
        }

        @Override
        public void run() {
            try {
                Log.i(TAG, "MyTask.run Start");
                mAction.loadResult(this, this);
                mCallback = null;
            } finally {
                Log.i(TAG, "MyTask.run End");
            }
        }
    }

    private static class MyToken implements LoadingCancellationToken {
        @Override
        public boolean isCanceledRequested() {
            return false;
        }
    }
}
