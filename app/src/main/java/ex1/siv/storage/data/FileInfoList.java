package ex1.siv.storage.data;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FileInfoList {
    public final List<FileSet> fileSetList;
    public final Map<String, FileInfo> miscFile;

    FileInfoList(List<FileSet> list, Map<String, FileInfo> miscFile) {
        this.fileSetList = list;
        this.miscFile = miscFile;
    }

    private String toFilename(FileInfo f) {
        return f == null ? null : f.filename;
    }

    public Set<String> toFileCollection() {
        HashSet<String> ret = new HashSet<>();
        for (FileSet item : fileSetList) {
            ret.add(toFilename(item.imageFile));
            ret.add(toFilename(item.textFile));
        }
        for (FileInfo file : miscFile.values()) {
            ret.add(toFilename(file));
        }
        ret.remove(null);
        return ret;
    }
}
