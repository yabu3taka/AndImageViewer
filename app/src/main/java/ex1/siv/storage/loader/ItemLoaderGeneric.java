package ex1.siv.storage.loader;

import ex1.siv.storage.StorageContext;
import ex1.siv.storage.data.FileSetList;
import ex1.siv.storage.data.FolderInfo;
import ex1.siv.storage.task.ResultTaskStarter;

public abstract class ItemLoaderGeneric<T extends FolderInfo> extends ItemLoader {
    protected final StorageContext context;
    private final T folder;

    public ItemLoaderGeneric(StorageContext context, T folder) {
        this.context = context;
        this.folder = folder;
    }

    @Override
    public ResultTaskStarter<FileSetList> loadFileList() {
        return loadFileListInternal(folder);
    }

    protected abstract ResultTaskStarter<FileSetList> loadFileListInternal(T folder);
}
