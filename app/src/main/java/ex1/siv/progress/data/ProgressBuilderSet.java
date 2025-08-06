package ex1.siv.progress.data;

import androidx.work.Data;

import java.util.ArrayList;
import java.util.List;

public class ProgressBuilderSet {
    private final List<ProgressAspectBuilder> aspects = new ArrayList<>();
    private int what;

    public ProgressBuilderSet(int what) {
        this.what = what;
    }
    public ProgressBuilderSet() {
        this.what = 0;
    }

    public void setWhat(int what) {
        this.what = what;
    }

    public <T extends ProgressAspectBuilder> T makeAndAddAspect(T aspect) {
        aspects.add(aspect);
        return aspect;
    }

    public Data createDataForProgress(ProgressBuilder builder) {
        Data.Builder dataBuilder = new Data.Builder();
        builder.setup(dataBuilder);
        dataBuilder.putInt(ProgressData.FIELD_WHAT, what);
        for (ProgressAspectBuilder aspect : aspects) {
            aspect.setupForProgress(dataBuilder);
        }
        return dataBuilder.build();
    }

    public Data createDataForResult(ProgressBuilder builder) {
        Data.Builder dataBuilder = new Data.Builder();
        builder.setup(dataBuilder);
        dataBuilder.putInt(ProgressData.FIELD_WHAT, what);
        for (ProgressAspectBuilder aspect : aspects) {
            aspect.setupForResult(builder.getStatus(), dataBuilder);
        }
        return dataBuilder.build();
    }

    public static Data createDateSimply(ProgressBuilder builder) {
        Data.Builder dataBuilder = new Data.Builder();
        builder.setup(dataBuilder);
        return dataBuilder.build();
    }

    public static Data createDateForKeep(ProgressBuilder builder, ProgressData progressData) {
        Data.Builder dataBuilder = new Data.Builder();
        builder.setup(dataBuilder);
        ProgressAspectRestUri.keep(dataBuilder, progressData);
        return dataBuilder.build();
    }
}
