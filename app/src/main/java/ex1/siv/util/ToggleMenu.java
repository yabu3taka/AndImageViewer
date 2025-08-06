package ex1.siv.util;

import android.view.Menu;

public class ToggleMenu {
    private final Menu menu;
    private final int startMenuId;
    private final int stopMenuId;

    public ToggleMenu(Menu menu, int startMenuId, int stopMenuId) {
        this.menu = menu;
        this.startMenuId = startMenuId;
        this.stopMenuId = stopMenuId;
    }

    public void setRunning(boolean b) {
        menu.findItem(startMenuId).setVisible(!b);
        menu.findItem(stopMenuId).setVisible(b);
    }

    public void setAllOff() {
        menu.findItem(startMenuId).setVisible(false);
        menu.findItem(stopMenuId).setVisible(false);
    }
}
