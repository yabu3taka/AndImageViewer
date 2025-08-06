package ex1.siv.ui.setting;

import android.view.MotionEvent;
import android.view.View;

import ex1.siv.area.AreaData;

public class NaviSetting {
    private final AreaData area;
    private final PosType nextPos;
    private final PosType nextFlick;
    private final int percent;

    NaviSetting(AreaData area, PosType nextPos, int percent, PosType nextFlick) {
        this.area = area;
        this.nextPos = nextPos;
        this.percent = percent;
        this.nextFlick = nextFlick;
    }

    private boolean isLeftNaviArea(MotionEvent e) {
        return e.getX() < (area.width * percent / 100.0);
    }

    private boolean isRightNaviArea(MotionEvent e) {
        return e.getX() > (area.width * (100 - percent) / 100.0);
    }

    public boolean isNaviArea(MotionEvent e) {
        if (isLeftNaviArea(e)) {
            return true;
        }
        return isRightNaviArea(e);
    }

    public int getDirection(MotionEvent e) {
        int dir = isLeftNaviArea(e) ? -1 : 1;
        if (nextPos.isRight()) {
            return dir;
        } else {
            return -dir;
        }
    }

    public int getDirectionByVelocity(float velocityX, float velocityY) {
        if (Math.abs(velocityX) < Math.abs(velocityY)) {
            return 0;
        }
        if (nextFlick.isRight()) {
            return velocityX > 0 ? 1 : -1;
        } else {
            return velocityX > 0 ? -1 : 1;
        }
    }

    /** @noinspection unused*/
    public boolean isDown(MotionEvent e) {
        return e.getY() < (area.height / 2.0);
    }

    public void setupTextZone(View v) {
        v.getLayoutParams().width = area.calcPercentWidth(80);
        v.getLayoutParams().height = area.calcPercentHeight(60);
    }
}
