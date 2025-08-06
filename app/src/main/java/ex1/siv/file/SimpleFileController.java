package ex1.siv.file;

import android.net.Uri;

import androidx.annotation.NonNull;

import java.io.File;
import java.util.Objects;

import ex1.siv.util.FileUtil;

public class SimpleFileController implements FileController {
    private final File file;

    public SimpleFileController(File file) {
        this.file = file;
    }

    public static FileController create(Uri uri) {
        if (Objects.equals(uri.getScheme(), "file")) {
            String path = uri.getPath();
            assert path != null;
            return new SimpleFileController(new File(path));
        } else {
            return null;
        }
    }

    @Override
    public String getName() {
        return file.getName();
    }

    @Override
    public FileController getParent() {
        return new SimpleFileController(file.getParentFile());
    }

    @Override
    public FileController getChild(String name) {
        File newFile = new File(file, name);
        if (!newFile.exists()) {
            return null;
        }
        return new SimpleFileController(newFile);
    }

    @Override
    public boolean rename(@NonNull String name) {
        File toDir = new File(file.getParentFile(), name);
        return file.renameTo(toDir);
    }

    @Override
    public boolean delete(boolean includeMyself) {
        if (includeMyself) {
            return FileUtil.delete(file);
        } else {
            return FileUtil.deleteContent(file);
        }
    }

    @Override
    public Uri toUri() {
        return Uri.fromFile(file);
    }
}
