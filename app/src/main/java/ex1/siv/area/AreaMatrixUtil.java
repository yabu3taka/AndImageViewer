package ex1.siv.area;

import android.graphics.Matrix;
import android.graphics.RectF;

public class AreaMatrixUtil {
    private AreaMatrixUtil() {
    }

    public static float getMatrixValue(Matrix matrix, int index) {
        float[] values = new float[9];
        matrix.getValues(values);
        return values[index];
    }

    public static RectF getMatrixRect(Matrix matrix, AreaData area) {
        float[] values = new float[9];
        matrix.getValues(values);

        float x = values[Matrix.MTRANS_X];
        float y = values[Matrix.MTRANS_Y];
        float width = area.width * values[Matrix.MSCALE_X];
        float height = area.height * values[Matrix.MSCALE_Y];
        return new RectF(x, y, x + width, y + height);
    }

    public static void moveRect(RectF rect, float distanceX, float distanceY) {
        rect.left += distanceX;
        rect.right += distanceX;

        rect.top += distanceY;
        rect.bottom += distanceY;
    }
}
