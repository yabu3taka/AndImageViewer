package ex1.siv.area.matrix;

import ex1.siv.area.AreaData;

public class AreaMatrixCreatorCenter extends AreaMatrixCreatorBase {
    public AreaMatrixCreatorCenter(AreaData viewArea, AreaData bitmapArea) {
        super(viewArea, bitmapArea);
    }

    @Override
    protected float adjustInitScale(AreaData viewArea, AreaData bitmapArea) {
        return bitmapArea.getFitScale(viewArea);
    }

    @Override
    protected float getInitX(AreaData viewArea, AreaData bitmapArea, float scale) {
        return Math.round((viewArea.width - bitmapArea.width * scale) * 0.5f);
    }

    @Override
    protected float getInitY(AreaData viewArea, AreaData bitmapArea, float scale) {
        return Math.round((viewArea.height - bitmapArea.height * scale) * 0.5f);
    }

    @Override
    public AreaMatrixCreator createNewInstanceFromViewArea(AreaData viewArea) {
        return new AreaMatrixCreatorCenter(viewArea, mBitmapArea);
    }
}
