package ex1.siv.storage.cache;

import android.graphics.Bitmap;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;

import ex1.siv.storage.loader.ItemLoaderGeneric;
import ex1.siv.storage.StorageContext;
import ex1.siv.storage.data.FileInfo;
import ex1.siv.storage.data.FileSet;
import ex1.siv.storage.loader.ItemLoader;
import ex1.siv.storage.data.FileSetList;
import ex1.siv.storage.data.FolderInfo;
import ex1.siv.storage.local.LocalStorage;
import ex1.siv.storage.task.ResultAction;
import ex1.siv.storage.task.ResultActionBitmap;
import ex1.siv.storage.task.ResultActionText;
import ex1.siv.storage.task.ResultFileOpener;
import ex1.siv.storage.task.ResultTaskStarter;
import ex1.siv.storage.task.QuickResultTaskStarter;
import ex1.siv.storage.task.TaskId;
import ex1.siv.storage.task.ThreadResultTaskStarter;
import ex1.siv.util.BitmapUtil;

public class CacheItemLoader extends ItemLoaderGeneric<FolderInfo>
        implements ResultFileOpener<File> {
    private final static String TAG = CacheItemLoader.class.getSimpleName();

    private final CacheDownloader mDownloader;
    private final CacheStorage mStorage;

    private CacheItemLoader(StorageContext context, CacheStorage storage, CacheDownloader downloader, FolderInfo origFolder) {
        super(context, origFolder);
        mStorage = storage;
        mDownloader = downloader;
    }

    public static ItemLoader getItemLoader(StorageContext context, CacheDownloader downloader, FolderInfo origFolder, File toFolder) {
        if (downloader.isCompleted()) {
            Log.i(TAG, "getItemLoader LocalStorage");
            LocalStorage cacheStorage = new LocalStorage();
            return cacheStorage.getItemLoader(context, cacheStorage.getFromFile(toFolder));
        } else {
            Log.i(TAG, "getItemLoader CacheItemLoader");
            return new CacheItemLoader(context, downloader.getCacheStorage(), downloader, origFolder);
        }
    }

    @Override
    public ResultTaskStarter<FileSetList> loadFileListInternal(FolderInfo folder) {
        return mStorage.loadFileList(folder);
    }

    @Override
    public File getResultFile(@NonNull FileInfo file) {
        return mDownloader.getCacheFile(file);
    }

    @Override
    public InputStream openResultFile(File resultFile, FileInfo file) throws Exception {
        return Files.newInputStream(resultFile.toPath());
    }

    @Override
    protected ResultTaskStarter<Bitmap> loadImageInternal(FileInfo file, int reqWidth, int reqHeight) {
        ResultAction<Bitmap> action = new ResultActionBitmap<>(this, file, reqWidth, reqHeight);
        if (mDownloader.existCacheFile(file)) {
            Log.i(TAG, "loadImageFile Cached=" + file);
            return new ThreadResultTaskStarter<>(action);
        } else {
            Log.i(TAG, "loadImageFile New=" + file);
            return new CacheResultTaskStarter<>(CacheActionReply.ForBitmap(file, mDownloader, action));
        }
    }

    @Override
    protected ResultTaskStarter<String> loadTextInternal(FileInfo file) {
        final TaskId<String> id = new TaskId<>(file);
        if (mDownloader.existCacheFile(file)) {
            Log.i(TAG, "loadTextFile Cached=" + file);
            try {
                File target = getResultFile(file);
                try (InputStream inputStream = openResultFile(target, file)) {
                    String text = BitmapUtil.getPhotoText(inputStream, target.getName());
                    return new QuickResultTaskStarter<>(id, text);
                }
            } catch (Exception ex) {
                Log.e(TAG, "loadTextFile Exception", ex);
                return new QuickResultTaskStarter<>(id, 0, ex);
            }
        } else {
            Log.i(TAG, "loadTextFile New=" + file);
            ResultAction<String> action = new ResultActionText<>(this, file);
            return new CacheResultTaskStarter<>(CacheActionReply.ForText(file, mDownloader, action));
        }
    }

    @Override
    public void prepareCache(FileSet file) {
        context.getCacheRequester().requestCache(CacheAction.ForBitmap(file.imageFile, mDownloader));
    }

    @Override
    public void clearAllCacheRequest() {
        context.getCacheRequester().stopAllCacheRequest();
    }
}
