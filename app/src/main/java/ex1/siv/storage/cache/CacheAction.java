package ex1.siv.storage.cache;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import ex1.siv.storage.data.FileInfo;

/**
 * 1ファイルのダウンロードの処理を行う。
 * {@link CacheRequester} に渡して依頼する。
 */
public class CacheAction {
    private final static String TAG = CacheAction.class.getSimpleName();
    private final static int CACHE_TYPE = 10;

    private final int type;
    public final FileInfo file;
    private final CacheDownloader mDownloader;

    protected CacheAction(int type, FileInfo file, CacheDownloader downloader) {
        this.type = type;
        this.file = file;
        this.mDownloader = downloader;
    }

    public static CacheAction ForBitmap(FileInfo file, CacheDownloader downloader) {
        return new CacheAction(FileInfo.TYPE_IMAGE, file, downloader);
    }

    /** @noinspection unused*/
    public static CacheAction ForText(FileInfo file, CacheDownloader downloader) {
        return new CacheAction(FileInfo.TYPE_TEXT, file, downloader);
    }

    static void removeAllMessages(Handler handler) {
        handler.removeMessages(FileInfo.TYPE_IMAGE);
        handler.removeMessages(FileInfo.TYPE_TEXT);

        removeAllCacheRequestMessages(handler);
    }

    static void removeAllCacheRequestMessages(Handler handler) {
        if(handler == null)
        {
            return;
        }
        handler.removeMessages(FileInfo.TYPE_IMAGE + CACHE_TYPE);
        handler.removeMessages(FileInfo.TYPE_TEXT + CACHE_TYPE);
    }

    public String getId() {
        return file.filename;
    }

    /*********************************************************************
     * Download Cache
     *********************************************************************/
    protected volatile Exception mException;

    Message makeCacheRequestMessage(boolean cache) {
        Message mess = Message.obtain();
        mess.what = this.type;
        if (cache) {
            mess.what += CACHE_TYPE;
        }
        mess.obj = this;
        return mess;
    }

    public boolean existCache() {
        return mDownloader.existCacheFile(file);
    }

    void downloadCache() {
        mException = null;
        try {
            if (!mCancel) {
                mDownloader.cacheFile(file);
            }
        } catch (Exception ex) {
            Log.e(TAG, "doCallback Ex", ex);
            mException = ex;
        }
    }

    /*********************************************************************
     * Status
     *********************************************************************/
    private volatile boolean mCancel = false;

    public boolean isCanceled() {
        return mCancel;
    }

    public void cancelMe(Handler handler) {
        mCancel = true;
        handler.removeMessages(this.type, this);
    }

    public void doCallback() {
    }
}
