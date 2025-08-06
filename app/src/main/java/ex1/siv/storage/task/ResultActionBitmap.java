package ex1.siv.storage.task;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.InputStream;

import ex1.siv.storage.data.FileInfo;
import ex1.siv.util.BitmapUtil;

public class ResultActionBitmap<F> extends ResultActionSimple<Bitmap> {
    private final static String TAG = ResultActionBitmap.class.getSimpleName();

    private final ResultFileOpener<F> opener;
    private final FileInfo file;
    private final int reqWidth;
    private final int reqHeight;

    public ResultActionBitmap(ResultFileOpener<F> opener, FileInfo file, int reqWidth, int reqHeight) {
        super(new TaskId<>(file));
        this.opener = opener;
        this.file = file;
        this.reqWidth = reqWidth;
        this.reqHeight = reqHeight;
    }

    @Override
    protected Bitmap getResultInternal() throws Exception {
        Log.i(TAG, "getResultInternal F=" + file);

        F target = opener.getResultFile(file);
        if (reqWidth > 0) {
            BitmapFactory.Options options;
            try (InputStream inputStream = opener.openResultFile(target, file)) {
                options = BitmapUtil.loadBitmapOptions(inputStream, file.filename, reqWidth, reqHeight);
            }
            try (InputStream inputStream = opener.openResultFile(target, file)) {
                return BitmapUtil.loadBitmap(inputStream, file.filename, options);
            }
        } else {
            try (InputStream inputStream = opener.openResultFile(target, file)) {
                return BitmapUtil.loadBitmap(inputStream, file.filename);
            }
        }
    }
}
