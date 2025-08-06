package ex1.siv.storage.worker;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.util.Collection;
import java.util.List;

import ex1.siv.progress.DataUtil;
import ex1.siv.progress.data.ProgressBuilderSet;
import ex1.siv.progress.data.ProgressData;
import ex1.siv.progress.data.ProgressBuilder;
import ex1.siv.progress.data.ProgressAspectRestUri;
import ex1.siv.progress.data.ProgressSingle;
import ex1.siv.room.AppDatabase;
import ex1.siv.room.DbConnection;
import ex1.siv.room.folder.FolderProp;
import ex1.siv.storage.Storage;
import ex1.siv.storage.StorageContext;
import ex1.siv.storage.StorageContextContainer;
import ex1.siv.storage.data.FolderInfo;
import ex1.siv.storage.loader.ItemLoader;

public class FolderImgBatchWorker extends Worker {
    private final static String TAG = FolderImgBatchWorker.class.getSimpleName();

    public final static int WHAT_FOLDER_IMT = 1;

    public FolderImgBatchWorker(
            @NonNull Context context,
            @NonNull WorkerParameters params) {
        super(context, params);
    }

    /*********************************************************************
     * Start
     *********************************************************************/
    private final static String WORKER_NAME = "folder-img-batch";
    private final static String FOLDER_URI = "folderUri";

    private static Data.Builder getDataBuilder(Collection<FolderInfo> folderList) {
        Data.Builder builder = new Data.Builder();
        builder.putStringArray(FOLDER_URI, DataUtil.toFolderInfoListData(folderList));
        return builder;
    }

    public static void startMe(Context context, Collection<FolderInfo> folderList) {
        Log.i(TAG, "startMe");
        Data.Builder builder = getDataBuilder(folderList);
        startMe(context, builder.build());
    }

    private static void startMe(Context context, Data data) {
        OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(FolderImgBatchWorker.class)
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
            mBuilderSet = new ProgressBuilderSet(WHAT_FOLDER_IMT);
            mRestUri = mBuilderSet.makeAndAddAspect(new ProgressAspectRestUri.AspectBuilder());

            Data data = getInputData();
            List<Uri> uriList = DataUtil.toUriList(data.getStringArray(FOLDER_URI));

            Storage storage = WorkerUtil.getStorageFromPreferences(getApplicationContext());
            StorageContext stc = new StorageContextContainer(getApplicationContext());
            AppDatabase dbc = DbConnection.connect(getApplicationContext());

            mRestUri.init(uriList);

            progress(ProgressData.Builder.forStart());

            ProgressSingle.Builder progressBuilder = new ProgressSingle.Builder(uriList.size());
            for (Uri uri : uriList) {
                Log.i(TAG, "doWork Uri=" + uri.toString());
                checkFlag();

                FolderInfo folder = storage.getFromUri(stc, uri);
                FolderProp folderPropCheck = dbc.folderPropDao().findById(folder.filename);
                if (folderPropCheck == null) {
                    ItemLoader mItemLoader = storage.getItemLoader(stc, folder);
                    mItemLoader.loadFileList().startTaskSync(stc, (resultData) ->
                    {
                        FolderProp folderProp = new FolderProp();
                        folderProp.folderId = folder.filename;
                        assert resultData.result != null;
                        folderProp.currentFile = resultData.result.get(0).imageFile.filename;
                        dbc.folderPropDao().save(folderProp);
                    });
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
            return Result.failure(mBuilderSet.createDataForResult(ProgressData.Builder.forError()));
        }
    }
}
