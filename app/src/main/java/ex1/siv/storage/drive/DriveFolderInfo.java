package ex1.siv.storage.drive;

import android.net.Uri;

import com.google.api.services.drive.model.File;

import ex1.siv.file.FileController;
import ex1.siv.storage.StorageContext;
import ex1.siv.storage.data.FolderInfo;
import ex1.siv.util.GoogleDriveAccess;

public class DriveFolderInfo extends FolderInfo {
    final String fileId;

    DriveFolderInfo(File file) {
        super(file.getName());
        this.fileId = file.getId();
    }

    DriveFolderInfo(String filename, String fileId) {
        super(filename);
        this.fileId = fileId;
    }

    @Override
    public Uri toUri() {
        Uri.Builder builder = new Uri.Builder();
        builder.scheme("imageFile");
        builder.authority(DriveStorage.AUTHORITY);
        builder.appendEncodedPath(filename);
        builder.appendEncodedPath(fileId);
        return builder.build();
    }

    @Override
    public FileController toFileController(StorageContext context) {
        return null;
    }

    public File toDriveFile() {
        return GoogleDriveAccess.getFileById(fileId);
    }
}