package ex1.siv.storage;

import android.content.Context;

import androidx.annotation.NonNull;

public interface StorageConnector {
    Storage getStorage(@NonNull Context context);
}
