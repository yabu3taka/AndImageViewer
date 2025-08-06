package ex1.siv.area.limit;

import ex1.siv.area.AreaData;

/** @noinspection unused*/
public class AreaLimitMax implements AreaLimit {
    public final int maxWidth;
    public final int maxHeight;

    public AreaLimitMax(int maxWidth, int maxHeight) {
        this.maxWidth = maxWidth;
        this.maxHeight = maxHeight;
    }

    @Override
    public boolean setWidth(AreaData area, int newWidth) {
        boolean valid;
        if (maxWidth <= 0) {
            valid = true;
        } else {
            valid = newWidth <= maxWidth;
        }
        if (valid) {
            area.width = newWidth;
        }
        return valid;
    }

    @Override
    public boolean setHeight(AreaData area, int newHeight) {
        boolean valid;
        if (maxHeight <= 0) {
            valid = true;
        } else {
            valid = newHeight <= maxHeight;
        }
        if (valid) {
            area.height = newHeight;
        }
        return valid;
    }
}
