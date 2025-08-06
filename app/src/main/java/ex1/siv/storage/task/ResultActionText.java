package ex1.siv.storage.task;

import android.util.Log;

import java.io.InputStream;

import ex1.siv.storage.data.FileInfo;
import ex1.siv.util.BitmapUtil;

public class ResultActionText<F> extends ResultActionSimple<String> {
    private final static String TAG = ResultActionText.class.getSimpleName();

    private final FileInfo file;
    private final ResultFileOpener<F> opener;

    public ResultActionText(ResultFileOpener<F> opener, FileInfo file) {
        super(new TaskId<>(file));
        this.opener = opener;
        this.file = file;
    }

    @Override
    protected String getResultInternal() throws Exception {
        Log.i(TAG, "getResultInternal F=" + file);

        F target = opener.getResultFile(file);
        try (InputStream inputStream = opener.openResultFile(target, file)) {
            return BitmapUtil.getPhotoText(inputStream, file.filename);
        }
    }
}
