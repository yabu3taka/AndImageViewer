package ex1.siv.ui;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;

import ex1.siv.storage.Storage;
import ex1.siv.storage.StorageConnector;
import ex1.siv.storage.StorageType;

public class StorageViewModel extends AndroidViewModel {
    private Storage mStorage = null;

    protected StorageViewModel(Application a) {
        super(a);
    }

    protected Storage getStorage(StorageType t) {
        Storage cur = mStorage;
        if (!t.isMime(cur)) {
            StorageConnector con = t.createConnector();
            cur = con.getStorage(getApplication());
            mStorage = cur;
        }
        return cur;
    }
}
