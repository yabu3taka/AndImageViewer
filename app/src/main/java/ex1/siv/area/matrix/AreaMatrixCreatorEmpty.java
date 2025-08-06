package ex1.siv.area.matrix;

import android.graphics.Matrix;

import ex1.siv.area.AreaData;

public class AreaMatrixCreatorEmpty extends AreaMatrixCreator {
    @Override
    public boolean isScaled() {
        return false;
    }

    @Override
    protected boolean isImageArea(float x, float y) {
        return false;
    }

    @Override
    public Matrix resetScale() {
        return new Matrix();
    }

    @Override
    public Matrix move(float distanceX, float distanceY) {
        return new Matrix();
    }

    @Override
    protected Matrix commitScaleFactor(float x, float y, float scaleFactor) {
        return new Matrix();
    }

    @Override
    public AreaMatrixCreator createNewInstanceFromViewArea(AreaData viewArea) {
        return new AreaMatrixCreatorEmpty();
    }
}
