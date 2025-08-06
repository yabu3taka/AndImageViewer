package ex1.siv.storage.cache;

import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import ex1.siv.modified.ModifiedChecker;
import ex1.siv.storage.data.FileInfo;
import ex1.siv.storage.data.FileInfoList;
import ex1.siv.util.FileUtil;
import ex1.siv.util.ListUtil;

public abstract class CacheDownloaderSimple extends CacheDownloader {
    private final static String TAG = CacheDownloaderSimple.class.getSimpleName();

    private final File toFolder;
    private final File flagFile;
    private final ModifiedChecker checker;

    protected CacheDownloaderSimple(File toFolder, ModifiedChecker checker) {
        this.toFolder = toFolder;
        this.flagFile = getDoneFlagFile(toFolder);
        this.checker = checker;
    }

    public static File getDoneFlagFile(File toFolder) {
        return new File(toFolder, "done.flg");
    }

    @Override
    public File getCacheFile(FileInfo file) {
        return new File(toFolder, file.filename);
    }

    @Override
    public boolean existCacheFile(FileInfo file) {
        return checker.existCacheFile(getCacheFile(file), file);
    }

    protected File getTmpCacheFile(FileInfo file) {
        return new File(toFolder, file.filename + ".tmp");
    }

    protected void commitCache(FileInfo file) throws Exception {
        File cacheFile = getCacheFile(file);
        File tmpFile = getTmpCacheFile(file);
        if (!tmpFile.renameTo(cacheFile)) {
            throw new IOException("Rename failed");
        }
        if (!checker.rememberFileInfo(cacheFile, file)) {
            throw new IOException("setLastModified failed");
        }
        Log.i(TAG, "commitCache F=" + file.filename + " T=" + cacheFile.lastModified() + " TT=" + file.getModified());
    }

    @Override
    public void startFolder(FileInfoList fileList) {
        Set<String> needs = fileList.toFileCollection();
        needs.add(flagFile.getName());
        checker.addNeedFile(needs);

        File[] files = FileUtil.getUnnecessaryFile(toFolder, needs);
        if (!ListUtil.isNullOrEmpty(files)) {
            FileUtil.delete(files);
        }

        //noinspection ResultOfMethodCallIgnored
        flagFile.delete();
    }

    @Override
    public void completeFolder() {
        checker.cleanUp();
        FileUtil.createFlagFile(flagFile);
        Log.i(TAG, "completeFolder N=" + FileUtil.getFileCount(toFolder) + " F=" + flagFile);
    }

    @Override
    public boolean isCompleted() {
        boolean ret = flagFile.exists();
        Log.i(TAG, "isCompleted F=" + flagFile + " R=" + ret);
        return ret;
    }
}
