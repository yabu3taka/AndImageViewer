package ex1.siv.progress.format;

import android.content.Context;

import java.util.HashMap;
import java.util.Optional;

import ex1.siv.R;
import ex1.siv.progress.data.ProgressData;
import ex1.siv.progress.data.ProgressSecondary;
import ex1.siv.progress.data.ProgressSingle;

public abstract class ProgressFormatSimple implements ProgressFormat {
    @Override
    public String format(ProgressData progressData) {
        ProgressSecondary sec = ProgressSecondary.create(progressData);
        if (sec != null) {
            return formatSecondary(sec);
        }

        ProgressSingle single = ProgressSingle.create(progressData);
        if (single != null) {
            return formatPrimary(single);
        }

        return formatMess(progressData);
    }

    /*********************************************************************
     * Progress
     *********************************************************************/
    protected abstract String formatPrimary(ProgressSingle data);

    protected abstract String formatSecondary(ProgressSecondary data);

    /*********************************************************************
     * Normal
     *********************************************************************/
    protected final HashMap<ProgressData.Status, String> mStatusMess = new HashMap<>();
    protected final HashMap<Integer, String> mWhatName = new HashMap<>();

    public void setupStatusMessage(ProgressData.Status status, String message) {
        mStatusMess.put(status, message);
    }

    public void setupStatusMessage(ProgressData.Status status, Context c, int resId) {
        setupStatusMessage(status, c.getString(resId));
    }

    public void setupStatusMessage(Context c) {
        setupStatusMessage(ProgressData.Status.Running, c, R.string.progress_start);

        setupStatusMessage(ProgressData.Status.Success, c, R.string.progress_success);
        setupStatusMessage(ProgressData.Status.Cancel, c, R.string.progress_cancel);
        setupStatusMessage(ProgressData.Status.Error, c, R.string.progress_error);
    }

    public void setupWhatName(int what, String name) {
        mWhatName.put(what, name);
    }

    public void setupWhatName(int what, Context c, int resId) {
        setupWhatName(what, c.getString(resId));
    }

    protected String formatMess(ProgressData progressData) {
        String message = mStatusMess.get(progressData.status);
        if (message != null) {
            int what = progressData.getWhat();
            Optional<String> nameOpt = Optional.ofNullable(mWhatName.get(what));
            String name = nameOpt.orElse("");
            return name + message;
        }
        return progressData.getMessage();
    }
}
