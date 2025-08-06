package ex1.siv.storage.trans;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import ex1.siv.storage.StorageContext;
import ex1.siv.storage.task.ResultCallback;
import ex1.siv.storage.task.ResultData;
import ex1.siv.storage.task.ResultTask;
import ex1.siv.storage.task.ResultTaskStarter;

public class LoadingLiveDataTrans<RParam, RResult> extends TaskTransaction<RParam, RResult>
        implements ResultCallback<RResult> {
    private final MutableLiveData<ResultData<RResult>> liveData;

    public LoadingLiveDataTrans() {
        this.liveData = new MutableLiveData<>();
    }

    public LiveData<ResultData<RResult>> getLiveData() {
        return liveData;
    }

    public RResult getResult() {
        ResultData<RResult> resultData = liveData.getValue();
        assert resultData != null;
        return resultData.result;
    }

    public ResultTask startTask(StorageContext context, RParam param, ResultTaskStarter<RResult> taskStarter) {
        this.startParam(param);

        liveData.setValue(new ResultData<>());
        return taskStarter.startTask(context, getCallback(this));
    }

    @Override
    public void onResult(ResultData<RResult> result) {
        if (result.hasResult()) {
            this.commitParam();
        } else {
            this.revertParam();
        }
        this.liveData.setValue(result);
    }
}
