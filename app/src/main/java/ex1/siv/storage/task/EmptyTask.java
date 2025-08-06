package ex1.siv.storage.task;

import ex1.siv.storage.StorageContext;

/**
 * 何もしないタスク
 */
public class EmptyTask implements ResultTask {
    @Override
    public void cancelMe(StorageContext context) {
    }
}
