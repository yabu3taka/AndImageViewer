package ex1.siv.storage.cache;

import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import ex1.siv.storage.StorageContext;
import ex1.siv.storage.exception.StorageException;
import ex1.siv.storage.data.FolderInfo;
import ex1.siv.storage.Storage;
import ex1.siv.storage.favorite.FavoriteSaver;
import ex1.siv.storage.local.LocalFolderInfo;
import ex1.siv.storage.local.LocalStorage;
import ex1.siv.util.FileUtil;

/**
 * キャッシュフォルダーを管理する
 * 複数のフォルダを管理する
 */
public class CacheManager implements FavoriteSaver {
    private final static String TAG = CacheManager.class.getSimpleName();

    public final File topDir;

    public CacheManager(File dir) {
        topDir = dir;
    }

    public CacheManager(StorageContext context) {
        topDir = context.getMyContext().getExternalFilesDir(null);
    }

    private File getCacheFolderInternal(String filename) {
        return new File(topDir, filename);
    }

    public File getCacheFolder(FolderInfo folder) {
        return getCacheFolderInternal(folder.filename);
    }

    public boolean hasCacheFolder(FolderInfo folder) {
        return getCacheFolder(folder).exists();
    }

    public static boolean isCacheOk(Storage storage) {
        return storage instanceof CacheStorage;
    }

    public List<CacheFolderInfo> getCacheFolderList() {
        List<CacheFolderInfo> ret = new ArrayList<>();
        for (FolderInfo folder : LocalStorage.getFolderListQuickly(topDir)) {
            ret.add(new CacheFolderInfo((LocalFolderInfo) folder));
        }
        return ret;
    }

    /*********************************************************************
     * FavoriteSaver
     *********************************************************************/
    @Override
    public void saveFavorite(Set<String> needs) {
        Log.i(TAG, "saveFavorite F=" + topDir);

        File[] files = FileUtil.getUnnecessaryFolder(topDir, needs);
        if (!FileUtil.delete(files)) {
            throw new StorageException("delete folder failed");
        }

        for (String folder : needs) {
            if (!FileUtil.existsOrMake(getCacheFolderInternal(folder))) {
                throw new StorageException("make folder failed");
            }
        }

        Log.i(TAG, "saveFavorite end");
    }

    @Override
    public void loadFavorite(Set<String> map) {
        for (FolderInfo folder : LocalStorage.getFolderListQuickly(topDir)) {
            map.add(folder.filename);
        }
    }
}
