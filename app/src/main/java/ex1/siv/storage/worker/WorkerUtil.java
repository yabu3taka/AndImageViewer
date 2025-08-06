package ex1.siv.storage.worker;

import android.content.Context;

import ex1.siv.storage.Storage;
import ex1.siv.storage.StorageConnector;
import ex1.siv.ui.setting.StorageFolderSetting;

public class WorkerUtil {
    private WorkerUtil() {
    }

    public static Storage getStorageFromPreferences(Context c) {
        StorageFolderSetting p = new StorageFolderSetting(c);
        StorageConnector con = p.getStorageFactory().createConnector();
        return con.getStorage(c);
    }
}
