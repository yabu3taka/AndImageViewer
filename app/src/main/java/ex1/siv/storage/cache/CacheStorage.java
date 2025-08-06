package ex1.siv.storage.cache;

import java.io.File;

import ex1.siv.storage.data.FileSetList;
import ex1.siv.storage.data.FolderInfo;
import ex1.siv.storage.task.ResultTaskStarter;

public interface CacheStorage {
    CacheDownloader getCacheDownloader(FolderInfo origFolder, File toFolder);

    ResultTaskStarter<FileSetList> loadFileList(FolderInfo folder);
}
