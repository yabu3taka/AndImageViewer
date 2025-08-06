package ex1.siv.storage.loader;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import ex1.siv.storage.data.FileInfoEmpty;
import ex1.siv.storage.data.FileSetList;
import ex1.siv.storage.task.ResultAction;
import ex1.siv.storage.task.ResultActionSimple;
import ex1.siv.storage.task.ResultTaskStarter;
import ex1.siv.storage.task.QuickResultTaskStarter;
import ex1.siv.storage.data.FileInfo;
import ex1.siv.storage.data.FileSet;
import ex1.siv.storage.task.TaskId;
import ex1.siv.storage.task.ThreadResultTaskStarter;
import ex1.siv.util.FileUtil;

public abstract class ItemLoader {
    public abstract ResultTaskStarter<FileSetList> loadFileList();

    public ResultTaskStarter<Bitmap> loadImage(final FileSet file, int reqWidth, int reqHeight) {
        if (file.imageFile instanceof FileInfoEmpty) {
            TaskId<Bitmap> id = new TaskId<>(file.imageFile);
            ResultAction<Bitmap> action = new ResultActionSimple<Bitmap>(id) {
                @Override
                public Bitmap getResultInternal() throws Exception {
                    int width = 300;
                    int height = 50;

                    Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

                    Canvas canvas = new Canvas(bmp);
                    canvas.drawColor(Color.WHITE);

                    String str = FileUtil.getBasename(FileUtil.getBasename(file.imageFile.filename));
                    Paint paint = new Paint();
                    paint.setColor(Color.GRAY);
                    Paint.FontMetrics metrics = paint.getFontMetrics();
                    paint.setTextSize(20);
                    float textWidth = paint.measureText(str);
                    canvas.drawText(str,
                            width / 2.0f - textWidth / 2,
                            height / 2.0f - (metrics.ascent + metrics.descent) / 2,
                            paint);

                    return bmp;
                }
            };
            return new ThreadResultTaskStarter<>(action);
        } else {
            return loadImageInternal(file.imageFile, reqWidth, reqHeight);
        }
    }

    protected abstract ResultTaskStarter<Bitmap> loadImageInternal(FileInfo file, int reqWidth, int reqHeight);

    public ResultTaskStarter<String> loadText(FileSet file) {
        if (file.hasNoText()) {
            TaskId<String> id = new TaskId<>(file.imageFile);
            return new QuickResultTaskStarter<>(id, "");
        }
        return loadTextInternal(file.textFile);
    }

    protected abstract ResultTaskStarter<String> loadTextInternal(FileInfo file);

    /* Cache */
    public abstract void prepareCache(FileSet file);

    public abstract void clearAllCacheRequest();
}
