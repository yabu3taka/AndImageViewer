package ex1.siv.ui.show;

import android.app.Application;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.List;

import ex1.siv.R;
import ex1.siv.room.AppDatabase;
import ex1.siv.room.DbConnection;
import ex1.siv.room.folder.FolderProp;
import ex1.siv.storage.StorageContext;
import ex1.siv.storage.loader.ItemLoader;
import ex1.siv.storage.Storage;
import ex1.siv.storage.StorageType;
import ex1.siv.storage.data.FolderInfo;
import ex1.siv.storage.exception.StorageStrIdException;
import ex1.siv.storage.loader.ItemPreparation;
import ex1.siv.storage.loader.ItemPreparationSimple;
import ex1.siv.storage.task.FilterResultCallback;
import ex1.siv.storage.task.ResultData;
import ex1.siv.storage.trans.LiveDataTrans;
import ex1.siv.storage.task.EmptyTask;
import ex1.siv.storage.task.ResultTask;
import ex1.siv.storage.data.FileSet;
import ex1.siv.storage.data.FileSetList;
import ex1.siv.storage.trans.LoadingLiveDataTrans;
import ex1.siv.ui.StorageViewModel;
import ex1.siv.util.BitmapUtil;
import ex1.siv.util.LiveDataUtil;

public class ShowViewModel extends StorageViewModel {
    private final static String TAG = ShowViewModel.class.getSimpleName();

    public ShowViewModel(Application a) {
        super(a);
        initDbConnector(a);
    }

    /*********************************************************************
     * Setting
     *********************************************************************/
    private int mScramble = 0;

    void setScramble(int s) {
        mScramble = s;
    }

    /*********************************************************************
     * File List
     *********************************************************************/
    private final LoadingLiveDataTrans<Uri, FileSetList> fileSetListLiveData = new LoadingLiveDataTrans<>();
    private ItemLoader mItemLoader = null;

    LiveData<ResultData<FileSetList>> getFileSetList() {
        return fileSetListLiveData.getLiveData();
    }

    boolean loadFileList(StorageContext context, Uri dir) {
        Log.i(TAG, "loadFileList d=" + dir);
        if (fileSetListLiveData.isSameTarget(dir)) {
            return false;
        }
        fileSetListLiveData.startParam(dir);

        Storage storage = getStorage(StorageType.createFactoryFrom(dir));
        FolderInfo folder = storage.getFromUri(context, dir);
        loadFolderProp(folder);
        mItemLoader = storage.getItemLoader(context, folder);

        fileSetListLiveData.setFilter(new FileListFilter());
        fileSetListLiveData.startTask(context, dir, mItemLoader.loadFileList());
        return true;
    }

    private static class FileListFilter extends FilterResultCallback<FileSetList> {
        @Override
        public Exception checkResultData(ResultData<FileSetList> result) {
            assert result.result != null;
            if (result.result.count <= 0) {
                return new StorageStrIdException(R.string.err_empty);
            }
            return null;
        }
    }

    /*********************************************************************
     * Current File
     *********************************************************************/
    private final MutableLiveData<ShowFileSet> currentLiveData = new MutableLiveData<>();

    LiveData<ShowFileSet> getCurrent() {
        return currentLiveData;
    }

    ShowFileSet initCurrent() {
        if (currentLiveData.getValue() != null) {
            return currentLiveData.getValue();
        }

        FileSetList fileSetList = fileSetListLiveData.getResult();
        assert fileSetList != null;

        ShowFileSet result = new ShowFileSet(fileSetList, 0);
        if (mFolderProp.currentFile != null) {
            int pos = fileSetList.indexOf(mFolderProp.currentFile);
            Log.i(TAG, "initCurrent indexOf F=" + mFolderProp.currentFile + " P=" + pos);
            if (pos > 0) {
                result = new ShowFileSet(fileSetList, pos);
            }
        }

        Log.i(TAG, "initCurrent F=" + result.fileSet.imageFile + " P=" + result.pos);
        currentLiveData.setValue(result);
        return result;
    }

    /** @noinspection SameReturnValue*/
    boolean setCurrent(FileSet f, int pos) {
        Log.i(TAG, "setCurrent F=" + f.imageFile + " P=" + pos);
        currentLiveData.setValue(new ShowFileSet(f, pos));
        return true;
    }

    void prepareCache(ShowFileSet data) {
        FileSetList fileSetList = fileSetListLiveData.getResult();
        if (fileSetList == null) {
            Log.e(TAG, "prepareCache no fileSetList");
            return;
        }

        ItemPreparation preparation = new ItemPreparationSimple();
        preparation.prepare(mItemLoader, fileSetList, data.pos);
    }

    /*********************************************************************
     * Bitmap Load
     *********************************************************************/
    private final LiveDataTrans<FileSet, Bitmap> bitmapLiveData = new LiveDataTrans<>();
    private ResultTask mBitmapTask = new EmptyTask();

    LiveData<Bitmap> getBitmap() {
        return bitmapLiveData.getLiveData();
    }

    boolean loadBitmap(StorageContext context, FileSet f) {
        if (bitmapLiveData.isSameTarget(f)) {
            return false;
        }

        Log.i(TAG, "loadBitmap F=" + f);
        BitmapUtil.scrambled = mScramble;
        mBitmapTask.cancelMe(context);
        mBitmapTask = bitmapLiveData.startTask(context, f, mItemLoader.loadImage(f, -1, -1));
        return true;
    }

    /*********************************************************************
     * Text Load
     *********************************************************************/
    private final LiveDataTrans<FileSet, String> textLiveData = new LiveDataTrans<>();
    private ResultTask mTextTask = new EmptyTask();

    LiveData<String> getText() {
        return textLiveData.getLiveData();
    }

    void loadBitmapText(StorageContext context, FileSet f) {
        if (!isTextOpened()) {
            return;
        }
        if (textLiveData.isSameTarget(f)) {
            return;
        }

        Log.i(TAG, "loadBitmapText F=" + f);
        BitmapUtil.scrambled = mScramble;
        mTextTask.cancelMe(context);
        mTextTask = textLiveData.startTask(context, f, mItemLoader.loadText(f));
    }

    /*********************************************************************
     * Text Open
     *********************************************************************/
    private final MutableLiveData<Boolean> textOpenLiveData = new MutableLiveData<>(false);

    boolean isTextOpened() {
        return LiveDataUtil.getBoolean(textOpenLiveData);
    }

    LiveData<Boolean> getTextOpen() {
        return textOpenLiveData;
    }

    void setTextOpen(StorageContext context, boolean opened) {
        FileSetList fileSetList = fileSetListLiveData.getResult();
        if (fileSetList == null) {
            Log.e(TAG, "setTextOpen no fileSetList");
            return;
        }
        if (!fileSetList.hasText) {
            Log.i(TAG, "setTextOpen No Text");
            if (isTextOpened()) {
                textOpenLiveData.setValue(false);
            }
            return;
        }

        Log.i(TAG, "setTextOpen F=" + opened);
        textOpenLiveData.setValue(opened);

        ShowFileSet current = currentLiveData.getValue();
        if (current == null) {
            Log.e(TAG, "setTextOpen no current");
            return;
        }
        loadBitmapText(context, current.fileSet);
    }

    /*********************************************************************
     * Side List
     *********************************************************************/
    public final static int LIST_TYPE_INDEX = 0;
    /** @noinspection unused*/
    public final static int LIST_TYPE_ALL = 1;

    private final MutableLiveData<List<FileSet>> sideListLiveData = new MutableLiveData<>();
    private int mCurrentType = -1;

    LiveData<List<FileSet>> getSideList() {
        return sideListLiveData;
    }

    void setSideListType(int type) {
        FileSetList fileSetList = fileSetListLiveData.getResult();
        if (fileSetList == null) {
            Log.e(TAG, "setSideListType no fileSetList");
            return;
        }

        Log.i(TAG, "setSideListType Cur=" + mCurrentType + " To " + type);
        if (mCurrentType == type) {
            return;
        }
        mCurrentType = type;

        boolean hasIndex = false;
        if (type == ShowViewModel.LIST_TYPE_INDEX) {
            hasIndex = fileSetList.hasIndex;
        }
        if (hasIndex) {
            List<FileSet> indexedList = new ArrayList<>();
            for (FileSet mFile : fileSetList) {
                if (mFile.indexed) {
                    indexedList.add(mFile);
                }
            }
            sideListLiveData.setValue(indexedList);
        } else {
            sideListLiveData.setValue(fileSetList.list());
        }
    }

    /*********************************************************************
     * Save Current
     *********************************************************************/
    private AppDatabase mDbConnector;
    private FolderProp mFolderProp;

    private void initDbConnector(Application a) {
        mDbConnector = DbConnection.connect(a);
    }

    private void loadFolderProp(FolderInfo folder) {
        mFolderProp = mDbConnector.folderPropDao().findById(folder.filename);
        if (mFolderProp == null) {
            mFolderProp = new FolderProp();
            mFolderProp.folderId = folder.filename;
        }
        Log.i(TAG, "loadFolderProp Folder=" + folder.filename + " File=" + mFolderProp.currentFile);
    }

    void saveFolderProp() {
        ShowFileSet sfs = currentLiveData.getValue();
        if (sfs == null) {
            return;
        }
        mFolderProp.currentFile = sfs.fileSet.imageFile.filename;
        mDbConnector.folderPropDao().save(mFolderProp);
        Log.i(TAG, "saveFolderProp Folder=" + mFolderProp.folderId + " File=" + mFolderProp.currentFile);
    }

    /*********************************************************************
     * Toggle Orientation
     *********************************************************************/
    private int orientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;

    int toggleOrientation() {
        switch (orientation) {
            case ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED:
                orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                break;
            case ActivityInfo.SCREEN_ORIENTATION_PORTRAIT:
                orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                break;
            case ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE:
                orientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;
                break;
        }
        return orientation;
    }
}
