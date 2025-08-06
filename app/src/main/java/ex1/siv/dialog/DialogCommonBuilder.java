package ex1.siv.dialog;

import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AlertDialog;

public class DialogCommonBuilder {
    private final static String FIELD_ID = "0id";
    private final static String FIELD_TITLE = "0title";
    private final static String FIELD_MESS = "0mess";
    private final static String FIELD_OK = "0ok";
    private final static String FIELD_CANCEL = "0cancel";
    private final static String FIELD_NEUTRAL = "0neutral";

    protected final Bundle mArgs = new Bundle();

    protected DialogCommonBuilder(int id) {
        mArgs.putInt(FIELD_ID, id);
    }

    public void setTitle(String str) {
        mArgs.putString(FIELD_TITLE, str);
    }

    public void setText(String str) {
        mArgs.putString(FIELD_MESS, str);
    }

    public void setOk(String str) {
        mArgs.putString(FIELD_OK, str);
    }

    /** @noinspection unused*/
    public void setNeutral(String str) {
        mArgs.putString(FIELD_NEUTRAL, str);
    }

    public void setCancel(String str) {
        mArgs.putString(FIELD_CANCEL, str);
    }

    public static int getQuestionId(Bundle args) {
        return args.getInt(FIELD_ID);
    }

    public static void setupDialogBuilder(AlertDialog.Builder builder,
                                          Bundle args,
                                          DialogInterface.OnClickListener listener) {
        builder.setTitle(args.getString(FIELD_TITLE, ""));
        builder.setMessage(args.getString(FIELD_MESS, ""));

        boolean hasButton = false;
        if (args.containsKey(FIELD_OK)) {
            builder.setPositiveButton(args.getString(FIELD_OK), listener);
            hasButton = true;
        }
        if (args.containsKey(FIELD_NEUTRAL)) {
            builder.setNeutralButton(args.getString(FIELD_NEUTRAL), listener);
            hasButton = true;
        }
        if (args.containsKey(FIELD_CANCEL)) {
            builder.setNegativeButton(args.getString(FIELD_CANCEL), listener);
            hasButton = true;
        }
        if (!hasButton) {
            builder.setPositiveButton(android.R.string.ok, listener);
        }
    }

    public interface OnValidationListener {
        /** @noinspection unused*/
        boolean onValidation(DialogInterface dialog, int which);
    }

    private static class MyOnShowListener implements DialogInterface.OnShowListener, View.OnClickListener {
        private final static String TAG = MyOnShowListener.class.getSimpleName();

        private AlertDialog dialog;
        private boolean okFlg = false;
        private boolean cancelFlg = false;
        private boolean neutralFlg = false;
        private final OnValidationListener validationListener;
        private final DialogInterface.OnClickListener clickListener;

        public MyOnShowListener(Bundle args,
                                OnValidationListener validationListener,
                                DialogInterface.OnClickListener clickListener) {
            setupFlag(args);
            this.validationListener = validationListener;
            this.clickListener = clickListener;
        }

        private void setupFlag(Bundle args) {
            boolean hasButton = false;
            if (args.containsKey(FIELD_OK)) {
                okFlg = true;
                hasButton = true;
            }
            if (args.containsKey(FIELD_NEUTRAL)) {
                neutralFlg = true;
                hasButton = true;
            }
            if (args.containsKey(FIELD_CANCEL)) {
                cancelFlg = true;
                hasButton = true;
            }
            if (!hasButton) {
                okFlg = true;
            }
        }

        @Override
        public void onShow(DialogInterface d) {
            dialog = (AlertDialog) d;
            if (okFlg) {
                dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(this);
            }
            if (cancelFlg) {
                dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setOnClickListener(this);
            }
            if (neutralFlg) {
                dialog.getButton(DialogInterface.BUTTON_NEUTRAL).setOnClickListener(this);
            }
        }

        private int getWhich(View v) {
            int itemId = v.getId();
            int[] types = {DialogInterface.BUTTON_POSITIVE,
                    DialogInterface.BUTTON_NEGATIVE,
                    DialogInterface.BUTTON_NEUTRAL};
            for (int type : types) {
                if (itemId == dialog.getButton(type).getId()) {
                    return type;
                }
            }
            return DialogInterface.BUTTON_NEGATIVE;
        }

        @Override
        public void onClick(View v) {
            int which = getWhich(v);
            Log.i(TAG, "onClick W=" + which);
            if (which == DialogInterface.BUTTON_POSITIVE) {
                if (this.validationListener.onValidation(dialog, which)) {
                    clickListener.onClick(dialog, which);
                    dialog.dismiss();
                }
            } else {
                clickListener.onClick(dialog, which);
                dialog.dismiss();
            }
        }
    }

    public static void setupDialogListener(AlertDialog dialog,
                                           Bundle args,
                                           OnValidationListener validationListener,
                                           DialogInterface.OnClickListener clickListener) {
        dialog.setOnShowListener(new MyOnShowListener(args, validationListener, clickListener));
    }
}