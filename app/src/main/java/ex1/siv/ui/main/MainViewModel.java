package ex1.siv.ui.main;

import android.app.Application;

import androidx.lifecycle.LiveData;

import ex1.siv.storage.Storage;
import ex1.siv.storage.StorageContext;
import ex1.siv.storage.task.ResultData;
import ex1.siv.storage.trans.LoadingLiveDataTrans;
import ex1.siv.storage.task.EmptyTask;
import ex1.siv.storage.task.ResultTask;
import ex1.siv.storage.data.TopFolderInfo;
import ex1.siv.ui.StorageViewModel;

public class MainViewModel extends StorageViewModel {
    public MainViewModel(Application a) {
        super(a);
    }

    private final LoadingLiveDataTrans<MainDirData, TopFolderInfo> topFolderLD = new LoadingLiveDataTrans<>();
    private ResultTask mTask = new EmptyTask();

    LiveData<ResultData<TopFolderInfo>> getTopFolder() {
        return topFolderLD.getLiveData();
    }

    void loadTopFolder(StorageContext context, MainDirData mdd) {
        Storage storage = getStorage(mdd.getStorageType());
        mTask.cancelMe(context);
        mTask = topFolderLD.startTask(context, mdd, storage.loadTopFolder(context, mdd.getTopDir()));
    }

    boolean isLoaded(MainDirData mdd) {
        return topFolderLD.isLoaded(mdd);
    }
}
