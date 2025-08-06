package ex1.siv.storage.data;

import android.graphics.Bitmap;

public class FolderImgAndText {
    public final Bitmap bitmap;
    public final FolderText text;

    public FolderImgAndText(FolderText text, Bitmap bitmap) {
        this.text = text;
        this.bitmap = bitmap;
    }

    public boolean hasInfo() {
        if (this.text.hasText()) {
            return true;
        }
        //noinspection RedundantIfStatement
        if (this.bitmap != null) {
            return true;
        }
        return false;
    }
}
