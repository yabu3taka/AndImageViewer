package ex1.siv.storage.cache;

import androidx.annotation.NonNull;

import ex1.siv.storage.StorageContext;
import ex1.siv.storage.task.ResultTask;
import ex1.siv.storage.task.ResultTaskStarter;
import ex1.siv.storage.task.ResultCallback;

public class CacheResultTaskStarter<R> implements ResultTaskStarter<R>, ResultTask {
    private final CacheActionReply<R> mCacheAction;

    CacheResultTaskStarter(CacheActionReply<R> cacheAction) {
        mCacheAction = cacheAction;
    }

    @Override
    public ResultTask startTask(@NonNull StorageContext context, @NonNull ResultCallback<R> callback) {
        context.getCacheRequester().requestLoad(mCacheAction, callback);
        return this;
    }

    @Override
    public void startTaskSync(@NonNull StorageContext context, @NonNull ResultCallback<R> callback) {
        mCacheAction.downloadCache();
        mCacheAction.setCallback(callback);
        mCacheAction.doCallback();
    }

    @Override
    public void cancelMe(StorageContext context) {
        context.getCacheRequester().cancelAction(mCacheAction);
    }
}
