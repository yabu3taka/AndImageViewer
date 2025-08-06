package ex1.siv.progress.data;

import androidx.work.Data;

public class ProgressSingle extends ProgressData {
    private static final String FIELD_TARGET = "one-target";
    private static final String FIELD_CURRENT = "one-current";
    private static final String FIELD_SIZE = "one-size";

    public final int current;
    public final int size;
    public final String target;

    protected ProgressSingle(ProgressData progressData) {
        super(progressData);
        this.current = progressData.data.getInt(FIELD_CURRENT, 0);
        this.size = progressData.data.getInt(FIELD_SIZE, 1);
        this.target = progressData.data.getString(FIELD_TARGET);
    }

    public static ProgressSingle create(ProgressData progressData) {
        if (progressData.data.hasKeyWithValueOfType(FIELD_CURRENT, Integer.class)) {
            return new ProgressSingle(progressData);
        }
        return null;
    }

    public final int getProgress() {
        return 100 * current / size;
    }

    /*********************************************************************
     * Data Builder
     ********************************************************************/
    public static class Builder extends ProgressData.Builder {
        private int pos = 0;
        private String target;
        private final int size;

        public Builder(int size) {
            super(ProgressData.Status.Running);
            this.size = size;
        }

        public Builder(Builder builder) {
            super(ProgressData.Status.Running);
            this.pos = builder.pos;
            this.target = builder.target;
            this.size = builder.size;
        }

        public Builder start(String target) {
            this.target = target;
            return this;
        }

        public Builder done(String target) {
            pos++;
            this.target = target;
            return this;
        }

        public void setup(Data.Builder builder) {
            super.setup(builder);
            builder.putString(FIELD_TARGET, target)
                    .putInt(FIELD_CURRENT, pos)
                    .putInt(FIELD_SIZE, size);
        }
    }
}
