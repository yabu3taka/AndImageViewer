package ex1.siv.storage.data;

import androidx.annotation.NonNull;

import java.util.Objects;

public class FileSet {
    public final FileInfo imageFile;
    public final FileInfo textFile;
    public final boolean indexed;

    public FileSet(FileInfo imageFile, FileInfo textFile, boolean indexed) {
        this.imageFile = imageFile;
        this.textFile = textFile;
        this.indexed = indexed;
    }

    public FileSet(FileInfoEmpty imageFile) {
        this.imageFile = imageFile;
        this.textFile = null;
        this.indexed = false;
    }

    @NonNull
    public String toString() {
        return imageFile.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof FileSet) {
            FileSet file = (FileSet) obj;
            return Objects.equals(imageFile, file.imageFile);
        } else {
            return false;
        }
    }

    public boolean hasNoText() {
        return textFile == null;
    }
}
