package ex1.siv.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import ex1.siv.R;
import ex1.siv.file.FileController;
import ex1.siv.util.ShowUtil;

public class InputDialogFragment extends DialogFragment {
    private final static String TAG = InputDialogFragment.class.getSimpleName();

    public interface OnAnswerListener {
        boolean validateInput(Context c, Bundle args, String str);
        void onAnswerClick(int questionId, int which, String str);
    }

    /*********************************************************************
     * Builder
     *********************************************************************/
    private final static String FIELD_TEXT = "text";

    public static class Builder extends DialogCommonBuilder {
        private Builder(int questionId) {
            super(questionId);
        }

        public void setInputText(String str) {
            mArgs.putString(FIELD_TEXT, str);
        }

        public void setRenameTarget(FileController targetFile) {
            setInputText(targetFile.getName());
        }

        public InputDialogFragment create() {
            InputDialogFragment dialogFragment = new InputDialogFragment();
            dialogFragment.setArguments(mArgs);
            return dialogFragment;
        }
    }

    public static Builder builder(int questionId, String title) {
        Builder ret = new Builder(questionId);
        ret.setTitle(title);
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
        final Bundle args = requireArguments();
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());

        final EditText editText = new EditText(getContext());
        final String origStr = args.getString(FIELD_TEXT, "");
        editText.setText(origStr);

        DialogCommonBuilder.setupDialogBuilder(builder, args, null);
        builder.setView(editText);

        final int questionId = DialogCommonBuilder.getQuestionId(args);
        DialogCommonBuilder.OnValidationListener validationListener = (dialog, which) -> {
            String str = editText.getText().toString();
            if (which == DialogInterface.BUTTON_POSITIVE) {
                boolean ret = mCallback.validateInput(requireContext(), args, str);
                Log.i(TAG, "validateInput Id=" + questionId + " W=" + which + " N=" + str + " R=" + ret);
                return ret;
            }
            return true;
        };
        DialogInterface.OnClickListener clickListener = (dialog, which) -> {
            String str = editText.getText().toString();
            Log.i(TAG, "onAnswerClick Id=" + questionId + " W=" + which + " N=" + str);
            mCallback.onAnswerClick(questionId, which, str);
        };

        AlertDialog alertDialog = builder.create();
        DialogCommonBuilder.setupDialogListener(alertDialog, args, validationListener, clickListener);
        return alertDialog;
    }

    public static boolean validateNewName(Context c, Bundle args, String str) {
        final String origStr = args.getString(FIELD_TEXT, "");
        if (origStr.equals(str)) {
            ShowUtil.showUserError(c, R.string.rename_same);
            return false;
        }
        return true;
    }

    public static boolean validateDuplication(Context c, FileController file, String str) {
        if (file.getChild(str) != null) {
            ShowUtil.showUserError(c, R.string.rename_exist);
            return false;
        }
        return true;
    }
}
