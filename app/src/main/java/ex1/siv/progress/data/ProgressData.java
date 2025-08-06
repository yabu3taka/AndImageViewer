package ex1.siv.progress.data;

import android.util.Log;

import androidx.work.Data;
import androidx.work.WorkInfo;

public class ProgressData {
    private final static String TAG = ProgressData.class.getSimpleName();

    private static final String FIELD_STATUS = "status";
    public static final String FIELD_WHAT = "what";
    private static final String FIELD_MESS = "mess";

    public final Status status;
    public final Data data;

    protected ProgressData(ProgressData progressData) {
        this.status = progressData.status;
        this.data = progressData.data;
    }

    public ProgressData(Data data) {
        this.status = Status.valueOf(data.getString(FIELD_STATUS));
        this.data = data;
    }

    public static ProgressData create(ProgressBuilder builder) {
        return new ProgressData(ProgressBuilderSet.createDateSimply(builder));
    }

    public static ProgressData createNone() {
        Log.i(TAG, "create null");
        return create(ProgressData.Builder.forNone());
    }

    public static ProgressData create(WorkInfo workInfo) {
        if (workInfo == null) {
            return createNone();
        }

        switch (workInfo.getState()) {
            case ENQUEUED:
            case BLOCKED:
                Log.i(TAG, "create ENQUEUED/BLOCKED");
                return create(ProgressData.Builder.forStart().setMessage("Waiting"));
            case CANCELLED:
                Log.i(TAG, "create CANCELLED");
                return create(ProgressData.Builder.forCancel());
            case SUCCEEDED:
                Log.i(TAG, "create SUCCEEDED");
                return new ProgressData(workInfo.getOutputData());
            case FAILED:
                Log.i(TAG, "create FAILED");
                return new ProgressData(workInfo.getOutputData());
        }
        Log.i(TAG, "create default");
        return new ProgressData(workInfo.getProgress());
    }

    public int getWhat() {
        return data.getInt(FIELD_WHAT, 0);
    }

    public String getMessage() {
        return data.getString(FIELD_MESS);
    }

    /*********************************************************************
     * Status
     ********************************************************************/
    public enum Status {
        None,

        Running,

        Success,
        Error,
        Cancel,
    }

    public boolean canStart() {
        switch (status) {
            case Running:
                return false;
        }
        return true;
    }

    public boolean completed() {
        switch (status) {
            case Success:
            case Error:
            case Cancel:
                return true;
        }
        return false;
    }

    /*********************************************************************
     * Data Builder
     ********************************************************************/
    public static class Builder implements ProgressBuilder {
        private final Status status;
        private String message;

        public Builder(Status status) {
            this.status = status;
            this.message = "";
        }

        public Status getStatus() {
            return status;
        }

        public Builder setMessage(String message) {
            this.message = message;
            return this;
        }

        public void setup(Data.Builder builder) {
            builder.putString(FIELD_STATUS, status.name());
            builder.putString(FIELD_MESS, message);
        }

        public static Builder forNone() {
            return new Builder(Status.None);
        }

        public static Builder forStart() {
            return new Builder(Status.Running);
        }

        public static Builder forSuccess() {
            return new Builder(Status.Success);
        }

        public static Builder forCancel() {
            return new Builder(Status.Cancel);
        }

        public static Builder forError() {
            return new Builder(Status.Error).setMessage("Error");
        }
    }
}
