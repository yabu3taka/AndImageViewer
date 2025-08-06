package ex1.siv.util;

import android.app.Activity;
import android.view.View;

public class UserInterfaceUtil {
    private UserInterfaceUtil() {
    }

    public static void setVisibility(View v, int visibility) {
        if (v != null) {
            v.setVisibility(visibility);
        }
    }

    public static void setGone(View v, boolean gone) {
        if (v != null) {
            v.setVisibility(gone ? View.GONE : View.VISIBLE);
        }
    }

    public static void setGone(Activity a, int id, boolean gone) {
        a.findViewById(id).setVisibility(gone ? View.GONE : View.VISIBLE);
    }
}
