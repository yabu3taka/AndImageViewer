package ex1.siv.storage.cache;

import ex1.siv.storage.local.LocalFolderInfo;

public class CacheFolderInfo extends LocalFolderInfo {
    private final boolean mDone;

    CacheFolderInfo(LocalFolderInfo folder) {
        super(folder.toFile());

        mDone = CacheDownloaderSimple.getDoneFlagFile(target).exists();
    }

    /** @noinspection BooleanMethodIsAlwaysInverted*/
    public boolean isDone() {
        return mDone;
    }
}
