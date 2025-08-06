package ex1.siv.ui.setting;

import android.view.View;
import android.widget.FrameLayout;

public class SideListSetting {
    private final PosType sidePos;

    SideListSetting(PosType sidePos) {
        this.sidePos = sidePos;
    }

    public void setupSideList(View v) {
        ((FrameLayout.LayoutParams) v.getLayoutParams()).gravity = sidePos.getGravity();
    }
}
