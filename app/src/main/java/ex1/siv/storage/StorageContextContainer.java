package ex1.siv.storage;

import android.content.Context;

import ex1.siv.storage.cache.CacheRequester;

public class StorageContextContainer implements StorageContext {

    private final Context context;

    public StorageContextContainer(Context c){
        this.context = c;
    }

    @Override
    public Context getMyContext() {
        return context;
    }

    @Override
    public CacheRequester getCacheRequester() {
        return null;
    }
}
