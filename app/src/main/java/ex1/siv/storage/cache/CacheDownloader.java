package ex1.siv.storage.cache;

import java.io.File;

import ex1.siv.storage.data.FileInfo;
import ex1.siv.storage.data.FileInfoList;

/**
 * キャッシュのダウンロード対象のファイルを管理する
 * 対象のフォルダー内のファイルを管理する
 */
public abstract class CacheDownloader {
    public abstract CacheStorage getCacheStorage();

    public abstract FileInfoList geServerFileList() throws Exception;

    public abstract File getCacheFile(FileInfo file);

    public abstract boolean existCacheFile(FileInfo file);

    public void cacheFile(FileInfo file) throws Exception {
        if (file != null) {
            if (existCacheFile(file)) {
                return;
            }
            cacheFileInternal(file);
        }
    }

    protected abstract void cacheFileInternal(FileInfo file) throws Exception;

    public abstract void startFolder(FileInfoList fileList);

    public abstract void completeFolder();

    public abstract boolean isCompleted();
}
