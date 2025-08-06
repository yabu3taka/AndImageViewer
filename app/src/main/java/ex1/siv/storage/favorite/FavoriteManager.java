package ex1.siv.storage.favorite;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import ex1.siv.storage.data.FolderInfo;

public class FavoriteManager {
    private final FavoriteSaver saver;
    private final Set<String> collection;

    public FavoriteManager(FavoriteSaver saver) {
        this.saver = saver;
        collection = new HashSet<>();
    }

    public void replaceFavorite(Collection<FolderInfo> list) {
        collection.clear();
        for (FolderInfo folder : list) {
            collection.add(folder.filename);
        }
        saver.saveFavorite(collection);
    }

    public void loadFavorite() {
        collection.clear();
        saver.loadFavorite(collection);
    }

    public boolean isFavorite(FolderInfo folder) {
        return collection.contains(folder.filename);
    }
}
