package ex1.siv.ui.dir;

import android.app.Application;
import android.net.Uri;

import androidx.lifecycle.LiveData;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import ex1.siv.room.AppDatabase;
import ex1.siv.room.DbConnection;
import ex1.siv.room.folder.FolderProp;
import ex1.siv.storage.StorageContext;
import ex1.siv.storage.Storage;
import ex1.siv.storage.StorageType;
import ex1.siv.storage.cache.CacheFolderInfo;
import ex1.siv.storage.data.FolderImgAndText;
import ex1.siv.storage.favorite.FavoriteManager;
import ex1.siv.storage.task.EmptyTask;
import ex1.siv.storage.task.ResultTask;
import ex1.siv.storage.trans.LiveDataTrans;
import ex1.siv.storage.cache.CacheManager;
import ex1.siv.storage.data.FolderInfo;
import ex1.siv.ui.StorageViewModel;
import ex1.siv.util.BitmapUtil;

public class DirViewModel extends StorageViewModel {
    public DirViewModel(Application a) {
        super(a);
        initDbConnector(a);
    }

    /*********************************************************************
     * FolderList
     *********************************************************************/
    private final LiveDataTrans<Uri, List<FolderInfo>> folderListLD = new LiveDataTrans<>();

    LiveData<List<FolderInfo>> getFolderList() {
        return folderListLD.getLiveData();
    }

    private Storage mStorage;
    Storage getStorage() {
        return mStorage;
    }

    private FavoriteManager mFavoriteManager;
    FavoriteManager getFavoriteManager() {
        return mFavoriteManager;
    }

    void loadFolderList(StorageContext context, Uri dir) {
        mStorage = getStorage(StorageType.createFactoryFrom(dir));
        mFavoriteManager = mStorage.getFavoriteManager(context);
        mFavoriteManager.loadFavorite();

        initAboutCache(context);

        if (folderListLD.isSameTarget(dir)) {
            return;
        }
        folderListLD.startTask(context, dir, mStorage.loadFolderList(context, mStorage.getFromUri(context, dir)));
    }

    void forceReload() {
        folderListLD.clearParam();
    }

    /*********************************************************************
     * FolderInfo
     *********************************************************************/
    private AppDatabase mDbConnector;

    private void initDbConnector(Application a) {
        mDbConnector = DbConnection.connect(a);
    }

    private final LiveDataTrans<FolderInfo, FolderImgAndText> folderInfoLD = new LiveDataTrans<>();
    private ResultTask mFolderInfoTask = new EmptyTask();

    LiveData<FolderImgAndText> getFolderInfo() {
        return folderInfoLD.getLiveData();
    }

    void loadFolderInfo(StorageContext context, FolderInfo folder) {
        if (folderInfoLD.isSameTarget(folder)) {
            return;
        }
        BitmapUtil.scrambled = mScramble;

        FolderProp folderProp = mDbConnector.folderPropDao().findById(folder.filename);
        String imgName = "";
        if (folderProp != null) {
            imgName = folderProp.currentFile;
        }

        mFolderInfoTask.cancelMe(context);
        mFolderInfoTask = folderInfoLD.startTask(context, folder, mStorage.getFolderImgAndText(context, folder, imgName));
    }

    /*********************************************************************
     * CacheManager
     *********************************************************************/
    private boolean mCacheOk = false;
    boolean isCacheOk() {
        return mCacheOk;
    }

    private CacheManager mCacheManager;
    CacheManager getCacheManager() {
        return mCacheManager;
    }

    private void initAboutCache(StorageContext context) {
        mCacheOk = CacheManager.isCacheOk(mStorage);
        mCacheManager = new CacheManager(context);
    }

    List<CacheFolderInfo> getCacheFolderList() {
        return mCacheManager.getCacheFolderList();
    }

    List<FolderInfo> appendCacheFolderInfo(List<FolderInfo> result) {
        if (!mCacheOk) {
            return result;
        }

        HashSet<String> resultMap = new HashSet<>();
        for (FolderInfo folder : result) {
            resultMap.add(folder.filename);
        }

        List<FolderInfo> ret = new ArrayList<>(result);
        for (CacheFolderInfo folder : getCacheFolderList()) {
            if (!resultMap.contains(folder.filename)) {
                ret.add(folder);
            }
        }
        return ret;
    }

    /*********************************************************************
     * Setting
     *********************************************************************/
    private int mScramble = 0;

    void setScramble(int s) {
        mScramble = s;
    }
}
