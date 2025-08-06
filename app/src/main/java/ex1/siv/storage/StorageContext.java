package ex1.siv.storage;

import android.content.Context;

import ex1.siv.storage.cache.CacheRequester;

public interface StorageContext {
    Context getMyContext();

    CacheRequester getCacheRequester();
}
