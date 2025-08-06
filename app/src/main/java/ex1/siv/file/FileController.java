package ex1.siv.file;

import android.net.Uri;

import androidx.annotation.NonNull;

public interface FileController {
    String getName();
    FileController getParent();
    FileController getChild(String name);
    boolean rename(@NonNull String name);
    boolean delete(boolean includeMyself);
    /** @noinspection unused*/
    Uri toUri();
}
