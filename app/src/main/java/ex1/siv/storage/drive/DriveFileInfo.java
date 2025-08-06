package ex1.siv.storage.drive;

import com.google.api.services.drive.model.File;

import ex1.siv.storage.data.FileInfo;
import ex1.siv.util.GoogleDriveAccess;

public class DriveFileInfo extends FileInfo {
    final String fileId;
    private final long modified;

    DriveFileInfo(File file) {
        super(file.getName());
        this.fileId = file.getId();
        this.modified = GoogleDriveAccess.toSecondEpoch(file.getModifiedTime());
    }

    @Override
    public long getModified() {
        return modified;
    }

    public File toDriveFile() {
        return GoogleDriveAccess.getFileById(fileId);
    }
}