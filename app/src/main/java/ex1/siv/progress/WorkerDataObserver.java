package ex1.siv.progress;

import android.util.Log;

import androidx.lifecycle.Observer;
import androidx.work.WorkInfo;

import java.util.List;

import ex1.siv.progress.data.ProgressData;

public class WorkerDataObserver implements Observer<List<WorkInfo>> {
    private final static String TAG = WorkerDataObserver.class.getSimpleName();

    private final Observer<ProgressData> ob;

    public WorkerDataObserver(Observer<ProgressData> ob) {
        this.ob = ob;
    }

    @Override
    public void onChanged(List<WorkInfo> workInfoList) {
        Log.i(TAG, "onChanged");
        for (WorkInfo workInfo : workInfoList) {
            ob.onChanged(ProgressData.create(workInfo));
        }
        if (workInfoList.isEmpty()) {
            ob.onChanged(ProgressData.createNone());
        }
    }
}
