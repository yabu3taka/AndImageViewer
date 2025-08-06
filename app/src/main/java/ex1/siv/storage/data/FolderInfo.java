package ex1.siv.storage.data;

import androidx.annotation.NonNull;
import android.net.Uri;

import java.util.Objects;

import ex1.siv.file.FileController;
import ex1.siv.storage.StorageContext;

public abstract class FolderInfo {
    public final String filename;

    protected FolderInfo(String filename) {
        this.filename = filename;
    }

    @NonNull
    public String toString() {
        return filename;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof FolderInfo) {
            FolderInfo folder = (FolderInfo) obj;
            return Objects.equals(filename, folder.filename);
        } else {
            return false;
        }
    }

    abstract public Uri toUri();

    abstract public FileController toFileController(StorageContext context);
}
