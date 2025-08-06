package ex1.siv.storage.cursor;

import ex1.siv.storage.data.FileSet;

public class EmptyCursor implements FileSetCursor {

    @Override
    public boolean hasNext() {
        return false;
    }

    @Override
    public FileSet getCurrentFile() {
        return null;
    }

    @Override
    public boolean setCurrentFile(FileSet f, CursorChangeListener l) {
        return false;
    }

    @Override
    public boolean setCurrentFile(int pos, CursorChangeListener l) {
        return false;
    }

    @Override
    public boolean movePos(int direction, CursorChangeListener l) {
        return false;
    }

    @Override
    public boolean moveIndexPos(int direction, CursorChangeListener l) {
        return false;
    }
}
