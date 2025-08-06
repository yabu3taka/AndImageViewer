package ex1.siv.storage.trans;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import ex1.siv.storage.StorageContext;
import ex1.siv.storage.task.ResultCallback;
import ex1.siv.storage.task.ResultTask;
import ex1.siv.storage.task.ResultTaskStarter;
import ex1.siv.util.ShowUtil;

public class LiveDataTrans<RParam, RResult> extends TaskTransaction<RParam, RResult> {
    private final MutableLiveData<RResult> liveData;

    public LiveDataTrans() {
        this.liveData = new MutableLiveData<>();
    }

    public LiveData<RResult> getLiveData() {
        return liveData;
    }

    /** @noinspection unused*/
    public RResult getResult() {
        return liveData.getValue();
    }

    public ResultTask startTask(StorageContext context, RParam param, ResultTaskStarter<RResult> taskStarter) {
        this.startParam(param);

        final Context app = context.getMyContext().getApplicationContext();
        ResultCallback<RResult> callback = result -> {
            if (result.hasResult()) {
                LiveDataTrans.this.commitParam();
                LiveDataTrans.this.liveData.setValue(result.result);
            } else {
                LiveDataTrans.this.revertParam();
                ShowUtil.showException(app, result.exception);
            }
        };
        return taskStarter.startTask(context, getCallback(callback));
    }
}
