package ex1.siv.storage.task;

import androidx.annotation.NonNull;

import java.io.InputStream;

import ex1.siv.storage.data.FileInfo;

public interface ResultFileOpener<F> {
    F getResultFile(@NonNull FileInfo file);
    /** @noinspection unused*/
    InputStream openResultFile(F resultFile, FileInfo file) throws Exception;
}
