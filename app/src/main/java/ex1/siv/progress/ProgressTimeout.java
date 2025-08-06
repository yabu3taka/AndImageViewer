package ex1.siv.progress;

import ex1.siv.progress.data.ProgressData;

public class ProgressTimeout {
    /** @noinspection CanBeFinal*/
    public long doneTimeout = 2000L;
    /** @noinspection CanBeFinal*/
    public long errorTimeout = 5000L;

    public long getTimeout(ProgressData progressData) {
        switch (progressData.status) {
            case Success:
                return doneTimeout;
            case Error:
            case Cancel:
                return errorTimeout;
        }
        return 0;
    }
}
