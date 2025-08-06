package ex1.siv.util;

import android.content.Context;
import android.widget.Toast;

import ex1.siv.storage.exception.StorageStrIdException;

public class ShowUtil {
    private ShowUtil() {
    }

    /** @noinspection SameParameterValue*/
    private static void showToast(Context context, String mess, int length) {
        Toast toast = Toast.makeText(context, mess, length);
        toast.show();
    }

    public static void showException(Context context, Exception ex) {
        String mess;
        if (ex instanceof StorageStrIdException) {
            mess = context.getString(((StorageStrIdException) ex).strId);
        } else {
            mess = ex.getMessage();
        }
        showToast(context, mess, Toast.LENGTH_SHORT);
    }

    public static void showInternalError(Context context, String mess) {
        showToast(context, mess, Toast.LENGTH_SHORT);
    }

    public static void showUserError(Context context, String mess) {
        showToast(context, mess, Toast.LENGTH_SHORT);
    }

    public static void showUserError(Context context, int strId) {
        showUserError(context, context.getString(strId));
    }

    public static void showUserInfo(Context context, String mess) {
        showToast(context, mess, Toast.LENGTH_SHORT);
    }
}
