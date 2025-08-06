package ex1.siv.storage.media;

import android.net.Uri;

import androidx.documentfile.provider.DocumentFile;

import ex1.siv.file.DocumentFileController;
import ex1.siv.file.FileController;
import ex1.siv.storage.StorageContext;
import ex1.siv.storage.data.FolderInfo;
import ex1.siv.util.ContentUtil;

public class MediaFolderInfo extends FolderInfo {
    private final ContentUtil.ContentItem target;

    MediaFolderInfo(ContentUtil.ContentItem target) {
        super(target.name);
        this.target = target;
    }

    MediaFolderInfo(DocumentFile docFile) {
        this(new ContentUtil.ContentItem(docFile));
    }

    @Override
    public Uri toUri() {
        return target.getDocumentUri();
    }

    @Override
    public FileController toFileController(StorageContext context) {
        return new DocumentFileController(getDocumentFile(context), target.getParentDocumentFile(context.getMyContext()));
    }

    DocumentFile getDocumentFile(StorageContext context) {
        return target.getDocumentFile(context.getMyContext());
    }
}
