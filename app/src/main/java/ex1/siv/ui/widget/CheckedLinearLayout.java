package ex1.siv.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.Checkable;

public class CheckedLinearLayout extends LinearLayout implements Checkable {
    private boolean mChecked;

    private static final int[] CHECKED_STATE_SET = {android.R.attr.state_checked};

    public CheckedLinearLayout(Context context) {
        this(context, null);
    }

    public CheckedLinearLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CheckedLinearLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void toggle() {
        setChecked(!mChecked);
    }

    @Override
    public boolean isChecked() {
        return mChecked;
    }

    @Override
    public void setChecked(boolean checked) {
        if (mChecked != checked) {
            mChecked = checked;
            refreshDrawableState();
        }
    }

    @Override
    protected int[] onCreateDrawableState(int extraSpace) {
        final int[] drawableState = super.onCreateDrawableState(extraSpace + 1);
        if (isChecked()) {
            mergeDrawableStates(drawableState, CHECKED_STATE_SET);
        }
        return drawableState;
    }
}
