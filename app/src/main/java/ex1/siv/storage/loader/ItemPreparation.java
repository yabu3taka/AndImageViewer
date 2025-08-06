package ex1.siv.storage.loader;

import ex1.siv.storage.data.FileSetList;

public abstract class ItemPreparation {
    protected void prepareIt(ItemLoader loader, FileSetList list, int i) {
        if (0 <= i && i < list.count) {
            loader.prepareCache(list.get(i));
        }
    }

    protected void prepareAll(ItemLoader loader, FileSetList list, int from, int to) {
        boolean outFrom = false;
        if (from < 0) {
            from = 0;
            outFrom = true;
        } else if (from >= list.count) {
            from = list.count - 1;
            outFrom = true;
        }

        boolean outTo = false;
        if (to < 0) {
            to = 0;
            outTo = true;
        } else if (to >= list.count) {
            to = list.count - 1;
            outTo = true;
        }

        if (outFrom && outTo) {
            return;
        }

        if (from < to) {
            for (int i = from; i <= to; ++i) {
                loader.prepareCache(list.get(i));
            }
        } else {
            for (int i = from; i >= to; --i) {
                loader.prepareCache(list.get(i));
            }
        }
    }

    public abstract void prepare(ItemLoader loader, FileSetList list, int pos);
}
