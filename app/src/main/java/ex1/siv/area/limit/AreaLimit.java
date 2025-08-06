package ex1.siv.area.limit;

import ex1.siv.area.AreaData;

public interface AreaLimit {
    boolean setWidth(AreaData area, int newWidth);
    boolean setHeight(AreaData area, int newHeight);
}
