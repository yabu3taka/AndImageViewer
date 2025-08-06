package ex1.siv.file;

import android.content.Context;
import android.net.Uri;

import androidx.annotation.NonNull;

/** @noinspection unused*/
public class FileControllerUtil {
    public static FileController create(@NonNull Context context, @NonNull Uri uri) {
        FileController fc = DocumentFileController.create(context, uri);
        if (fc != null) {
            return fc;
        }
        fc = SimpleFileController.create(uri);
        //noinspection RedundantIfStatement
        if (fc != null) {
            return fc;
        }
        return null;
    }
}
