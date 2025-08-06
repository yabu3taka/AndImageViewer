package ex1.siv.ui.setting;

import android.content.Context;
import android.content.SharedPreferences;

import ex1.siv.storage.StorageType;

public class StorageFolderSetting {
    private final static String STORAGE_FIELD = "storage";
    private final static String DRIVE_DIR_FIELD = "drive";
    private final static String LOCAL_DIR_FIELD = "dir";

    private final SharedPreferences mPref;

    public StorageFolderSetting(Context context) {
        mPref = context.getSharedPreferences("dirsetting", Context.MODE_PRIVATE);
    }

    public SharedPreferences me() {
        return mPref;
    }

    public int getStorageType() {
        return mPref.getInt(STORAGE_FIELD, StorageType.STORAGE_LOCAL);
    }

    public StorageType getStorageFactory() {
        return new StorageType(getStorageType());
    }

    public String getDriveTopDir() {
        return mPref.getString(DRIVE_DIR_FIELD, "");
    }

    public String getLocalTopDir() {
        return mPref.getString(LOCAL_DIR_FIELD, "");
    }

    public static void setStorageType(SharedPreferences.Editor e, int type) {
        e.putInt(STORAGE_FIELD, type);
    }

    public static void setDriveTopDir(SharedPreferences.Editor e, String dir) {
        e.putString(DRIVE_DIR_FIELD, dir);
    }

    public static void setLocalTopDir(SharedPreferences.Editor e, String dir) {
        e.putString(LOCAL_DIR_FIELD, dir);
    }
}
