package ex1.siv.ui.setting;

import android.annotation.SuppressLint;
import android.view.Gravity;

import java.util.Objects;

public enum PosType {
    @SuppressLint("RtlHardcoded") RIGHT("R", Gravity.RIGHT),
    @SuppressLint("RtlHardcoded") LEFT("L", Gravity.LEFT);

    private final String type;
    private final int gravity;

    PosType(String type, int gravity) {
        this.type = type;
        this.gravity = gravity;
    }

    public String getType() {
        return type;
    }

    public int getGravity() {
        return gravity;
    }

    public boolean isRight() {
        return Objects.equals(type, "R");
    }

    public static PosType find(String type) {
        return find(type, RIGHT);
    }

    public static PosType find(String type, PosType def) {
        for (PosType t : values()) {
            if (Objects.equals(t.type, type)) {
                return t;
            }
        }
        return def;
    }
}
