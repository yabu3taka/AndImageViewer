package ex1.siv.storage.media;

import androidx.documentfile.provider.DocumentFile;

import ex1.siv.storage.StorageContext;
import ex1.siv.storage.data.FileInfo;
import ex1.siv.util.ContentUtil;

public class MediaFileInfo extends FileInfo {
    private final ContentUtil.ContentItem target;

    MediaFileInfo(ContentUtil.ContentItem target) {
        super(target.name);
        this.target = target;
    }

    DocumentFile getDocumentFile(StorageContext context) {
        return target.getDocumentFile(context.getMyContext());
    }

    @Override
    public long getModified() {
        return target.lastModified;
    }
}
