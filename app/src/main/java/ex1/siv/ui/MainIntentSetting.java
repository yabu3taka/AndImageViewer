package ex1.siv.ui;

import android.app.Activity;
import android.content.Intent;

public class MainIntentSetting {
    private final static String SCRAMBLE_FIELD = "scramble";

    public static int getScramble(Intent intent) {
        return intent.getIntExtra(SCRAMBLE_FIELD, 0);
    }

    public static void setScramble(Intent intent, int s) {
        intent.putExtra(SCRAMBLE_FIELD, s);
    }

    public static void copy(Intent intent, Activity a) {
        setScramble(intent, getScramble(a.getIntent()));
    }
}
