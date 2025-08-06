package ex1.siv.progress.observer;

import android.app.Activity;
import android.widget.TextView;

import androidx.lifecycle.Observer;

import ex1.siv.progress.data.ProgressData;
import ex1.siv.progress.format.ProgressFormat;

public class ProgressMessObserver implements Observer<ProgressData> {
    private final TextView mView;
    private final ProgressFormat mFormat;

    public ProgressMessObserver(TextView view, ProgressFormat format) {
        mView = view;
        mFormat = format;
    }

    public ProgressMessObserver(Activity a, int id, ProgressFormat format) {
        this(a.findViewById(id), format);
    }

    @Override
    public void onChanged(ProgressData progressData) {
        if (progressData.status == ProgressData.Status.None) {
            mView.setText("");
        } else {
            String mess = mFormat.format(progressData);
            if (mess != null) {
                mView.setText(mess);
            }
        }
    }
}
