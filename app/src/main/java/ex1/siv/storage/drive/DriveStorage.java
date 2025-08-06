package ex1.siv.storage.drive;

import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.api.services.drive.model.File;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import ex1.siv.storage.data.FolderImgAndText;
import ex1.siv.storage.data.FolderText;
import ex1.siv.storage.favorite.FavoriteManager;
import ex1.siv.storage.StorageContext;
import ex1.siv.modified.ModifiedChecker;
import ex1.siv.modified.ModifiedCheckerDatFile;
import ex1.siv.modified.ModifiedCheckerDirect;
import ex1.siv.storage.cache.CacheDownloaderSimple;
import ex1.siv.storage.data.FileInfoList;
import ex1.siv.storage.task.QuickResultTaskStarter;
import ex1.siv.storage.task.ResultActionSimple;
import ex1.siv.storage.task.TaskId;
import ex1.siv.storage.task.ThreadResultTaskStarter;
import ex1.siv.storage.data.FileInfo;
import ex1.siv.storage.loader.ItemLoader;
import ex1.siv.storage.cache.CacheDownloader;
import ex1.siv.storage.cache.CacheItemLoader;
import ex1.siv.storage.cache.CacheManager;
import ex1.siv.storage.task.ResultAction;
import ex1.siv.storage.exception.StorageException;
import ex1.siv.storage.data.FileSetListBuilder;
import ex1.siv.storage.data.FileSetList;
import ex1.siv.storage.data.FolderInfo;
import ex1.siv.storage.task.ResultTaskStarter;
import ex1.siv.storage.Storage;
import ex1.siv.storage.cache.CacheStorage;
import ex1.siv.storage.data.TopFolderInfo;
import ex1.siv.util.FileUtil;
import ex1.siv.util.GoogleDriveAccess;
import ex1.siv.util.ListUtil;

public class DriveStorage implements Storage, CacheStorage, FileSetListBuilder.SettingFileOpener<DriveFileInfo> {
    private final static String TAG = DriveStorage.class.getSimpleName();

    final static String AUTHORITY = "gdrive";

    private final GoogleDriveAccess mDrive;

    public static boolean isGoogleDrive(Uri uri) {
        return AUTHORITY.equals(uri.getAuthority());
    }

    DriveStorage(GoogleDriveAccess d) {
        mDrive = d;
    }

    @Override
    public ResultTaskStarter<TopFolderInfo> loadTopFolder(@NonNull StorageContext context, @NonNull final String dir) {
        Log.i(TAG, "loadTopFolder D=" + dir);
        final TaskId<TopFolderInfo> id = new TaskId<>(dir);
        ResultAction<TopFolderInfo> action = new ResultActionSimple<TopFolderInfo>(id) {
            @Override
            public TopFolderInfo getResultInternal() throws Exception {
                File file = mDrive.getFileInfo(null, dir);
                try (InputStream inputStream = mDrive.openFileInFolder(file, TopFolderInfo.PASS_FILE)) {
                    return TopFolderInfo.create(new DriveFolderInfo(file), inputStream);
                }
            }
        };
        return new ThreadResultTaskStarter<>(action);
    }

    @Override
    public ResultTaskStarter<List<FolderInfo>> loadFolderList(@NonNull StorageContext context, @NonNull final FolderInfo folder) {
        Log.i(TAG, "loadFolderList F=" + folder.filename);
        final TaskId<List<FolderInfo>> id = new TaskId<>(folder);
        ResultAction<List<FolderInfo>> action = new ResultActionSimple<List<FolderInfo>>(id) {
            @Override
            public List<FolderInfo> getResultInternal() throws Exception {
                File folderId = ((DriveFolderInfo) folder).toDriveFile();
                List<File> files = mDrive.getFolderList(folderId);
                if (ListUtil.isNullOrEmpty(files)) {
                    throw new StorageException("Can not open dir, or empty dir");
                }

                List<FolderInfo> ret = new ArrayList<>();
                for (File file : files) {
                    ret.add(new DriveFolderInfo(file));
                }
                return ret;
            }
        };
        return new ThreadResultTaskStarter<>(action);
    }

    @Override
    public FolderInfo getFromUri(@NonNull StorageContext context, @NonNull Uri uri) {
        Log.i(TAG, "URI=" + uri);
        List<String> list = uri.getPathSegments();
        return new DriveFolderInfo(list.get(0), list.get(1));
    }

    @Override
    public ResultTaskStarter<FolderImgAndText> getFolderImgAndText(@NonNull StorageContext context, @NonNull FolderInfo folder, String imgName) {
        final TaskId<FolderImgAndText> id = new TaskId<>(folder);
        try {
            FolderText text;
            File folderFile = ((DriveFolderInfo) folder).toDriveFile();
            try (InputStream inputStream = mDrive.openFileInFolder(folderFile, FolderText.FOLDER_FILE)) {
                text = FolderText.createFromFolderFile(inputStream);
            }
            return new QuickResultTaskStarter<>(id, new FolderImgAndText(text, null));
        } catch (Exception ex) {
            Log.e(TAG, "loadTopFolder Exception", ex);
            return new QuickResultTaskStarter<>(id, 0, ex);
        }
    }

    @Override
    public InputStream openSettingFile(DriveFileInfo file) throws Exception {
        return mDrive.openFile(file.toDriveFile());
    }

    @Override
    public ResultTaskStarter<FileSetList> loadFileList(final FolderInfo folder) {
        Log.i(TAG, "loadFileList F=" + folder.filename);
        final TaskId<FileSetList> id = new TaskId<>(folder);
        ResultAction<FileSetList> action = new ResultActionSimple<FileSetList>(id) {
            @Override
            public FileSetList getResultInternal() throws Exception {
                File folderId = ((DriveFolderInfo) folder).toDriveFile();

                List<File> files = mDrive.getFileList(folderId);
                if (ListUtil.isNullOrEmpty(files)) {
                    throw new StorageException("Can not open dir, or empty dir");
                }

                FileSetListBuilder<DriveFileInfo> builder = new FileSetListBuilder<>();
                for (File file : files) {
                    builder.add(new DriveFileInfo(file));
                }
                files.clear();

                builder.loadIndex(DriveStorage.this);
                builder.loadSetting(DriveStorage.this);
                return builder.createFileSetList();
            }
        };
        return new ThreadResultTaskStarter<>(action);
    }

    @Override
    public FavoriteManager getFavoriteManager(@NonNull StorageContext context) {
        return new FavoriteManager(new CacheManager(context));
    }

    /*********************************************************************
     * ItemLoader
     *********************************************************************/
    @Override
    public ItemLoader getItemLoader(@NonNull StorageContext context, @NonNull FolderInfo folder) {
        CacheManager cacheManager = new CacheManager(context);
        java.io.File toFolder;
        CacheDownloader downloader;
        if (cacheManager.hasCacheFolder(folder)) {
            toFolder = cacheManager.getCacheFolder(folder);
            downloader = getCacheDownloader(folder, toFolder);
        } else {
            toFolder = new java.io.File(context.getMyContext().getCacheDir(), folder.filename);
            if (!FileUtil.existsOrMake(toFolder)) {
                throw new StorageException("make folder failed");
            }
            downloader = getCacheDownloader(new ModifiedCheckerDirect(), folder, toFolder);
        }
        return CacheItemLoader.getItemLoader(context, downloader, folder, toFolder);
    }

    /*********************************************************************
     * Cache
     *********************************************************************/
    @Override
    public CacheDownloader getCacheDownloader(FolderInfo origFolder, java.io.File toFolder) {
        return getCacheDownloader(new ModifiedCheckerDatFile(toFolder), origFolder, toFolder);
    }

    private CacheDownloader getCacheDownloader(ModifiedChecker checker, final FolderInfo origFolder, java.io.File toFolder) {
        return new CacheDownloaderSimple(toFolder, checker) {
            @Override
            public CacheStorage getCacheStorage() {
                return DriveStorage.this;
            }

            @Override
            public FileInfoList geServerFileList() throws Exception {
                File folderId = ((DriveFolderInfo) origFolder).toDriveFile();
                Log.i(TAG, "geCacheFileList Id=" + folderId + " name=" + origFolder.filename);

                List<File> files = mDrive.getFileList(folderId);
                if (ListUtil.isNullOrEmpty(files)) {
                    Log.e(TAG, "geCacheFileList cacheFolder is empty.");
                    return null;
                }

                FileSetListBuilder<DriveFileInfo> builder = new FileSetListBuilder<>();
                for (File file : files) {
                    builder.add(new DriveFileInfo(file));
                }
                return builder.createFileInfoList();
            }

            @Override
            protected void cacheFileInternal(FileInfo file) throws Exception {
                mDrive.downloadFile(((DriveFileInfo) file).toDriveFile(), getTmpCacheFile(file));
                commitCache(file);
            }
        };
    }

    /*********************************************************************
     * Direct ItemLoader
     *********************************************************************/
    /*
    private class DirectItemLoader extends ItemLoaderGeneric<DriveFolderInfo>
            implements ResultFileOpener<File> {
        private DirectItemLoader(StorageContext context, DriveFolderInfo folder) {
            super(context, folder);
        }

        @Override
        public ResultTaskStarter<FileSetList> loadFileListInternal(TaskId<FileSetList> id, DriveFolderInfo folder) {
            return DriveStorage.this.loadFileList(folder);
        }

        @Override
        public File getResultFile(@NonNull FileInfo file) {
            return ((DriveFileInfo) file).toDriveFile();
        }

        @Override
        public InputStream openResultFile(File resultFile, FileInfo file) throws Exception {
            return BitmapUtil.getInputStream(mDrive.openFile(resultFile), file.filename);
        }

        @Override
        protected ResultTaskStarter<Bitmap> loadImageInternal(FileInfo file, int reqWidth, int reqHeight) {
            Log.d(TAG, "DirectItemLoader.loadImageFile F=" + file);
            ResultAction<Bitmap> action = new ResultActionBitmap<>(this, file, 0, 0);
            return new ThreadResultTaskStarter<>(action);
        }

        @Override
        protected ResultTaskStarter<String> loadTextInternal(FileInfo file) {
            Log.d(TAG, "DirectItemLoader.loadTextFile F=" + file);
            ResultAction<String> action = new ResultActionText<>(this, file);
            return new ThreadResultTaskStarter<>(action);
        }

        @Override
        public void prepareCache(FileSet file) {
        }

        @Override
        public void clearAllCacheRequest() {
        }
    }
     */
}
