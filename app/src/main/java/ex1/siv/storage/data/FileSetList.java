package ex1.siv.storage.data;

import androidx.annotation.NonNull;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import ex1.siv.storage.cursor.FileSetCursor;
import ex1.siv.storage.cursor.EmptyCursor;

public class FileSetList implements Iterable<FileSet> {
    private final List<FileSet> mFiles;
    public final boolean hasIndex;
    public final boolean hasText;
    public final int count;

    public FileSetList(List<FileSet> list, boolean hasIndex, boolean hasText) {
        this.mFiles = list;
        this.count = list.size();
        this.hasIndex = hasIndex;
        this.hasText = hasText;
    }

    public FileSet get(int i) {
        return mFiles.get(i);
    }

    public List<FileSet> list() {
        return mFiles;
    }

    @NonNull
    public Iterator<FileSet> iterator() {
        return mFiles.iterator();
    }

    public int indexOf(String f) {
        for (int i = 0; i < count; ++i) {
            if (Objects.equals(mFiles.get(i).imageFile.filename, f)) {
                return i;
            }
        }
        return -1;
    }

    public int indexOf(FileSet f) {
        for (int i = 0; i < count; ++i) {
            if (Objects.equals(mFiles.get(i), f)) {
                return i;
            }
        }
        return -1;
    }

    public FileSetCursor getCursor(FileSet f) {
        if (count <= 0) {
            return new EmptyCursor();
        }

        if (f != null) {
            int pos = indexOf(f);
            if (pos >= 0) {
                return new MyCursor(pos);
            }
        }
        return new MyCursor(0);
    }

    private class MyCursor implements FileSetCursor {
        private int mPos;

        private MyCursor(int p) {
            mPos = p;
        }

        @Override
        public FileSet getCurrentFile() {
            return mFiles.get(mPos);
        }

        @Override
        public boolean hasNext() {
            return mPos + 1 < count;
        }

        private boolean changeCurrentPos(int p, CursorChangeListener l) {
            if (mPos == p) {
                return false;
            }
            FileSet file = mFiles.get(p);
            if (l.onPositionChanged(file, p)) {
                mPos = p;
                return true;
            } else {
                return false;
            }
        }

        @Override
        public boolean setCurrentFile(FileSet f, CursorChangeListener l) {
            for (int i = 0; i < count; ++i) {
                if (Objects.equals(mFiles.get(i), f)) {
                    return changeCurrentPos(i, l);
                }
            }
            return false;
        }

        @Override
        public boolean setCurrentFile(int pos, CursorChangeListener l) {
            return changeCurrentPos(pos, l);
        }

        @Override
        public boolean movePos(int direction, CursorChangeListener l) {
            if (direction == 0) {
                return false;
            }
            int newPos = mPos + direction;
            if (newPos < 0) {
                newPos = 0;
            } else if (newPos >= count) {
                newPos = count - 1;
            }
            return changeCurrentPos(newPos, l);
        }

        @Override
        public boolean moveIndexPos(int direction, CursorChangeListener l) {
            if (hasIndex) {
                if (direction > 0) {
                    for (int i = mPos + 1; i < count; ++i) {
                        if (!mFiles.get(i).indexed) {
                            continue;
                        }
                        direction--;
                        if (direction <= 0) {
                            return changeCurrentPos(i, l);
                        }
                    }
                } else {
                    direction = -direction;
                    for (int i = mPos - 1; i >= 0; --i) {
                        if (!mFiles.get(i).indexed) {
                            continue;
                        }
                        direction--;
                        if (direction <= 0) {
                            return changeCurrentPos(i, l);
                        }
                    }
                }
                return false;
            } else {
                return movePos(direction, l);
            }
        }
    }
}
