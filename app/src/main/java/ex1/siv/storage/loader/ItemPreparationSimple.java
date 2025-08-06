package ex1.siv.storage.loader;

import ex1.siv.storage.data.FileSetList;

public class ItemPreparationSimple extends ItemPreparation {
    @Override
    public void prepare(ItemLoader loader, FileSetList list, int pos) {
        loader.clearAllCacheRequest();

        prepareIt(loader, list, pos + 1);
        prepareIt(loader, list, pos - 1);
        prepareAll(loader, list, pos + 2, pos + 5);
    }
}
