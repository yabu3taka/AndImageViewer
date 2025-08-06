package ex1.siv.storage.local;

import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import ex1.siv.storage.data.FolderImgAndText;
import ex1.siv.storage.data.FolderText;
import ex1.siv.storage.favorite.FavoriteManager;
import ex1.siv.storage.loader.ItemLoaderGeneric;
import ex1.siv.storage.StorageContext;
import ex1.siv.storage.loader.ItemLoader;
import ex1.siv.storage.data.FileInfo;
import ex1.siv.storage.exception.StorageException;
import ex1.siv.storage.data.FileSet;
import ex1.siv.storage.data.FileSetListBuilder;
import ex1.siv.storage.data.FileSetList;
import ex1.siv.storage.data.FolderInfo;
import ex1.siv.storage.task.ResultAction;
import ex1.siv.storage.task.ResultActionBitmap;
import ex1.siv.storage.task.ResultFileOpener;
import ex1.siv.storage.task.ResultTaskStarter;
import ex1.siv.storage.task.QuickResultTaskStarter;
import ex1.siv.storage.Storage;
import ex1.siv.storage.task.TaskId;
import ex1.siv.storage.task.ThreadResultTaskStarter;
import ex1.siv.storage.data.TopFolderInfo;
import ex1.siv.util.BitmapUtil;
import ex1.siv.util.ListUtil;

public class LocalStorage implements Storage {
    private final static String TAG = LocalStorage.class.getSimpleName();

    @Override
    public ResultTaskStarter<TopFolderInfo> loadTopFolder(@NonNull StorageContext context, @NonNull String dir) {
        Log.i(TAG, "loadTopFolder D=" + dir);
        final TaskId<TopFolderInfo> id = new TaskId<>(dir);

        try {
            File dirFile = new File(dir);
            if (!dirFile.isDirectory()) {
                throw new StorageException("No such dir " + dirFile.getAbsolutePath());
            }

            File passFile = new File(dirFile, TopFolderInfo.PASS_FILE);
            if (!passFile.isFile()) {
                throw new StorageException("No Pass File " + dirFile.getAbsolutePath());
            }

            try (InputStream inputStream = Files.newInputStream(passFile.toPath())) {
                TopFolderInfo topFolder = TopFolderInfo.create(new LocalFolderInfo(dirFile), inputStream);
                return new QuickResultTaskStarter<>(id, topFolder);
            }
        } catch (Exception ex) {
            Log.e(TAG, "loadTopFolder Exception", ex);
            return new QuickResultTaskStarter<>(id, 0, ex);
        }
    }

    @Override
    public ResultTaskStarter<List<FolderInfo>> loadFolderList(@NonNull StorageContext context, @NonNull FolderInfo folder) {
        final TaskId<List<FolderInfo>> id = new TaskId<>(folder);
        try {
            File dirFile = ((LocalFolderInfo) folder).toFile();
            List<FolderInfo> ret = getFolderListQuickly(dirFile);
            return new QuickResultTaskStarter<>(id, ret);
        } catch (Exception ex) {
            Log.e(TAG, "loadFolderList Exception", ex);
            return new QuickResultTaskStarter<>(id, 0, ex);
        }
    }

    public static List<FolderInfo> getFolderListQuickly(File dirFile) {
        Log.i(TAG, "getFolderListQuickly F=" + dirFile);
        if (!dirFile.isDirectory()) {
            throw new StorageException("Not Directory");
        }

        File[] files = dirFile.listFiles(File::isDirectory);
        assert files != null;

        List<FolderInfo> ret = new ArrayList<>();
        for (File file : files) {
            ret.add(new LocalFolderInfo(dirFile, file.getName()));
        }
        return ret;
    }

    @Override
    public FolderInfo getFromUri(@NonNull StorageContext context, @NonNull Uri uri) {
        String path = uri.getPath();
        assert path != null;
        return new LocalFolderInfo(new File(path));
    }

    public FolderInfo getFromFile(File tmp) {
        return new LocalFolderInfo(tmp);
    }

    @Override
    public ResultTaskStarter<FolderImgAndText> getFolderImgAndText(@NonNull StorageContext context, @NonNull FolderInfo folder, String imgName) {
        final TaskId<FolderImgAndText> id = new TaskId<>(folder);
        try {
            FolderText text = new FolderText();
            File folderFile = new File(((LocalFolderInfo) folder).target, FolderText.FOLDER_FILE);
            if (folderFile.isFile()) {
                try (InputStream inputStream = Files.newInputStream(folderFile.toPath())) {
                    text = FolderText.createFromFolderFile(inputStream);
                }
            }
            return new QuickResultTaskStarter<>(id, new FolderImgAndText(text, null));
        } catch (Exception ex) {
            Log.e(TAG, "loadTopFolder Exception", ex);
            return new QuickResultTaskStarter<>(id, 0, ex);
        }
    }

    @Override
    public FavoriteManager getFavoriteManager(@NonNull StorageContext context) {
        return null;
    }

    /*********************************************************************
     * ItemLoader
     *********************************************************************/
    @Override
    public ItemLoader getItemLoader(@NonNull StorageContext context, @NonNull FolderInfo folder) {
        return new MyItemLoader(context, folder);
    }

    private static class MyItemLoader extends ItemLoaderGeneric<LocalFolderInfo>
            implements ResultFileOpener<File>, FileSetListBuilder.SettingFileOpener<LocalFileInfo> {
        private MyItemLoader(StorageContext context, FolderInfo folder) {
            super(context, (LocalFolderInfo) folder);
        }

        @Override
        public InputStream openSettingFile(LocalFileInfo file) throws Exception {
            return Files.newInputStream(file.target.toPath());
        }

        @Override
        public ResultTaskStarter<FileSetList> loadFileListInternal(LocalFolderInfo folder) {
            Log.i(TAG, "loadFileListInternal F=" + folder);
            final TaskId<FileSetList> id = new TaskId<>(folder);
            try {
                File dirFile = folder.toFile();
                if (!dirFile.isDirectory()) {
                    throw new StorageException("Not Directory");
                }

                String[] files = dirFile.list();
                if (ListUtil.isNullOrEmpty(files)) {
                    throw new StorageException("Can not open dir, or empty dir");
                }

                FileSetListBuilder<LocalFileInfo> builder = new FileSetListBuilder<>();
                for (String file : files) {
                    builder.add(new LocalFileInfo(dirFile, file));
                }

                builder.loadIndex(MyItemLoader.this);
                builder.loadSetting(MyItemLoader.this);

                FileSetList list = builder.createFileSetList();
                return new QuickResultTaskStarter<>(id, list);
            } catch (Exception ex) {
                Log.e(TAG, "loadFileList Exception", ex);
                return new QuickResultTaskStarter<>(id, 0, ex);
            }
        }

        @Override
        public File getResultFile(@NonNull FileInfo file) {
            return ((LocalFileInfo) file).target;
        }

        @Override
        public InputStream openResultFile(File resultFile, FileInfo file) throws Exception {
            return Files.newInputStream(resultFile.toPath());
        }

        @Override
        protected ResultTaskStarter<Bitmap> loadImageInternal(FileInfo file, int reqWidth, int reqHeight) {
            Log.i(TAG, "loadImageInternal F=" + file);
            ResultAction<Bitmap> action = new ResultActionBitmap<>(this, file, reqWidth, reqHeight);
            return new ThreadResultTaskStarter<>(action);
        }

        @Override
        protected ResultTaskStarter<String> loadTextInternal(FileInfo file) {
            Log.i(TAG, "loadTextInternal F=" + file);
            final TaskId<String> id = new TaskId<>(file);
            try {
                File target = getResultFile(file);
                try (InputStream inputStream = openResultFile(target, file)) {
                    String text = BitmapUtil.getPhotoText(inputStream, target.getName());
                    return new QuickResultTaskStarter<>(id, text);
                }
            } catch (Exception ex) {
                Log.e(TAG, "loadTextFile Exception", ex);
                return new QuickResultTaskStarter<>(id, 0, ex);
            }
        }

        @Override
        public void prepareCache(FileSet file) {
        }

        @Override
        public void clearAllCacheRequest() {
        }
    }
}
