package ex1.siv.progress.observer;

import android.app.Activity;
import android.view.View;

import androidx.lifecycle.Observer;

import java.util.ArrayList;

import ex1.siv.progress.data.ProgressData;
import ex1.siv.util.UserInterfaceUtil;

public class ProgressPlayerObserver implements Observer<ProgressData> {
    private final ArrayList<View> mButtonViews = new ArrayList<>();
    private View mPlayView = null;

    private ProgressPlayerObserver addButton(View v) {
        mButtonViews.add(v);
        return this;
    }

    public ProgressPlayerObserver addPlayButton(Activity a, int id) {
        mPlayView = a.findViewById(id);
        return addButton(mPlayView);
    }

    public ProgressPlayerObserver addStopButton(Activity a, int id) {
        return addButton(a.findViewById(id));
    }

    private void setButtonVisibility(int visibility) {
        for (View v : mButtonViews) {
            v.setVisibility(visibility);
        }
    }

    /*********************************************************************
     * Main
     *********************************************************************/
    protected void initStatus(ProgressData ignoredData) {
        setButtonVisibility(View.GONE);
    }

    @Override
    public void onChanged(ProgressData progressData) {
        if (progressData.status == ProgressData.Status.Running) {
            setButtonVisibility(View.VISIBLE);
            UserInterfaceUtil.setVisibility(mPlayView, View.GONE);
        } else {
            initStatus(progressData);
        }
    }
}
