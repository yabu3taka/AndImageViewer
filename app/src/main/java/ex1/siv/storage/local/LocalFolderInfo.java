package ex1.siv.storage.local;

import android.net.Uri;

import java.io.File;

import ex1.siv.file.FileController;
import ex1.siv.file.SimpleFileController;
import ex1.siv.storage.StorageContext;
import ex1.siv.storage.data.FolderInfo;

public class LocalFolderInfo extends FolderInfo {
    public final File target;

    LocalFolderInfo(File parentPath, String filename) {
        super(filename);
        this.target = new File(parentPath, filename);
    }

    public LocalFolderInfo(File target) {
        super(target.getName());
        this.target = target;
    }

    @Override
    public Uri toUri() {
        return Uri.fromFile(target);
    }

    @Override
    public FileController toFileController(StorageContext context) {
        return new SimpleFileController(target);
    }

    public File toFile() {
        return target;
    }
}
