package ex1.siv.progress.observer;

import android.app.Activity;
import android.util.Log;
import android.view.View;

import androidx.lifecycle.Observer;
import androidx.work.Data;

import java.util.ArrayList;

import ex1.siv.progress.data.ProgressBuilderSet;
import ex1.siv.progress.data.ProgressData;
import ex1.siv.progress.ProgressTimeout;
import ex1.siv.util.UserInterfaceUtil;

public class ProgressAreaObserver implements Observer<ProgressData> {
    private final static String TAG = ProgressAreaObserver.class.getSimpleName();

    public final ProgressTimeout timeoutSetting = new ProgressTimeout();

    private final View mView;

    public ProgressAreaObserver(View group) {
        mView = group;
    }

    public ProgressAreaObserver(Activity a, int id) {
        this(a.findViewById(id));
    }

    /*********************************************************************
     * Sub Observer List
     *********************************************************************/
    private final ArrayList<Observer<ProgressData>> mObservers = new ArrayList<>();

    public void addObserver(Observer<ProgressData> ob) {
        mObservers.add(ob);
    }

    private void doOnChanged(ProgressData progressData) {
        for (Observer<ProgressData> ob : mObservers) {
            ob.onChanged(progressData);
        }
    }

    /*********************************************************************
     * Main
     ********************************************************************/
    protected void initStatus(ProgressData ignoredData) {
        UserInterfaceUtil.setVisibility(mView, View.GONE);
    }

    @Override
    public void onChanged(final ProgressData progressData) {
        Log.i(TAG, "onChanged S=" + progressData.status);

        doOnChanged(progressData);

        switch (progressData.status) {
            case None:
                initStatus(progressData);
                break;
            case Running:
                UserInterfaceUtil.setVisibility(mView, View.VISIBLE);
                break;
            default:
                long timeout = this.timeoutSetting.getTimeout(progressData);
                if (timeout > 0) {
                    mView.postDelayed(() -> {
                        Data data = ProgressBuilderSet.createDateForKeep(ProgressData.Builder.forNone(), progressData);
                        doOnChanged(new ProgressData(data));
                        initStatus(progressData);
                    }, timeout);
                } else {
                    initStatus(progressData);
                }
                break;
        }
    }
}
