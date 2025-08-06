package ex1.siv.storage.favorite;

import java.util.Set;

public interface FavoriteSaver {
    void saveFavorite(Set<String> list);
    void loadFavorite(Set<String> map);
}
