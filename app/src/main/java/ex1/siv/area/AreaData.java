package ex1.siv.area;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.view.View;
import android.view.WindowMetrics;
import android.widget.Adapter;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;

import ex1.siv.area.limit.AreaLimit;
import ex1.siv.area.limit.EmptyAreaLimit;

/** @noinspection UnusedReturnValue, unused */
public class AreaData {
    public int width = 0;
    public int height = 0;

    public AreaData() {
    }

    public AreaData(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public AreaData(Bitmap b) {
        this(b.getWidth(), b.getHeight());
    }

    public AreaData(View v) {
        this(v.getWidth(), v.getHeight());
    }

    public static AreaData getArea(WindowMetrics display) {
        Rect size = display.getBounds();
        return new AreaData(size.width(), size.height());
    }

    public static AreaData getListViewArea(Context context, Adapter adapter, AreaLimit limit) {
        AreaData area = new AreaData();
        View view = null;
        FrameLayout fakeParent = new FrameLayout(context);
        for (int i = 0, count = adapter.getCount(); i < count; i++) {
            view = adapter.getView(i, view, fakeParent);
            view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
            area.maxWidth(view.getMeasuredWidth(), limit);
            area.addHeight(view.getMeasuredHeight(), limit);
        }
        return area;
    }

    public static AreaData getListViewArea(Context context, Adapter adapter) {
        return getListViewArea(context, adapter, new EmptyAreaLimit());
    }

    @NonNull
    @Override
    public String toString() {
        return "(" + width + "," + height + ")";
    }

    /*********************************************************************
     * Ratio
     *********************************************************************/
    public float getFitScale(AreaData toArea) {
        float widthRatio = (float) toArea.width / (float) width;
        float heightRatio = (float) toArea.height / (float) height;
        return Math.min(widthRatio, heightRatio);
    }

    public AreaData scaleArea(float scale) {
        return new AreaData((int) (width * scale), (int) (height * scale));
    }

    /*********************************************************************
     * Add
     *********************************************************************/
    public boolean addWidth(int add, AreaLimit limit) {
        return limit.setWidth(this, width + add);
    }

    public boolean addHeight(int add, AreaLimit limit) {
        return limit.setHeight(this, height + add);
    }

    /*********************************************************************
     * Max
     *********************************************************************/
    public boolean maxWidth(int newWidth, AreaLimit limit) {
        if (width < newWidth) {
            return limit.setWidth(this, newWidth);
        } else {
            return true;
        }
    }

    public boolean maxHeight(int newHeight, AreaLimit limit) {
        if (height < newHeight) {
            return limit.setHeight(this, newHeight);
        } else {
            return true;
        }
    }

    /*********************************************************************
     * PreferableListViewItem
     *********************************************************************/
    public int getPreferableListViewItemWidth() {
        return (int) (width * 1.05);
    }

    public int getPreferableListViewItemHeight() {
        return (int) (height * 1.05);
    }

    /*********************************************************************
     * Percent
     *********************************************************************/
    public int calcPercentWidth(int percent) {
        return width * percent / 100;
    }

    public int calcPercentHeight(int percent) {
        return height * percent / 100;
    }
}
