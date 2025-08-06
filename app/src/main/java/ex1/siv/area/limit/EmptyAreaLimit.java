package ex1.siv.area.limit;

import ex1.siv.area.AreaData;

public class EmptyAreaLimit implements AreaLimit {
    @Override
    public boolean setWidth(AreaData area, int newWidth) {
        area.width = newWidth;
        return true;
    }

    @Override
    public boolean setHeight(AreaData area, int newHeight) {
        area.height = newHeight;
        return true;
    }
}
