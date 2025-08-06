package ex1.siv.storage.media;

import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.documentfile.provider.DocumentFile;

import com.google.common.base.Strings;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;

import ex1.siv.storage.Storage;
import ex1.siv.storage.StorageContext;
import ex1.siv.storage.data.FileInfo;
import ex1.siv.storage.data.FileSet;
import ex1.siv.storage.data.FileSetList;
import ex1.siv.storage.data.FileSetListBuilder;
import ex1.siv.storage.data.FolderInfo;
import ex1.siv.storage.data.FolderImgAndText;
import ex1.siv.storage.data.FolderText;
import ex1.siv.storage.data.TopFolderInfo;
import ex1.siv.storage.exception.StorageException;
import ex1.siv.storage.favorite.FavoriteManager;
import ex1.siv.storage.favorite.SimpleFavoriteSaver;
import ex1.siv.storage.loader.ItemLoader;
import ex1.siv.storage.loader.ItemLoaderGeneric;
import ex1.siv.storage.task.LoadingActionSimple;
import ex1.siv.storage.task.LoadingCancellationToken;
import ex1.siv.storage.task.LoadingResultTaskStarter;
import ex1.siv.storage.task.QuickResultTaskStarter;
import ex1.siv.storage.task.ResultAction;
import ex1.siv.storage.task.ResultActionBitmap;
import ex1.siv.storage.task.ResultActionText;
import ex1.siv.storage.task.ResultCallback;
import ex1.siv.storage.task.ResultFileOpener;
import ex1.siv.storage.task.ResultTaskStarter;
import ex1.siv.storage.task.TaskId;
import ex1.siv.storage.task.ThreadResultTaskStarter;
import ex1.siv.util.BitmapUtil;
import ex1.siv.util.ContentUtil;
import ex1.siv.util.ListUtil;

public class MediaStorage implements Storage {
    private final static String TAG = MediaStorage.class.getSimpleName();

    @Override
    public ResultTaskStarter<TopFolderInfo> loadTopFolder(@NonNull StorageContext context, @NonNull String dir) {
        Log.i(TAG, "loadTopFolder D=" + dir);
        final TaskId<TopFolderInfo> id = new TaskId<>(dir);

        try {
            DocumentFile dirFile = DocumentFile.fromTreeUri(context.getMyContext(), Uri.parse(dir));
            if (dirFile == null || !dirFile.isDirectory()) {
                throw new StorageException("No such dir " + dir);
            }

            DocumentFile passFile = dirFile.findFile(TopFolderInfo.PASS_FILE);
            if (passFile == null || !passFile.isFile()) {
                throw new StorageException("No Pass File " + dir);
            }

            try (InputStream inputStream = openInputStream(context, passFile)) {
                TopFolderInfo topFolder = TopFolderInfo.create(new MediaFolderInfo(dirFile), inputStream);
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
            DocumentFile dirFile = ((MediaFolderInfo) folder).getDocumentFile(context);
            Log.i(TAG, "loadFolderList CLS=" + dirFile + " N=" + dirFile.getUri());
            if (!dirFile.isDirectory()) {
                throw new StorageException("Not Directory");
            }

            List<FolderInfo> files = ContentUtil.listFile(context.getMyContext(), dirFile, MediaFolderInfo::new, true);
            if (ListUtil.isNullOrEmpty(files)) {
                throw new StorageException("Can not open dir, or empty dir");
            }

            Log.i(TAG, "loadFolderList END");
            return new QuickResultTaskStarter<>(id, files);
        } catch (Exception ex) {
            Log.e(TAG, "loadFolderList Exception", ex);
            return new QuickResultTaskStarter<>(id, 0, ex);
        }
    }

    @Override
    public FolderInfo getFromUri(@NonNull StorageContext context, @NonNull Uri uri) {
        DocumentFile file = DocumentFile.fromTreeUri(context.getMyContext(), uri);
        assert file != null;
        return new MediaFolderInfo(file);
    }

    @Override
    public ResultTaskStarter<FolderImgAndText> getFolderImgAndText(@NonNull StorageContext context, @NonNull FolderInfo folder, String imgName) {
        Log.i(TAG, "getFolderImgAndText N=" + folder.filename);
        final TaskId<FolderImgAndText> id = new TaskId<>(folder);
        try {
            DocumentFile dirFile = ((MediaFolderInfo) folder).getDocumentFile(context);

            ContentUtil.FileFinder finder = ContentUtil.getFinder(context.getMyContext(), dirFile);
            finder.addTarget(FolderText.FOLDER_FILE);
            finder.addTarget(imgName);
            ContentUtil.DocumentFileMap map = finder.findFile();
            assert map != null;

            DocumentFile folderInfoFile = map.getFile(FolderText.FOLDER_FILE);
            FolderText text = new FolderText();
            if (folderInfoFile != null) {
                Log.i(TAG, "getFolderImgAndText Text find");
                try (InputStream inputStream = openInputStream(context, folderInfoFile)) {
                    text = FolderText.createFromFolderFile(inputStream);
                }
            }

            Bitmap img = null;
            if (!Strings.isNullOrEmpty(imgName)) {
                Log.i(TAG, "getFolderImgAndText img=" + imgName);
                DocumentFile imgFile = map.getFile(imgName);
                if (imgFile != null) {
                    Log.i(TAG, "getFolderImgAndText img find");
                    try (InputStream is = ContentUtil.openInputStream(context.getMyContext(), imgFile)) {
                        img = BitmapUtil.loadBitmap(is, imgFile.getName());
                    }
                }
            }
            FolderImgAndText ret = new FolderImgAndText(text, img);
            return new QuickResultTaskStarter<>(id, ret);
        } catch (Exception ex) {
            Log.e(TAG, "loadTopFolder Exception", ex);
            return new QuickResultTaskStarter<>(id, 0, ex);
        }
    }

    @Override
    public FavoriteManager getFavoriteManager(@NonNull StorageContext context) {
        return new FavoriteManager(new SimpleFavoriteSaver(context));
    }

    /*********************************************************************
     * ItemLoader
     *********************************************************************/
    @Override
    public ItemLoader getItemLoader(@NonNull StorageContext context, @NonNull FolderInfo folder) {
        return new MyItemLoader(context, folder);
    }

    private static InputStream openInputStream(StorageContext context, DocumentFile docFile) throws FileNotFoundException {
        return ContentUtil.openInputStream(context.getMyContext(), docFile);
    }

    private static class MyItemLoader extends ItemLoaderGeneric<MediaFolderInfo>
            implements ResultFileOpener<DocumentFile>, FileSetListBuilder.SettingFileOpener<MediaFileInfo> {
        private MyItemLoader(StorageContext context, FolderInfo folder) {
            super(context, (MediaFolderInfo) folder);
        }

        @Override
        public InputStream openSettingFile(MediaFileInfo file) throws Exception {
            return MediaStorage.openInputStream(context, file.getDocumentFile(context));
        }

        @Override
        public ResultTaskStarter<FileSetList> loadFileListInternal(MediaFolderInfo folder) {
            Log.i(TAG, "loadFileListInternal F=" + folder.filename);
            TaskId<FileSetList> id = new TaskId<>(folder);
            LoadingActionSimple<FileSetList> action = new LoadingActionSimple<FileSetList>(id) {
                public void loadResultInternal(ResultCallback<FileSetList> callback, LoadingCancellationToken token) throws Exception {
                    DocumentFile dirFile = folder.getDocumentFile(context);

                    Log.i(TAG, "loadFileListInternal CLS=" + dirFile + " N=" + dirFile.getUri());
                    if (!dirFile.isDirectory()) {
                        throw new StorageException("Not Directory");
                    }

                    FileSetListBuilder<MediaFileInfo> builder = new FileSetListBuilder<>(MediaFileInfo::new);
                    ContentUtil.listing(context.getMyContext(), dirFile, builder, false);
                    if (builder.isEmpty()) {
                        throw new StorageException("Can not open dir, or empty dir");
                    }

                    builder.loadIndex(MyItemLoader.this);
                    builder.loadSetting(MyItemLoader.this);

                    Log.i(TAG, "loadFileListInternal END");
                    callback.onResult(id.createResultForEnding(builder.createFileSetList()));
                }
            };
            return new LoadingResultTaskStarter<>(action);
        }

        @Override
        public DocumentFile getResultFile(@NonNull FileInfo file) {
            return ((MediaFileInfo) file).getDocumentFile(context);
        }

        @Override
        public InputStream openResultFile(DocumentFile resultFile, FileInfo file) throws Exception {
            return MediaStorage.openInputStream(context, resultFile);
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
}
