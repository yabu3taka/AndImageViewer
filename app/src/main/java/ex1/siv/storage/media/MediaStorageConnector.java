package ex1.siv.storage.media;

import android.content.Context;

import androidx.annotation.NonNull;

import ex1.siv.storage.Storage;
import ex1.siv.storage.StorageConnector;

public class MediaStorageConnector implements StorageConnector {
    @Override
    public Storage getStorage(@NonNull Context context) {
        return new MediaStorage();
    }
}
