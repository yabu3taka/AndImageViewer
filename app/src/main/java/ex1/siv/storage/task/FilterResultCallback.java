package ex1.siv.storage.task;

/**
 * 得られた結果が正しいかチェックしてからコールバックを実行するコールバックを作成
 * @param <R>
 */
public abstract class FilterResultCallback<R> {
    protected abstract Exception checkResultData(ResultData<R> result);

    public ResultCallback<R> makeCallback(ResultCallback<R> callback) {
        MyCallBack<R> ret = new MyCallBack<>();
        ret.mFilter = this;
        ret.mCallback = callback;
        return ret;
    }

    private static class MyCallBack<IR> implements ResultCallback<IR> {
        private FilterResultCallback<IR> mFilter;
        private ResultCallback<IR> mCallback;

        @Override
        public void onResult(ResultData<IR> result) {
            Exception ex = null;
            if (result.hasResult()) {
                ex = mFilter.checkResultData(result);
            }
            if (ex != null) {
                mCallback.onResult(result.getExceptionResult(ex));
            } else {
                mCallback.onResult(result);
            }
        }
    }
}
