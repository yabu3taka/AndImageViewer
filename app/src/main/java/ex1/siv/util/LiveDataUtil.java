package ex1.siv.util;

import androidx.lifecycle.LiveData;

public class LiveDataUtil {
    private LiveDataUtil() {
    }

    public static boolean getBoolean(LiveData<Boolean> ld) {
        Boolean v = ld.getValue();
        if (v == null) {
            return false;
        }
        return v;
    }
}
