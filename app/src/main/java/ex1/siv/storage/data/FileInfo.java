package ex1.siv.storage.data;

import androidx.annotation.NonNull;

import java.util.Objects;

import ex1.siv.modified.ModifiedFileInfo;

public abstract class FileInfo implements ModifiedFileInfo {
    public static final int TYPE_IMAGE = 1;
    public static final int TYPE_TEXT = 2;

    public final String filename;

    protected FileInfo(String filename) {
        this.filename = filename;
    }

    @NonNull
    public String toString() {
        return filename;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof FileInfo) {
            FileInfo file = (FileInfo) obj;
            return Objects.equals(filename, file.filename);
        } else {
            return false;
        }
    }

    public abstract long getModified();
}
