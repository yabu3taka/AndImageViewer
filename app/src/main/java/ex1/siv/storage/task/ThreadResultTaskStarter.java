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
 */
public class ThreadResultTaskStarter<R> implements ResultTaskStarter<R> {
    private final static String TAG = ThreadResultTaskStarter.class.getSimpleName();

    private final ResultAction<R> mAction;

    public ThreadResultTaskStarter(@NonNull ResultAction<R> action) {
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
            ResultData<R> result = mAction.getResult();
            callback.onResult(result);
        } finally {
            Log.i(TAG, "startTaskSync End");
        }
    }

    private static class MyTask<IR>
            implements ResultTask, Runnable {
        private final ResultAction<IR> mAction;
        private volatile ResultCallback<IR> mCallback;

        private MyTask(ResultAction<IR> action, ResultCallback<IR> callback) {
            mAction = action;
            mCallback = callback;
        }

        @Override
        public void cancelMe(StorageContext context) {
            Log.i(TAG, "cancel");
            mCallback = null;
        }

        @Override
        public void run() {
            try {
                Log.i(TAG, "doInBackground Start");
                ResultCallback<IR> callback = mCallback;
                mCallback = null;
                if (callback == null) {
                    return;
                }

                ResultData<IR> result = mAction.getResult();

                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(() -> {
                    Log.i(TAG, "onPostExecute");
                    callback.onResult(result);
                    Log.i(TAG, "onPostExecute End");
                });
            } finally {
                Log.i(TAG, "doInBackground End");
            }
        }
    }
}
