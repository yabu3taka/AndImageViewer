package ex1.siv.progress.data;

import androidx.work.Data;

public interface ProgressAspectBuilder {
    void setupForProgress(Data.Builder builder);

    void setupForResult(ProgressData.Status status, Data.Builder builder);
}
