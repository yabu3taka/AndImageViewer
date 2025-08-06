package ex1.siv.progress.data;

import androidx.work.Data;

public interface ProgressBuilder {
    ProgressData.Status getStatus();

    void setup(Data.Builder builder);
}
