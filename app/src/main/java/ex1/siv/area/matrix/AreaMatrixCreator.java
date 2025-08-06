package ex1.siv.area.matrix;

import android.graphics.Matrix;
import android.graphics.PointF;
import android.view.MotionEvent;

import ex1.siv.area.AreaData;

public abstract class AreaMatrixCreator {
    public abstract boolean isScaled();

    protected abstract boolean isImageArea(float x, float y);

    public boolean isImageArea(PointF dp) {
        return isImageArea(dp.x, dp.y);
    }

    public boolean isImageArea(MotionEvent e) {
        return isImageArea(e.getX(), e.getY());
    }

    public abstract Matrix resetScale();

    public abstract Matrix move(float distanceX, float distanceY);

    protected abstract Matrix commitScaleFactor(float x, float y, float scaleFactor);

    public Matrix commitScaleFactor(MotionEvent e, float scaleFactor) {
        return commitScaleFactor(e.getX(), e.getY(), scaleFactor);
    }

    public Matrix commitScaleFactor(PointF p, float scaleFactor) {
        return commitScaleFactor(p.x, p.y, scaleFactor);
    }

    public abstract AreaMatrixCreator createNewInstanceFromViewArea(AreaData viewArea);
}
