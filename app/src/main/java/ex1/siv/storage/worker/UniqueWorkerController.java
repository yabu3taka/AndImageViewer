package ex1.siv.storage.worker;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import java.util.List;

import ex1.siv.progress.WorkerDataObserver;
import ex1.siv.progress.data.ProgressData;

public class UniqueWorkerController {
    private final static String TAG = UniqueWorkerController.class.getSimpleName();

    private final Context context;
    private final String jobName;

    public UniqueWorkerController(@NonNull Context context, String jobName) {
        this.context = context;
        this.jobName = jobName;
    }

    private WorkManager manager() {
        return WorkManager.getInstance(context);
    }

    public WorkInfo getWorkInfo() {
        try {
            List<WorkInfo> infoList = manager().getWorkInfosForUniqueWork(jobName).get();
            if (infoList.isEmpty()) {
                return null;
            }
            return infoList.get(0);
        } catch (Exception ex) {
            return null;
        }
    }

    public void setupObserver(LifecycleOwner o, Observer<ProgressData> ob) {
        manager().getWorkInfosForUniqueWorkLiveData(jobName).observe(o, new WorkerDataObserver(ob));
    }

    public ProgressData getProgressData() {
        return ProgressData.create(getWorkInfo());
    }

    /** @noinspection BooleanMethodIsAlwaysInverted*/
    public boolean canStart() {
        return getProgressData().canStart();
    }

    public void cancelMe() {
        Log.i(TAG, "cancelMe");
        manager().cancelUniqueWork(jobName);
    }
}
