package ex1.siv.file;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.documentfile.provider.DocumentFile;

public class DocumentFileController implements FileController {
    private final static String TAG = DocumentFileController.class.getSimpleName();

    private final DocumentFile file;
    private final DocumentFile parent;

    public DocumentFileController(DocumentFile file) {
        this.file = file;
        this.parent = null;
    }

    public DocumentFileController(DocumentFile file, DocumentFile parent) {
        this.file = file;
        this.parent = parent;
    }

    public static FileController create(Context context, Uri uri) {
        if (DocumentFile.isDocumentUri(context, uri)) {
            DocumentFile file = DocumentFile.fromTreeUri(context, uri);
            return new DocumentFileController(file);
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
        DocumentFile newFile = parent;
        if (newFile == null) {
            Log.e(TAG, "getParent No Parent");
            return null;
        }
        return new DocumentFileController(newFile);
    }

    @Override
    public FileController getChild(String name) {
        DocumentFile newFile = file.findFile(name);
        if (newFile == null) {
            return null;
        }
        return new DocumentFileController(newFile);
    }

    @Override
    public boolean rename(@NonNull String name) {
        return file.renameTo(name);
    }

    @Override
    public boolean delete(boolean includeMyself) {
        Log.i(TAG, "delete T=" + file.getUri() + " My=" + includeMyself);

        String name = file.getName();
        boolean ret = file.delete();
        if (!includeMyself) {
            if (parent != null) {
                assert name != null;
                parent.createDirectory(name);
            }
        }
        return ret;
    }

    @Override
    public Uri toUri() {
        return file.getUri();
    }
}
