package ex1.siv.dialog;

import androidx.annotation.NonNull;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

public class YesNoDialogFragment extends DialogFragment {
    private final static String TAG = YesNoDialogFragment.class.getSimpleName();

    public interface OnAnswerListener {
        void onAnswerClick(int questionId, int which);
    }

    /*********************************************************************
     * Builder
     *********************************************************************/
    public static class Builder extends DialogCommonBuilder {
        private Builder(int questionId) {
            super(questionId);
        }

        public YesNoDialogFragment create() {
            YesNoDialogFragment dialogFragment = new YesNoDialogFragment();
            dialogFragment.setArguments(mArgs);
            return dialogFragment;
        }
    }

    public static Builder builder(int questionId, String title, String mess) {
        Builder ret = new Builder(questionId);
        ret.setTitle(title);
        ret.setText(mess);
        return ret;
    }

    /*********************************************************************
     * As DialogFragment
     *********************************************************************/
    private OnAnswerListener mCallback = null;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mCallback = (OnAnswerListener) context;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle args = requireArguments();
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());

        final int questionId = DialogCommonBuilder.getQuestionId(args);
        DialogInterface.OnClickListener listener = (dialog, which) -> {
            Log.i(TAG, "onAnswerClick Id=" + questionId + " W=" + which);
            mCallback.onAnswerClick(questionId, which);
        };

        DialogCommonBuilder.setupDialogBuilder(builder, args, listener);
        return builder.create();
    }
}
