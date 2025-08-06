package ex1.siv.storage.cache;

import android.graphics.Bitmap;

import ex1.siv.storage.data.FileInfo;
import ex1.siv.storage.task.ResultAction;
import ex1.siv.storage.task.ResultCallback;
import ex1.siv.storage.task.TaskId;

/**
 * 1ファイルのダウンロードの処理を行う。
 * {@link CacheRequester} に渡して依頼する。
 * 完了後に指定された処理を行う。
 */
public class CacheActionReply<R> extends CacheAction {
    private final ResultAction<R> mLoadAction;
    private ResultCallback<R> mCallback;

    private CacheActionReply(int type, FileInfo file, CacheDownloader downloader, ResultAction<R> loadAction) {
        super(type, file, downloader);
        this.mLoadAction = loadAction;
    }

    public static CacheActionReply<Bitmap> ForBitmap(FileInfo file, CacheDownloader downloader, ResultAction<Bitmap> loadAction) {
        return new CacheActionReply<>(FileInfo.TYPE_IMAGE, file, downloader, loadAction);
    }

    public static CacheActionReply<String> ForText(FileInfo file, CacheDownloader downloader, ResultAction<String> loadAction) {
        return new CacheActionReply<>(FileInfo.TYPE_TEXT, file, downloader, loadAction);
    }

    public void setCallback(ResultCallback<R> c) {
        mCallback = c;
    }

    @Override
    public void doCallback() {
        if (mException != null) {
            mCallback.onResult(new TaskId<R>(file).createResultForException(mException));
        } else {
            mCallback.onResult(mLoadAction.getResult());
        }
    }
}
