package ex1.siv.storage.cursor;

import ex1.siv.storage.data.FileSet;

public interface FileSetCursor {
    interface CursorChangeListener {
        boolean onPositionChanged(FileSet f, int pos);
    }

    FileSet getCurrentFile();

    boolean hasNext();

    boolean setCurrentFile(FileSet f, CursorChangeListener l);

    boolean setCurrentFile(int pos, CursorChangeListener l);

    boolean movePos(int direction, CursorChangeListener l);

    boolean moveIndexPos(int direction, CursorChangeListener l);
}
