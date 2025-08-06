package ex1.siv.storage.task;

import ex1.siv.storage.StorageContext;

/**
 * 開始したタスクを管理する
 * ResultTaskStarter が開始する
 */
public interface ResultTask {
    void cancelMe(StorageContext context);
}
