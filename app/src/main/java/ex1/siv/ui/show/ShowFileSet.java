package ex1.siv.ui.show;

import ex1.siv.storage.data.FileSet;
import ex1.siv.storage.data.FileSetList;

public class ShowFileSet {
    public final FileSet fileSet;
    public final int pos;

    public ShowFileSet(FileSet fileSet, int pos) {
        this.fileSet = fileSet;
        this.pos = pos;
    }

    public ShowFileSet(FileSetList fileSetList, int pos) {
        this.fileSet = fileSetList.get(pos);
        this.pos = pos;
    }
}
