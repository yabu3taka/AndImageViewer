package ex1.siv.progress.observer;

import android.app.Activity;
import android.widget.ProgressBar;

import androidx.lifecycle.Observer;

import ex1.siv.progress.data.ProgressData;
import ex1.siv.progress.data.ProgressSecondary;
import ex1.siv.progress.data.ProgressSingle;

public class ProgressBarObserver implements Observer<ProgressData> {
    private final ProgressBar mView;

    public ProgressBarObserver(ProgressBar view) {
        mView = view;
    }

    public ProgressBarObserver(Activity a, int id) {
        this(a.findViewById(id));
    }

    @Override
    public void onChanged(ProgressData progressData) {
        switch (progressData.status) {
            case Success:
                mView.setProgress(100);
                break;
            case None:
                mView.setProgress(0);
                mView.setSecondaryProgress(0);
                break;
            default:
                ProgressSecondary sec = ProgressSecondary.create(progressData);
                if (sec != null) {
                    mView.setSecondaryProgress(sec.getSecondaryProgress());
                }

                ProgressSingle one = ProgressSingle.create(progressData);
                if (one != null) {
                    mView.setProgress(one.getProgress());
                }
                break;
        }
    }
}
