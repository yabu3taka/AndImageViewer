package ex1.siv.progress.data;

import androidx.work.Data;

public class ProgressSecondary extends ProgressSingle {
    private static final String FIELD_SEC_TARGET = "sec-target";
    private static final String FIELD_SEC_CURRENT = "sec-current";
    private static final String FIELD_SEC_SIZE = "sec-size";

    public final int secondaryCurrent;
    public final int secondarySize;
    public final String secondaryTarget;

    private ProgressSecondary(ProgressData progressData) {
        super(progressData);
        this.secondaryCurrent = progressData.data.getInt(FIELD_SEC_CURRENT, 0);
        this.secondarySize = progressData.data.getInt(FIELD_SEC_SIZE, 1);
        this.secondaryTarget = progressData.data.getString(FIELD_SEC_TARGET);
    }

    public static ProgressSecondary create(ProgressData progressData) {
        if (progressData.data.hasKeyWithValueOfType(FIELD_SEC_CURRENT, Integer.class)) {
            return new ProgressSecondary(progressData);
        }
        return null;
    }

    public final int getSecondaryProgress() {
        return 100 * secondaryCurrent / secondarySize;
    }

    /*********************************************************************
     * Data Builder
     ********************************************************************/
    public static class Builder implements ProgressBuilder {
        private final ProgressSingle.Builder parentData;
        private int pos = 0;
        private String target;
        private final int size;

        public Builder(ProgressSingle.Builder parentData, int size) {
            this.parentData = new ProgressSingle.Builder(parentData);
            this.size = size;
        }

        public Builder done(String target) {
            pos++;
            this.target = target;
            return this;
        }

        public Status getStatus() {
            return parentData.getStatus();
        }

        public void setup(Data.Builder builder) {
            parentData.setup(builder);
            builder.putString(FIELD_SEC_TARGET, target)
                    .putInt(FIELD_SEC_CURRENT, pos)
                    .putInt(FIELD_SEC_SIZE, size);
        }
    }
}
