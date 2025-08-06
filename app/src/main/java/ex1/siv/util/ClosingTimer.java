package ex1.siv.util;

public class ClosingTimer {
    private long pressedTime = 0;
    private final long limit;

    public ClosingTimer(long limit) {
        this.limit = limit;
    }

    public boolean isClosingOk() {
        long currentTime = System.currentTimeMillis();
        if ((currentTime - pressedTime) > this.limit) {
            pressedTime = currentTime;
            return false;
        }
        return true;
    }
}
