package ex1.siv.storage;

import android.net.Uri;

import androidx.annotation.NonNull;

import java.util.List;

import ex1.siv.storage.data.FolderImgAndText;
import ex1.siv.storage.favorite.FavoriteManager;
import ex1.siv.storage.loader.ItemLoader;
import ex1.siv.storage.task.ResultTaskStarter;
import ex1.siv.storage.data.FolderInfo;
import ex1.siv.storage.data.TopFolderInfo;

public interface Storage {
    ResultTaskStarter<TopFolderInfo> loadTopFolder(@NonNull StorageContext context, @NonNull String dir);

    ResultTaskStarter<List<FolderInfo>> loadFolderList(@NonNull StorageContext context, @NonNull FolderInfo folder);

    FolderInfo getFromUri(@NonNull StorageContext context, @NonNull Uri uri);

    ResultTaskStarter<FolderImgAndText> getFolderImgAndText(@NonNull StorageContext context, @NonNull FolderInfo folder, String imgName);

    ItemLoader getItemLoader(@NonNull StorageContext context, @NonNull FolderInfo folder);

    FavoriteManager getFavoriteManager(@NonNull StorageContext context);
}
