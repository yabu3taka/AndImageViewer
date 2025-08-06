package ex1.siv.area.matrix;

import android.graphics.Matrix;
import android.graphics.RectF;

import ex1.siv.area.AreaData;
import ex1.siv.area.AreaMatrixUtil;

public abstract class AreaMatrixCreatorBase extends AreaMatrixCreator {
    final AreaData mBitmapArea;
    private final AreaData mViewArea;
    private final Matrix mInitMatrix;
    private final float mMinScale;
    private final float mMaxScale;

    private Matrix mMatrix;
    private boolean mScaledFlag;

    AreaMatrixCreatorBase(AreaData viewArea, AreaData bitmapArea) {
        mBitmapArea = bitmapArea;
        mViewArea = viewArea;

        float scale = adjustInitScale(viewArea, bitmapArea);
        float dx = getInitX(viewArea, bitmapArea, scale);
        float dy = getInitY(viewArea, bitmapArea, scale);

        mInitMatrix = new Matrix();
        mInitMatrix.reset();
        mInitMatrix.postScale(scale, scale);
        mInitMatrix.postTranslate(dx, dy);

        mMinScale = getMinScale(scale);
        mMaxScale = getMaxScale(scale);
        mScaledFlag = false;
    }

    protected abstract float adjustInitScale(AreaData viewArea, AreaData bitmapArea);

    protected abstract float getInitX(AreaData viewArea, AreaData bitmapArea, float scale);

    protected abstract float getInitY(AreaData viewArea, AreaData bitmapArea, float scale);

    protected float getMinScale(float scale) {
        return scale;
    }

    protected float getMaxScale(float scale) {
        return scale * 5.0f;
    }

    @Override
    public boolean isScaled() {
        return mScaledFlag;
    }

    @Override
    protected boolean isImageArea(float x, float y) {
        RectF rect = AreaMatrixUtil.getMatrixRect(mMatrix, mBitmapArea);
        return rect.contains(x, y);
    }

    @Override
    public Matrix resetScale() {
        mScaledFlag = false;
        mMatrix = new Matrix(mInitMatrix);
        return mMatrix;
    }

    public float cleanUpDistance(float distance, float bitmapStart, float bitmapEnd, int viewSize) {
        final int padding = 5;
        if (bitmapEnd > viewSize) {
            if (bitmapStart > padding) {
                if (distance > 0) {
                    return 0;
                }
            }
        } else if (bitmapStart < 0) {
            if (bitmapEnd < viewSize - padding) {
                if (distance < 0) {
                    return 0;
                }
            }
        }
        return distance;
    }

    @Override
    public Matrix move(float distanceX, float distanceY) {
        RectF rect = AreaMatrixUtil.getMatrixRect(mMatrix, mBitmapArea);
        AreaMatrixUtil.moveRect(rect, distanceX, distanceY);

        distanceX = cleanUpDistance(distanceX, rect.left, rect.right, mViewArea.width);
        distanceY = cleanUpDistance(distanceY, rect.top, rect.bottom, mViewArea.height);

        mMatrix.postTranslate(distanceX, distanceY);
        return mMatrix;
    }

    private float adjustScaleFactor(float scaleFactor) {
        final float zoom = 2.0f;
        if (scaleFactor < 1.0f) {
            return 1.0f - (1.0f - scaleFactor) * zoom;
        } else {
            return 1.0f + (scaleFactor - 1.0f) * zoom;
        }
    }

    @Override
    protected Matrix commitScaleFactor(float x, float y, float scaleFactor) {
        float currentScale = AreaMatrixUtil.getMatrixValue(mMatrix, Matrix.MSCALE_Y);
        scaleFactor = adjustScaleFactor(scaleFactor);
        float scale = scaleFactor * currentScale;
        mScaledFlag = true;

        if (scale < mMinScale) {
            return resetScale();
        }
        if (scale > mMaxScale) {
            return mMatrix;
        }

        mMatrix.postScale(scaleFactor, scaleFactor, x, y);
        return mMatrix;
    }
}
