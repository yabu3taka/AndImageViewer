package ex1.siv.storage.trans;

import ex1.siv.storage.task.FilterResultCallback;
import ex1.siv.storage.task.ResultCallback;

public abstract class TaskTransaction<RParam, RResult> {
    private RParam currentParam = null;
    private RParam loadingParam = null;
    private RParam retryParam = null;

    public boolean isLoaded(RParam param) {
        return param.equals(currentParam);
    }

    public boolean isSameTarget(RParam param) {
        if (isLoaded(param)) {
            return true;
        }
        return param.equals(loadingParam);
    }

    public void clearParam() {
        currentParam = null;
    }

    public void startParam(RParam param) {
        loadingParam = param;
    }

    void commitParam() {
        currentParam = loadingParam;
        loadingParam = null;
    }

    void revertParam() {
        retryParam = loadingParam;
        loadingParam = null;
    }

    /** @noinspection unused*/
    public RParam getRetryParam() {
        return retryParam;
    }

    /*********************************************************************
     * Filter
     *********************************************************************/
    private FilterResultCallback<RResult> mFilter;

    public void setFilter(FilterResultCallback<RResult> filter) {
        this.mFilter = filter;
    }

    protected ResultCallback<RResult> getCallback(ResultCallback<RResult> cb) {
        if (mFilter != null) {
            return mFilter.makeCallback(cb);
        } else {
            return cb;
        }
    }
}
