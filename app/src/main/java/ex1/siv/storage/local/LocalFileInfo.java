package ex1.siv.storage.local;

import java.io.File;

import ex1.siv.storage.data.FileInfo;

public class LocalFileInfo extends FileInfo {
    public final File target;

    LocalFileInfo(File parentPath, String filename) {
        super(filename);
        this.target = new File(parentPath, filename);
    }

    @Override
    public long getModified() {
        return target.lastModified();
    }
}
