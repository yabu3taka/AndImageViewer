package ex1.siv.storage.data;

public class FileInfoEmpty extends FileInfo {

    protected FileInfoEmpty(FileInfo file) {
        super(file.filename + ".blk");
    }

    @Override
    public long getModified() {
        return 0;
    }
}
