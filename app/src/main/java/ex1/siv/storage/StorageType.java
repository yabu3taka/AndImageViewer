package ex1.siv.storage;

import android.net.Uri;

import ex1.siv.storage.drive.DriveStorage;
import ex1.siv.storage.drive.DriveStorageConnector;
import ex1.siv.storage.media.MediaStorage;
import ex1.siv.storage.media.MediaStorageConnector;

public class StorageType {
    public final static int STORAGE_LOCAL = 0;
    public final static int STORAGE_DRIVE = 1;

    private final int type;

    public StorageType(int type) {
        this.type = type;
    }

    public static StorageType createFactoryFrom(Uri uri) {
        if (DriveStorage.isGoogleDrive(uri)) {
            return new StorageType(STORAGE_DRIVE);
        } else {
            return new StorageType(STORAGE_LOCAL);
        }
    }

    public StorageConnector createConnector() {
        if (type == STORAGE_DRIVE) {
            return new DriveStorageConnector();
        } else {
            return new MediaStorageConnector();
        }
    }

    public boolean isMime(Storage s) {
        if (type == STORAGE_DRIVE) {
            return s instanceof DriveStorage;
        } else {
            return s instanceof MediaStorage;
        }
    }
}
