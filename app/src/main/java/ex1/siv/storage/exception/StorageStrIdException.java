package ex1.siv.storage.exception;

public class StorageStrIdException extends RuntimeException {
    public final int strId;

    public StorageStrIdException(int strId) {
        this.strId = strId;
    }
}
