package ex1.siv.storage.cache;

import android.content.Context;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import android.util.Log;

import java.io.File;
import java.util.Collection;
import java.util.List;

import ex1.siv.progress.DataUtil;
import ex1.siv.progress.data.ProgressBuilder;
import ex1.siv.progress.data.ProgressBuilderSet;
import ex1.siv.progress.data.ProgressAspectRestUri;
import ex1.siv.progress.data.ProgressSecondary;
import ex1.siv.progress.data.ProgressSingle;
import ex1.siv.storage.StorageContext;
import ex1.siv.storage.StorageContextContainer;
import ex1.siv.storage.data.FileInfo;
import ex1.siv.storage.data.FileInfoList;
import ex1.siv.storage.worker.UniqueWorkerController;
import ex1.siv.storage.worker.WorkerCancelException;
import ex1.siv.progress.data.ProgressData;
import ex1.siv.storage.data.FileSet;
import ex1.siv.storage.data.FolderInfo;
import ex1.siv.storage.Storage;
import ex1.siv.storage.worker.WorkerUtil;
import ex1.siv.util.FileUtil;

public class CacheBatchWorker extends Worker {
    private final static String TAG = CacheBatchWorker.class.getSimpleName();

    public final static int WHAT_CACHE_DL = 11;
    public final static int WHAT_CACHE_CHECK = 12;

    public CacheBatchWorker(
            @NonNull Context context,
            @NonNull WorkerParameters params) {
        super(context, params);
    }

    /*********************************************************************
     * Start
     *********************************************************************/
    private final static String WORKER_NAME = "cache-batch";
    private final static String FOLDER_URI = "folderUri";
    private final static String CACHE_DIR = "cacheDir";
    private final static String CHECK_MODE = "chkMode";

    private static Data.Builder getDataBuilder(File cacheFolder, Collection<FolderInfo> folderList) {
        Data.Builder builder = new Data.Builder();
        builder.putString(CACHE_DIR, cacheFolder.getAbsolutePath());
        builder.putStringArray(FOLDER_URI, DataUtil.toFolderInfoListData(folderList));
        return builder;
    }

    public static void startMe(Context context, CacheManager cacheManager, Collection<FolderInfo> folderList) {
        Log.i(TAG, "startMe D=" + cacheManager.topDir);
        Data.Builder builder = getDataBuilder(cacheManager.topDir, folderList);
        builder.putBoolean(CHECK_MODE, false);

        startMe(context, builder.build());
    }

    public static void startCheck(Context context, CacheManager cacheManager, Collection<FolderInfo> folderList) {
        Log.i(TAG, "startMe D=" + cacheManager.topDir);
        Data.Builder builder = getDataBuilder(cacheManager.topDir, folderList);
        builder.putBoolean(CHECK_MODE, true);

        startMe(context, builder.build());
    }

    private static void startMe(Context context, Data data) {
        OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(CacheBatchWorker.class)
                .setInputData(data)
                .build();
        WorkManager.getInstance(context).enqueueUniqueWork(WORKER_NAME, ExistingWorkPolicy.KEEP, workRequest);
    }

    /*********************************************************************
     * Controller
     *********************************************************************/
    public static UniqueWorkerController getController(Context c) {
        return new UniqueWorkerController(c, WORKER_NAME);
    }

    public static ProgressAspectRestUri getRestUri(Context c) {
        return ProgressAspectRestUri.create(getController(c).getProgressData());
    }

    /*********************************************************************
     * As Worker
     *********************************************************************/
    private ProgressBuilderSet mBuilderSet;
    private ProgressAspectRestUri.AspectBuilder mRestUri;

    private void progress(ProgressBuilder builder) {
        setProgressAsync(mBuilderSet.createDataForProgress(builder));
    }

    private void checkFlag() {
        if (isStopped()) {
            throw new WorkerCancelException();
        }
    }

    @Override
    public void onStopped() {
        super.onStopped();
        mRestUri.clear();
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            Log.i(TAG, "doWork Start");
            mBuilderSet = new ProgressBuilderSet();
            mRestUri = mBuilderSet.makeAndAddAspect(new ProgressAspectRestUri.AspectBuilder());

            Data data = getInputData();
            File dirFile = DataUtil.toFile(data.getString(CACHE_DIR));
            List<Uri> uriList = DataUtil.toUriList(data.getStringArray(FOLDER_URI));
            boolean chkMode = data.getBoolean(CHECK_MODE, false);

            CacheManager cacheManager = new CacheManager(dirFile);
            Storage storage = WorkerUtil.getStorageFromPreferences(getApplicationContext());
            StorageContext stc = new StorageContextContainer(getApplicationContext());

            mBuilderSet.setWhat(chkMode ? WHAT_CACHE_CHECK : WHAT_CACHE_DL);
            mRestUri.init(uriList);

            progress(ProgressData.Builder.forStart());

            ProgressSingle.Builder progressBuilder = new ProgressSingle.Builder(uriList.size());
            for (Uri uri : uriList) {
                Log.i(TAG, "doWork Uri=" + uri.toString());
                checkFlag();

                FolderInfo folder = storage.getFromUri(stc, uri);
                File cacheFolder = cacheManager.getCacheFolder(folder);
                if (FileUtil.existsOrMake(cacheFolder)) {
                    CacheDownloader downloader = ((CacheStorage) storage).getCacheDownloader(folder, cacheFolder);
                    ProgressSingle.Builder parentProgress = progressBuilder.start(folder.filename);
                    if (chkMode) {
                        checkFolderAndClearDoneFlag(downloader);
                    } else {
                        downloadFolder(downloader, parentProgress);
                    }
                }

                mRestUri.done(uri);
                progress(progressBuilder.done(folder.filename));
            }

            return Result.success(mBuilderSet.createDataForResult(ProgressData.Builder.forSuccess()));
        } catch (WorkerCancelException ex2) {
            Log.e(TAG, "doWork Cancel");
            return Result.success(mBuilderSet.createDataForResult(ProgressData.Builder.forCancel()));
        } catch (Exception ex2) {
            Log.e(TAG, "doWork Exception", ex2);
            return Result.failure(mBuilderSet.createDataForProgress(ProgressData.Builder.forError()));
        }
    }

    private void downloadFolder(CacheDownloader downloader,
                                ProgressSingle.Builder parentProgress) throws Exception {
        FileInfoList fileList = downloader.geServerFileList();
        if (fileList == null) {
            Log.e(TAG, "downloadFolder Null fileList");
            return;
        }

        downloader.startFolder(fileList);

        ProgressSecondary.Builder progressBuilder = new ProgressSecondary.Builder(parentProgress, fileList.fileSetList.size());
        for (FileSet file : fileList.fileSetList) {
            checkFlag();

            downloader.cacheFile(file.imageFile);
            downloader.cacheFile(file.textFile);

            progress(progressBuilder.done(file.imageFile.filename));
        }

        for (FileInfo file : fileList.miscFile.values()) {
            downloader.cacheFile(file);
        }

        downloader.completeFolder();
    }

    private void checkFolderAndClearDoneFlag(CacheDownloader downloader) throws Exception {
        FileInfoList fileList = downloader.geServerFileList();
        if (fileList == null) {
            Log.e(TAG, "checkFolderAndClearDoneFlag Null fileList");
            return;
        }
        if (!checkFolder(downloader, fileList)) {
            Log.i(TAG, "checkFolderAndClearDoneFlag clear flag");
            downloader.startFolder(fileList);
        }
    }

    /**
     * @noinspection BooleanMethodIsAlwaysInverted
     */
    private boolean checkFile(CacheDownloader downloader, FileInfo file) {
        if (file != null) {
            return downloader.existCacheFile(file);
        }
        return true;
    }

    private boolean checkFolder(CacheDownloader downloader, FileInfoList fileList) {
        for (FileSet file : fileList.fileSetList) {
            checkFlag();

            if (!checkFile(downloader, file.imageFile)) {
                Log.i(TAG, "checkFolder F=" + file.imageFile.filename);
                return false;
            }
            if (!checkFile(downloader, file.textFile)) {
                Log.i(TAG, "checkFolder F=" + file.textFile.filename);
                return false;
            }
        }
        return true;
    }
}
